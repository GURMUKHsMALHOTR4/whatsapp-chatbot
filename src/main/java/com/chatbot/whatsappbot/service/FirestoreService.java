package com.chatbot.whatsappbot.service;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.DocumentReference;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class FirestoreService {

    public void saveMessage(String from, String messageBody) {
        Firestore db = FirestoreClient.getFirestore();

        Map<String, Object> message = new HashMap<>();
        message.put("from", from);
        message.put("message", messageBody);
        message.put("timestamp", System.currentTimeMillis());

        try {
            DocumentReference ref = db.collection("messages").add(message).get();
            System.out.println("✅ Message saved with ID: " + ref.getId());
        } catch (Exception e) {
            System.out.println("❌ Error saving message: " + e.getMessage());
        }
    }
}
