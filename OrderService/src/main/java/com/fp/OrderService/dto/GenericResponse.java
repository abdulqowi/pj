package com.fp.OrderService.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class GenericResponse<T> {
    private String status;
    private String code;
    private String message;
    private T data;
}
