package com.mksafenet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDto {
    private String type;   // "bot", "system", "consequence", "success"
    private String text;
    private int delayMs;
    private String icon;   // optional emoji/icon hint
}
