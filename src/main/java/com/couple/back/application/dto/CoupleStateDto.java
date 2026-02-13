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
public class CoupleStateDto {
    private Long coupleId;
    private String coupleName;
    private boolean hasPartner;
    private LocalDateTime createdAt;
}
