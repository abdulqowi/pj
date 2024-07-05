package com.pja.common.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class ApiResponse {
    private String status;
    private String code;
    private String message;
    private String data;
}