package com.example.bankcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "Стандартный формат ответа об ошибке")
@Getter
@Setter
@AllArgsConstructor
public class ErrorResponse {

    @Schema(
            description = "HTTP статус код ошибки",
            example = "400",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private int status;

    @Schema(
            description = "Тип ошибки",
            example = "Bad Request",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String error;

    @Schema(
            description = "Детали ошибки (может быть строкой или объектом)",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Object message;
}
