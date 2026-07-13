package com.idbi.msme.config;

import com.google.cloud.firestore.Firestore;
import com.google.firebase.database.FirebaseDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("local")
public class FirebaseLocalConfig {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseLocalConfig.class);

    @Bean
    @Primary
    public Firestore firestore() {
        logger.warn("Running in LOCAL mode - using Firestore emulator/in-memory");
        try {
            return com.google.firebase.cloud.FirestoreClient.getFirestore();
        } catch (Exception e) {
            logger.warn("Firestore not available, backend will start without real DB: {}", e.getMessage());
            return null;
        }
    }

    @Bean
    @Primary
    public FirebaseDatabase firebaseDatabase() {
        logger.warn("Running in LOCAL mode - RTDB not available");
        try {
            return FirebaseDatabase.getInstance();
        } catch (Exception e) {
            logger.warn("RTDB not available: {}", e.getMessage());
            return null;
        }
    }
}
