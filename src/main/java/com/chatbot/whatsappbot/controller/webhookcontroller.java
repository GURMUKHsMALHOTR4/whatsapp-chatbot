package com.chatbot.whatsappbot.controller;

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

@RestController
@RequestMapping("/webhook")
public class webhookcontroller {

    private final Firestore db;

    @Value("${whatsapp.phone-number-id}")
    private String phoneNumberId;

    @Value("${whatsapp.access-token}")
    private String accessToken;

    public webhookcontroller() throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {
            FileInputStream serviceAccount = new FileInputStream("src/main/resources/firebase-config.json");

            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setProjectId("whatsapp-chatbot-1127f")
                    .build();

            FirebaseApp.initializeApp(options);
        }

        this.db = FirestoreClient.getFirestore();
    }

    // ✅ Webhook verification
    @GetMapping
    public ResponseEntity<String> verifyWebhook(
            @RequestParam("hub.mode") String mode,
            @RequestParam("hub.verify_token") String token,
            @RequestParam("hub.challenge") String challenge
    ) {
        if ("subscribe".equals(mode) && "gurmukhverifytoken".equals(token)) {
            return ResponseEntity.ok(challenge);
        } else {
            return ResponseEntity.status(403).body("Verification failed");
        }
    }

    // ✅ Handle incoming messages
    @PostMapping
    public ResponseEntity<Void> receiveMessage(@RequestBody Map<String, Object> payload) {
        try {
            System.out.println("📩 Incoming Payload:\n" + payload);

            List<Map<String, Object>> entries = (List<Map<String, Object>>) payload.get("entry");
            if (entries == null || entries.isEmpty()) return ResponseEntity.ok().build();

            Map<String, Object> entry = entries.get(0);
            List<Map<String, Object>> changes = (List<Map<String, Object>>) entry.get("changes");
            if (changes == null || changes.isEmpty()) return ResponseEntity.ok().build();

            Map<String, Object> value = (Map<String, Object>) changes.get(0).get("value");
            if (value == null || !value.containsKey("messages")) return ResponseEntity.ok().build();

            List<Map<String, Object>> messages = (List<Map<String, Object>>) value.get("messages");
            if (messages == null || messages.isEmpty()) return ResponseEntity.ok().build();

            for (Map<String, Object> message : messages) {
                String from = (String) message.get("from");
                Map<String, Object> textObject = (Map<String, Object>) message.get("text");
                String text = textObject != null ? (String) textObject.get("body") : null;
                String timestamp = (String) message.get("timestamp");

                // ✅ Save message to Firestore
                Map<String, Object> data = new HashMap<>();
                data.put("from", from);
                data.put("message", text);
                data.put("timestamp", timestamp);

                DocumentReference docRef = db.collection("messages").document();
                ApiFuture<WriteResult> result = docRef.set(data);
                System.out.println("✅ Message saved to Firestore.");

                // ✅ Respond with plain text (no template)
                if (text != null) {
                    String lowerText = text.toLowerCase();
                    List<String> greetings = Arrays.asList("hi", "hello", "hey", "hii", "hola", "yo", "greetings", "helo", "sup");

                    boolean isGreeting = greetings.stream().anyMatch(lowerText::contains);

                    String reply = isGreeting
                            ? "👋 Hello! This is a test message from Gurmukh's WhatsApp Bot. How can I help you?"
                            : "📩 You said: " + text;

                    sendWhatsAppReply(from, reply);
                }
            }

        } catch (Exception e) {
            System.err.println("❌ Error processing message:");
            e.printStackTrace();
        }

        return ResponseEntity.ok().build();
    }

    // ✅ Send regular text reply
    private void sendWhatsAppReply(String recipientPhone, String messageText) {
        try {
            String json = String.format("""
            {
              "messaging_product": "whatsapp",
              "to": "%s",
              "type": "text",
              "text": {
                "body": "%s"
              }
            }
            """, recipientPhone, messageText);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://graph.facebook.com/v22.0/" + phoneNumberId + "/messages"))
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("📤 Replied to user. API Response:\n" + response.body());

        } catch (Exception e) {
            System.err.println("❌ Error sending WhatsApp reply:");
            e.printStackTrace();
        }
    }
}
