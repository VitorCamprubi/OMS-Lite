package com.vitorcamprubi.OMS_Lite.service;

import com.vitorcamprubi.OMS_Lite.domain.Order;
import com.vitorcamprubi.OMS_Lite.domain.OrderItem;
import com.vitorcamprubi.OMS_Lite.domain.OrderStatus;
import com.vitorcamprubi.OMS_Lite.domain.Product;
import com.vitorcamprubi.OMS_Lite.dto.order.CreateOrderRequest;
import com.vitorcamprubi.OMS_Lite.repository.CustomerRepository;
import com.vitorcamprubi.OMS_Lite.repository.OrderRepository;
import com.vitorcamprubi.OMS_Lite.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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

    @Transactional
    public Order createConfirmedOrder(Long customerId, List<CreateOrderRequest.ItemRequest> itemsRequest) {

        // Regra de negócio: pedido precisa ter pelo menos 1 item
        if (itemsRequest == null || itemsRequest.isEmpty()) {
            throw new IllegalArgumentException("Pedido deve conter ao menos 1 item.");
        }

        // 1) Buscar cliente
        var customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        // 2) Consolidar itens duplicados (mesmo productId repetido)
        // Ex: [{id=1,q=2},{id=1,q=3}] => {1 => 5}
        Map<Long, Integer> qtyByProductId = new LinkedHashMap<>();
        for (CreateOrderRequest.ItemRequest req : itemsRequest) {
            if (req == null || req.productId() == null || req.quantity() == null) {
                throw new IllegalArgumentException("Item inválido no pedido.");
            }
            qtyByProductId.merge(req.productId(), req.quantity(), Integer::sum);
        }

        // 3) Buscar todos os produtos em lote
        List<Long> productIds = new ArrayList<>(qtyByProductId.keySet());
        List<Product> products = new ArrayList<>();
        productRepository.findAllById(productIds).forEach(products::add);

        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        // 4) Validar se todos os productsId existem
        for (Long productId : productIds) {
            if (!productMap.containsKey(productId)) {
                throw new RuntimeException("Produto não encontrado: " + productId);
            }
        }

        // 5) Criar pedido em memória
        var order = new Order();
        order.setCustomer(customer);
        order.setCreatedAt(LocalDateTime.now());
        order.setStatus(OrderStatus.CONFIRMED);
        order.setTotalAmount(BigDecimal.ZERO);

        BigDecimal total = BigDecimal.ZERO;

        // 6) Validar estoque, baixar e criar itens
        for (Map.Entry<Long, Integer> entry : qtyByProductId.entrySet()) {
            Long productId = entry.getKey();
            Integer quantity = entry.getValue();

            Product product = productMap.get(productId);

            Integer stock = product.getStockQuantity();
            if (stock == null) stock = 0;

            if (stock < quantity) {
                throw new RuntimeException("Estoque insuficiente para o produto id " + product.getId());
            }

            // baixa estoque
            product.setStockQuantity(stock - quantity);

            // calcula total da linha
            BigDecimal unitPrice = product.getUnitPrice();
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(quantity));

            // cria OrderItem
            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setQuantity(quantity);
            orderItem.setUnitPrice(unitPrice);
            orderItem.setTotalPrice(lineTotal);

            order.addItem(orderItem);

            total = total.add(lineTotal);
        }

        // 7) Total geral
        order.setTotalAmount(total);

        // 8) Salvar (cascade salva itens)
        return orderRepository.save(order);
    }
}
