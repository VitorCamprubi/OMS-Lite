package com.vitorcamprubi.OMS_Lite.exception;

/**
 * Lançada quando uma regra de negócio é violada (ex.: estoque insuficiente,
 * pedido sem itens, e-mail duplicado). Mapeada para HTTP 409 pelo
 * {@link GlobalExceptionHandler}.
 */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }

    public static BusinessRuleException insufficientStock(Long productId, int requested, int available) {
        return new BusinessRuleException(
                "Estoque insuficiente para o produto id=" + productId
                        + " (solicitado=" + requested + ", disponível=" + available + ")"
        );
    }

    public static BusinessRuleException emptyOrder() {
        return new BusinessRuleException("Pedido deve conter ao menos 1 item.");
    }

    public static BusinessRuleException duplicate(String field, String value) {
        return new BusinessRuleException("Já existe registro com " + field + "=" + value);
    }
}
