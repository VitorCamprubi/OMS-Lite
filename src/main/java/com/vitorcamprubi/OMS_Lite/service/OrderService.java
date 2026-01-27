package com.vitorcamprubi.OMS_Lite.service;

import com.vitorcamprubi.OMS_Lite.domain.Order;
import com.vitorcamprubi.OMS_Lite.domain.OrderItem;
import com.vitorcamprubi.OMS_Lite.domain.OrderStatus;
import com.vitorcamprubi.OMS_Lite.domain.Product;
import com.vitorcamprubi.OMS_Lite.repository.CustomerRepository;
import com.vitorcamprubi.OMS_Lite.repository.OrderRepository;
import com.vitorcamprubi.OMS_Lite.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    public OrderService(OrderRepository orderRepository,
                        CustomerRepository customerRepository,
                        ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
    }

    // Classe simples para representar um item do pedido
    public static class ItemRequest {
        private Long productId;
        private Integer quantity;

        public ItemRequest() {
        }

        public ItemRequest(Long productId, Integer quantity) {
            this.productId = productId;
            this.quantity = quantity;
        }

        public Long getProductId() {
            return productId;
        }

        public void setProductId(Long productId) {
            this.productId = productId;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }
    }

    @Transactional
    public Order createConfirmedOrder(Long customerId, List<ItemRequest> itemsRequest) {

        // 1. Buscar o cliente
        var customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        // 2. Criar o pedido em memória
        var order = new Order();
        order.setCustomer(customer);
        order.setCreatedAt(LocalDateTime.now());
        order.setStatus(OrderStatus.CONFIRMED);
        order.setTotalAmount(BigDecimal.ZERO);

        // 3. Vamos calcular o total aos poucos
        BigDecimal total = BigDecimal.ZERO;

        for (ItemRequest req : itemsRequest) {

            // 4. Buscar o produto
            Product product = productRepository.findById(req.getProductId())
                    .orElseThrow(() ->
                            new RuntimeException("Produto não encontrado: " + req.getProductId()));

            // 5. Verificar estoque
            if (product.getStockQuantity() < req.getQuantity()) {
                throw new RuntimeException(
                        "Estoque insuficiente para o produto id " + product.getId()
                );
            }

            // 6. Baixar estoque
            product.setStockQuantity(product.getStockQuantity() - req.getQuantity());

            // 7. Calcular total da linha
            BigDecimal lineTotal = product.getUnitPrice()
                    .multiply(BigDecimal.valueOf(req.getQuantity()));

            // 8. Criar OrderItem e associar ao pedido
            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setQuantity(req.getQuantity());
            orderItem.setUnitPrice(product.getUnitPrice());
            orderItem.setTotalPrice(lineTotal);

            order.addItem(orderItem);

            // 9. Somar no total geral
            total = total.add(lineTotal);
        }

        // 10. Seta o total calculado
        order.setTotalAmount(total);

        // 11. Salva o pedido (pedido + itens, por causa do cascade)
        return orderRepository.save(order);
    }
}
