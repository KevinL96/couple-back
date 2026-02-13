package com.couple.back.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDto {
    private Long id;
    private String firebaseUid;
    private String email;
    private String name;
    private String photoUrl;
    private String provider;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
