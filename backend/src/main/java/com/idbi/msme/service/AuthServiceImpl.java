package com.idbi.msme.service;

import com.idbi.msme.dto.UserProfileResponse;
import com.idbi.msme.exception.ResourceNotFoundException;
import com.idbi.msme.model.UserProfile;
import com.idbi.msme.repository.FirestoreDataAccess;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthServiceImpl implements AuthService {

    private final FirestoreDataAccess db;

    public AuthServiceImpl(FirestoreDataAccess db) {
        this.db = db;
    }

    @Override
    public UserProfileResponse getUserProfile(String uid) {
        Optional<UserProfile> profile = db.findUserById(uid);
        if (profile.isEmpty()) {
            throw new ResourceNotFoundException("User profile not found with ID: " + uid);
        }
        UserProfile u = profile.get();
        return new UserProfileResponse(u.getId(), u.getEmail(), u.getFullName(), u.getRole(), u.getPhone(), u.getStatus());
    }
}
