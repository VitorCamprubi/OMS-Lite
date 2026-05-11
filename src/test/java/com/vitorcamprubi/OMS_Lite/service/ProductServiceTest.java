package com.vitorcamprubi.OMS_Lite.service;

import com.vitorcamprubi.OMS_Lite.domain.Product;
import com.vitorcamprubi.OMS_Lite.dto.product.CreateProductRequest;
import com.vitorcamprubi.OMS_Lite.exception.BusinessRuleException;
import com.vitorcamprubi.OMS_Lite.exception.ResourceNotFoundException;
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
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void shouldCreateProduct_whenRequestIsValid() {
        var request = new CreateProductRequest("Teclado", "TEC-001", new BigDecimal("250.00"), 10);
        when(productRepository.existsBySku("TEC-001")).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        Product saved = productService.create(request);

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(captor.capture());

        assertThat(captor.getValue().getName()).isEqualTo("Teclado");
        assertThat(captor.getValue().getSku()).isEqualTo("TEC-001");
        assertThat(captor.getValue().getUnitPrice()).isEqualByComparingTo("250.00");
        assertThat(captor.getValue().getStockQuantity()).isEqualTo(10);
        assertThat(saved).isNotNull();
    }

    @Test
    void shouldThrowBusinessRule_whenCreateWithDuplicateSku() {
        var request = new CreateProductRequest("Teclado", "DUP-1", new BigDecimal("100.00"), 5);
        when(productRepository.existsBySku("DUP-1")).thenReturn(true);

        assertThatThrownBy(() -> productService.create(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("sku")
                .hasMessageContaining("DUP-1");

        verify(productRepository, never()).save(any());
    }

    @Test
    void shouldReturnAllProducts_whenFindAll() {
        Product a = new Product();
        a.setName("A");
        Product b = new Product();
        b.setName("B");
        when(productRepository.findAll()).thenReturn(List.of(a, b));

        List<Product> result = productService.findAll();

        assertThat(result).hasSize(2);
    }

    @Test
    void shouldReturnProduct_whenFindByIdExists() {
        Product p = new Product();
        p.setName("Mouse");
        when(productRepository.findById(3L)).thenReturn(Optional.of(p));

        Product found = productService.findById(3L);

        assertThat(found.getName()).isEqualTo("Mouse");
    }

    @Test
    void shouldThrowResourceNotFound_whenFindByIdNotExists() {
        when(productRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.findById(404L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("404");
    }
}
