package com.vitorcamprubi.OMS_Lite.service;

import com.vitorcamprubi.OMS_Lite.domain.Product;
import com.vitorcamprubi.OMS_Lite.dto.product.CreateProductRequest;
import com.vitorcamprubi.OMS_Lite.exception.BusinessRuleException;
import com.vitorcamprubi.OMS_Lite.exception.ResourceNotFoundException;
import com.vitorcamprubi.OMS_Lite.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public Product create(CreateProductRequest request) {
        if (productRepository.existsBySku(request.sku())) {
            throw BusinessRuleException.duplicate("sku", request.sku());
        }

        Product product = new Product();
        product.setName(request.name());
        product.setSku(request.sku());
        product.setUnitPrice(request.unitPrice());
        product.setStockQuantity(request.stockQuantity());
        return productRepository.save(product);
    }

    @Transactional(readOnly = true)
    public List<Product> findAll() {
        return productRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.product(id));
    }
}
