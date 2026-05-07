package com.mksafenet.dto;

import lombok.Data;

@Data
public class ChatRespondRequest {
    private String studentId;
    private String answer;
}
