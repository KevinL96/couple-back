package com.couple.back.application.service;

import com.couple.back.application.dto.AuthResponse;
import com.couple.back.domain.model.User;
import com.couple.back.domain.repository.UserRepository;
import com.google.firebase.ErrorCode;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthErrorCode;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    
    @Mock
    private FirebaseAuthService firebaseAuthService;
    
    @Mock
    private UserRepository userRepository;
    
    private AuthService authService;
    
    private FirebaseToken mockToken;
    
    @BeforeEach
    void setUp() {
        mockToken = mock(FirebaseToken.class);
        authService = new AuthService(firebaseAuthService, userRepository);
    }
    
    @Test
    void authenticateWithFirebase_NewUser_CreatesUser() throws FirebaseAuthException {
        // Arrange
        String idToken = "test-token";
        String firebaseUid = "firebase-uid-123";
        String email = "test@example.com";
        String name = "Test User";
        String photoUrl = "https://example.com/photo.jpg";
        
        when(mockToken.getUid()).thenReturn(firebaseUid);
        when(mockToken.getEmail()).thenReturn(email);
        when(mockToken.getName()).thenReturn(name);
        when(mockToken.getPicture()).thenReturn(photoUrl);
        when(mockToken.getIssuer()).thenReturn("securetoken.google.com");
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("firebase", "google.com");
        when(mockToken.getClaims()).thenReturn(claims);
        
        when(firebaseAuthService.verifyToken(idToken)).thenReturn(mockToken);
        when(userRepository.findByFirebaseUid(firebaseUid)).thenReturn(Optional.empty());
        
        User savedUser = User.builder()
                .id(1L)
                .firebaseUid(firebaseUid)
                .email(email)
                .name(name)
                .photoUrl(photoUrl)
                .provider("GOOGLE")
                .build();
        
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        
        // Act
        AuthResponse response = authService.authenticateWithFirebase(idToken);
        
        // Assert
        assertNotNull(response);
        assertNotNull(response.getUser());
        assertEquals(firebaseUid, response.getUser().getFirebaseUid());
        assertEquals(email, response.getUser().getEmail());
        assertEquals(name, response.getUser().getName());
        assertEquals("GOOGLE", response.getUser().getProvider());
        assertFalse(response.getCoupleState().isHasPartner());
        
        verify(firebaseAuthService, times(1)).verifyToken(idToken);
        verify(userRepository, times(1)).save(any(User.class));
    }
    
    @Test
    void authenticateWithFirebase_ExistingUser_UpdatesUser() throws FirebaseAuthException {
        // Arrange
        String idToken = "test-token";
        String firebaseUid = "firebase-uid-123";
        String email = "updated@example.com";
        String name = "Updated User";
        
        when(mockToken.getUid()).thenReturn(firebaseUid);
        when(mockToken.getEmail()).thenReturn(email);
        when(mockToken.getName()).thenReturn(name);
        when(mockToken.getPicture()).thenReturn(null);
        when(mockToken.getIssuer()).thenReturn("securetoken.google.com");
        
        Map<String, Object> claims = new HashMap<>();
        when(mockToken.getClaims()).thenReturn(claims);
        
        when(firebaseAuthService.verifyToken(idToken)).thenReturn(mockToken);
        
        User existingUser = User.builder()
                .id(1L)
                .firebaseUid(firebaseUid)
                .email("old@example.com")
                .name("Old Name")
                .provider("EMAIL")
                .build();
        
        when(userRepository.findByFirebaseUid(firebaseUid)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);
        
        // Act
        AuthResponse response = authService.authenticateWithFirebase(idToken);
        
        // Assert
        assertNotNull(response);
        assertEquals(email, response.getUser().getEmail());
        assertEquals(name, response.getUser().getName());
        
        verify(userRepository, times(1)).save(any(User.class));
    }
    
    @Test
    void authenticateWithFirebase_InvalidToken_ThrowsException() throws FirebaseAuthException {
        // Arrange
        String idToken = "invalid-token";
        doThrow(new RuntimeException("Invalid token")).when(firebaseAuthService).verifyToken(idToken);
        // Act & Assert
    assertThrows(RuntimeException.class, () -> {
        authService.authenticateWithFirebase(idToken);
    });
        
        verify(userRepository, never()).save(any(User.class));
    }
}
