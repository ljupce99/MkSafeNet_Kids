package com.mksafenet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String role;
    private String username;
    private String displayName;
    private Long schoolId;
    private String schoolName;
}
