package com.vitorcamprubi.OMS_Lite.exception;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Corpo padrão de erro retornado pela API.
 *
 * @param status      código HTTP
 * @param error       reason phrase / nome curto do erro
 * @param message     mensagem amigável (geralmente a do exception)
 * @param path        URI do request (best-effort)
 * @param violations  detalhamento por campo no caso de erros de validação (pode ser null)
 * @param timestamp   horário do erro em UTC
 */
public record ApiError(
        int status,
        String error,
        String message,
        String path,
        List<FieldViolation> violations,
        OffsetDateTime timestamp
) {
    public record FieldViolation(String field, String message) {}
}
