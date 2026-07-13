package com.idbi.msme.service;

import com.idbi.msme.dto.UserProfileResponse;

public interface AuthService {
    UserProfileResponse getUserProfile(String uid);
}
