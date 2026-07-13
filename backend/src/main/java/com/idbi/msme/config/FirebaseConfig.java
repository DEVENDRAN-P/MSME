package com.idbi.msme.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.database.FirebaseDatabase;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseConfig.class);

    @Value("${firebase.config-path:classpath:firebase-service-account.json}")
    private Resource serviceAccount;

    @Value("${firebase.project-id:}")
    private String projectId;

    @Value("${firebase.database-url:}")
    private String databaseUrl;

    @PostConstruct
    public void init() {
        if (FirebaseApp.getApps().isEmpty()) {
            try {
                InputStream serviceAccountStream = getCredentialsStream();
                FirebaseOptions.Builder builder = FirebaseOptions.builder();

                if (serviceAccountStream != null) {
                    builder.setCredentials(GoogleCredentials.fromStream(serviceAccountStream));
                    logger.info("Firebase Admin SDK initializing with service account credentials");
                } else {
                    builder.setCredentials(GoogleCredentials.getApplicationDefault());
                    logger.info("Firebase Admin SDK initializing with Application Default Credentials");
                }

                if (projectId != null && !projectId.isBlank()) {
                    builder.setProjectId(projectId);
                }
                if (databaseUrl != null && !databaseUrl.isBlank()) {
                    builder.setDatabaseUrl(databaseUrl);
                }

                FirebaseApp.initializeApp(builder.build());
                logger.info("Firebase Admin SDK initialized successfully");
            } catch (IOException e) {
                logger.error("Failed to initialize Firebase Admin SDK: {}", e.getMessage());
                throw new RuntimeException("Failed to initialize Firebase Admin SDK. " +
                        "Place firebase-service-account.json in src/main/resources/ or set GOOGLE_APPLICATION_CREDENTIALS.", e);
            }
        }
    }

    private InputStream getCredentialsStream() {
        if (serviceAccount != null && serviceAccount.exists()) {
            try {
                return serviceAccount.getInputStream();
            } catch (IOException e) {
                logger.warn("Could not read service account file: {}", e.getMessage());
            }
        }
        return null;
    }

    @Bean
    public Firestore firestore() {
        return FirestoreClient.getFirestore();
    }

    @Bean
    public FirebaseDatabase firebaseDatabase() {
        return FirebaseDatabase.getInstance();
    }
}
