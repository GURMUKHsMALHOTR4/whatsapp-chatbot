package com.chatbot.whatsappbot.service;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.DocumentReference;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class FirebaseMessageService {

    public void saveMessage(String from, String to, String message, String timestamp) {
        try {
            Firestore db = FirestoreClient.getFirestore();
            Map<String, Object> messageData = new HashMap<>();
            messageData.put("from", from);
            messageData.put("to", to);
            messageData.put("message", message);
            messageData.put("timestamp", timestamp);

            DocumentReference docRef = db.collection("messages").document();
            docRef.set(messageData);
            System.out.println("✅ Message saved to Firestore");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
