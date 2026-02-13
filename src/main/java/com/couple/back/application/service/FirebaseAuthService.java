package com.couple.back.application.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class FirebaseAuthService {
    
    public FirebaseToken verifyToken(String idToken) throws FirebaseAuthException {
        try {
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
            log.info("Successfully verified Firebase token for user: {}", decodedToken.getUid());
            return decodedToken;
        } catch (FirebaseAuthException e) {
            log.error("Failed to verify Firebase token: {}", e.getMessage());
            throw e;
        }
    }
}
