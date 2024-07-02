package com.fp.TransService.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class GenericResponse<T> {
    private String status;
    private String code;
    private String message;
    private T data;
}