package com.fp.OrderService.dto;

import lombok.*;
import lombok.experimental.Accessors;

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
