package com.vitorcamprubi.OMS_Lite.service;

import com.vitorcamprubi.OMS_Lite.domain.Customer;
import com.vitorcamprubi.OMS_Lite.domain.Order;
import com.vitorcamprubi.OMS_Lite.domain.OrderItem;
import com.vitorcamprubi.OMS_Lite.domain.OrderStatus;
import com.vitorcamprubi.OMS_Lite.domain.Product;
import com.vitorcamprubi.OMS_Lite.dto.order.CreateOrderRequest;
import com.vitorcamprubi.OMS_Lite.exception.BusinessRuleException;
import com.vitorcamprubi.OMS_Lite.exception.ResourceNotFoundException;
import com.vitorcamprubi.OMS_Lite.repository.CustomerRepository;
import com.vitorcamprubi.OMS_Lite.repository.OrderRepository;
import com.vitorcamprubi.OMS_Lite.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private OrderService orderService;

    private Customer customer(long id) {
        Customer c = new Customer();
        c.setId(id);
        c.setName("Cliente " + id);
        c.setEmail("c" + id + "@example.com");
        c.setDocument("doc-" + id);
        return c;
    }

    private Product product(long id, String price, int stock) {
        Product p = new Product();
        p.setId(id);
        p.setName("P-" + id);
        p.setSku("SKU-" + id);
        p.setUnitPrice(new BigDecimal(price));
        p.setStockQuantity(stock);
        return p;
    }

    @Test
    void shouldCreateConfirmedOrder_whenRequestIsValid() {
        Customer c = customer(1L);
        Product p1 = product(10L, "100.00", 50);
        Product p2 = product(20L, "25.50", 30);

        when(customerRepository.findById(1L)).thenReturn(Optional.of(c));
        when(productRepository.findAllById(List.of(10L, 20L))).thenReturn(List.of(p1, p2));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        var items = List.of(
                new CreateOrderRequest.ItemRequest(10L, 2),
                new CreateOrderRequest.ItemRequest(20L, 4)
        );

        Order order = orderService.createConfirmedOrder(1L, items);

        assertThat(order.getCustomer()).isSameAs(c);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(order.getCreatedAt()).isNotNull();
        // total = 2*100.00 + 4*25.50 = 200.00 + 102.00 = 302.00
        assertThat(order.getTotalAmount()).isEqualByComparingTo("302.00");
        assertThat(order.getOrderItems()).hasSize(2);

        // Validates stock decrement
        assertThat(p1.getStockQuantity()).isEqualTo(48);
        assertThat(p2.getStockQuantity()).isEqualTo(26);

        // Validates each line total
        OrderItem line1 = order.getOrderItems().get(0);
        assertThat(line1.getQuantity()).isEqualTo(2);
        assertThat(line1.getUnitPrice()).isEqualByComparingTo("100.00");
        assertThat(line1.getTotalPrice()).isEqualByComparingTo("200.00");
    }

    @Test
    void shouldThrowBusinessRule_whenItemsListIsEmpty() {
        assertThatThrownBy(() -> orderService.createConfirmedOrder(1L, List.of()))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("ao menos 1 item");

        verify(customerRepository, never()).findById(any());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void shouldThrowBusinessRule_whenItemsListIsNull() {
        assertThatThrownBy(() -> orderService.createConfirmedOrder(1L, null))
                .isInstanceOf(BusinessRuleException.class);

        verify(orderRepository, never()).save(any());
    }

    @Test
    void shouldThrowResourceNotFound_whenCustomerDoesNotExist() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        var items = List.of(new CreateOrderRequest.ItemRequest(1L, 1));

        assertThatThrownBy(() -> orderService.createConfirmedOrder(99L, items))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Cliente")
                .hasMessageContaining("99");

        verify(orderRepository, never()).save(any());
    }

    @Test
    void shouldThrowResourceNotFound_whenAnyProductDoesNotExist() {
        Customer c = customer(1L);
        Product p1 = product(10L, "10.00", 100);

        when(customerRepository.findById(1L)).thenReturn(Optional.of(c));
        // ask for productIds 10 and 99, but repo only returns 10
        when(productRepository.findAllById(List.of(10L, 99L))).thenReturn(List.of(p1));

        var items = List.of(
                new CreateOrderRequest.ItemRequest(10L, 1),
                new CreateOrderRequest.ItemRequest(99L, 1)
        );

        assertThatThrownBy(() -> orderService.createConfirmedOrder(1L, items))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Produto")
                .hasMessageContaining("99");

        verify(orderRepository, never()).save(any());
    }

    @Test
    void shouldThrowBusinessRule_whenStockIsInsufficient() {
        Customer c = customer(1L);
        Product p1 = product(10L, "10.00", 2); // only 2 in stock

        when(customerRepository.findById(1L)).thenReturn(Optional.of(c));
        when(productRepository.findAllById(List.of(10L))).thenReturn(List.of(p1));

        var items = List.of(new CreateOrderRequest.ItemRequest(10L, 5));

        assertThatThrownBy(() -> orderService.createConfirmedOrder(1L, items))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Estoque insuficiente");

        verify(orderRepository, never()).save(any());
    }

    @Test
    void shouldConsolidateQuantities_whenSameProductIdAppearsMultipleTimes() {
        Customer c = customer(1L);
        Product p1 = product(10L, "5.00", 100);

        when(customerRepository.findById(1L)).thenReturn(Optional.of(c));
        when(productRepository.findAllById(List.of(10L))).thenReturn(List.of(p1));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        var items = List.of(
                new CreateOrderRequest.ItemRequest(10L, 2),
                new CreateOrderRequest.ItemRequest(10L, 3)
        );

        Order order = orderService.createConfirmedOrder(1L, items);

        // Single consolidated item with quantity 5
        assertThat(order.getOrderItems()).hasSize(1);
        assertThat(order.getOrderItems().get(0).getQuantity()).isEqualTo(5);
        assertThat(order.getOrderItems().get(0).getTotalPrice()).isEqualByComparingTo("25.00");
        assertThat(order.getTotalAmount()).isEqualByComparingTo("25.00");
        assertThat(p1.getStockQuantity()).isEqualTo(95);
    }

    @Test
    void shouldThrowIllegalArgument_whenItemHasNullProductId() {
        var items = List.of(new CreateOrderRequest.ItemRequest(null, 1));

        // customerRepository must answer because empty/null check passes
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer(1L)));

        assertThatThrownBy(() -> orderService.createConfirmedOrder(1L, items))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Item inválido");

        verify(orderRepository, never()).save(any());
    }

    @Test
    void shouldReturnOrder_whenFindByIdExists() {
        Order o = new Order();
        when(orderRepository.findById(5L)).thenReturn(Optional.of(o));

        Order found = orderService.findById(5L);

        assertThat(found).isSameAs(o);
    }

    @Test
    void shouldThrowResourceNotFound_whenFindByIdNotExists() {
        when(orderRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.findById(404L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Pedido")
                .hasMessageContaining("404");
    }

    @Test
    void shouldPersistOrder_whenSaveSucceeds() {
        Customer c = customer(1L);
        Product p1 = product(10L, "10.00", 5);

        when(customerRepository.findById(1L)).thenReturn(Optional.of(c));
        when(productRepository.findAllById(List.of(10L))).thenReturn(List.of(p1));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        var items = List.of(new CreateOrderRequest.ItemRequest(10L, 1));

        orderService.createConfirmedOrder(1L, items);

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }
}
