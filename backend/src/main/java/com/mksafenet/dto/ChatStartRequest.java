package com.mksafenet.dto;

import lombok.Data;

@Data
public class ChatStartRequest {
    private String sessionToken;
    private String studentName;
}
