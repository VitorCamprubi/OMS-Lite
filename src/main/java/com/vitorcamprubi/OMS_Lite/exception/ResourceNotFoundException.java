package com.vitorcamprubi.OMS_Lite.exception;

/**
 * Lançada quando um recurso solicitado (cliente, produto, pedido) não é encontrado.
 * Mapeada para HTTP 404 pelo {@link GlobalExceptionHandler}.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public static ResourceNotFoundException customer(Long id) {
        return new ResourceNotFoundException("Cliente não encontrado: id=" + id);
    }

    public static ResourceNotFoundException product(Long id) {
        return new ResourceNotFoundException("Produto não encontrado: id=" + id);
    }

    public static ResourceNotFoundException order(Long id) {
        return new ResourceNotFoundException("Pedido não encontrado: id=" + id);
    }
}
