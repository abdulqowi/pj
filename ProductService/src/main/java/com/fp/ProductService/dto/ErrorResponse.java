package com.fp.ProductService.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class ErrorResponse {
    private int status;
    private String error;
    private String message;
}
