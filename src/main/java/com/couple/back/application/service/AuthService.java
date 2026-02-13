package com.couple.back.application.service;

import com.couple.back.application.dto.AuthResponse;
import com.couple.back.application.dto.CoupleStateDto;
import com.couple.back.application.dto.UserProfileDto;
import com.couple.back.domain.model.Couple;
import com.couple.back.domain.model.User;
import com.couple.back.domain.repository.UserRepository;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {
    
    private final FirebaseAuthService firebaseAuthService;
    private final UserRepository userRepository;
    
    @Transactional
    public AuthResponse authenticateWithFirebase(String idToken) throws FirebaseAuthException {
        // Verify Firebase token
        FirebaseToken decodedToken = firebaseAuthService.verifyToken(idToken);
        
        // Extract user info from token
        String firebaseUid = decodedToken.getUid();
        String email = decodedToken.getEmail();
        String name = decodedToken.getName();
        String photoUrl = decodedToken.getPicture();
        
        // Determine provider
        String provider = determineProvider(decodedToken);
        
        // Upsert user
        User user = upsertUser(firebaseUid, email, name, photoUrl, provider);
        
        // Build response
        return AuthResponse.builder()
                .user(buildUserProfile(user))
                .coupleState(buildCoupleState(user))
                .build();
    }
    
    private String determineProvider(FirebaseToken token) {
        // Check if Google sign-in
        if (token.getIssuer().contains("securetoken.google.com")) {
            Object signInProvider = token.getClaims().get("firebase");
            if (signInProvider != null) {
                String provider = signInProvider.toString();
                if (provider.contains("google.com")) {
                    return "GOOGLE";
                }
            }
        }
        return "EMAIL";
    }
    
    private User upsertUser(String firebaseUid, String email, String name, String photoUrl, String provider) {
        return userRepository.findByFirebaseUid(firebaseUid)
                .map(existingUser -> {
                    // Update existing user
                    existingUser.setEmail(email);
                    existingUser.setName(name);
                    existingUser.setPhotoUrl(photoUrl);
                    existingUser.setProvider(provider);
                    log.info("Updating existing user: {}", firebaseUid);
                    return userRepository.save(existingUser);
                })
                .orElseGet(() -> {
                    // Create new user
                    User newUser = User.builder()
                            .firebaseUid(firebaseUid)
                            .email(email)
                            .name(name)
                            .photoUrl(photoUrl)
                            .provider(provider)
                            .build();
                    log.info("Creating new user: {}", firebaseUid);
                    return userRepository.save(newUser);
                });
    }
    
    private UserProfileDto buildUserProfile(User user) {
        return UserProfileDto.builder()
                .id(user.getId())
                .firebaseUid(user.getFirebaseUid())
                .email(user.getEmail())
                .name(user.getName())
                .photoUrl(user.getPhotoUrl())
                .provider(user.getProvider())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
    
    private CoupleStateDto buildCoupleState(User user) {
        Couple couple = user.getCouple();
        if (couple != null) {
            return CoupleStateDto.builder()
                    .coupleId(couple.getId())
                    .coupleName(couple.getCoupleName())
                    .hasPartner(true)
                    .createdAt(couple.getCreatedAt())
                    .build();
        } else {
            return CoupleStateDto.builder()
                    .hasPartner(false)
                    .build();
        }
    }
}
