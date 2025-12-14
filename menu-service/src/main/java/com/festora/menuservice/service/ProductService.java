package com.festora.menuservice.service;

import com.festora.menuservice.entity.Product;
import com.festora.menuservice.repository.ProductRepo;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepo repo;

    public ProductService(ProductRepo repo) {
        this.repo = repo;
    }

    public List<Product> getAllProducts() {
        return repo.findAll();
    }

    public Optional<Product> getProductById(Long id) {
        return repo.findById(id);
    }

    public Product addProduct(Product product) {
        return repo.save(product);
    }

    public Product updateProduct(Long id, Product productDetails) {
        return repo.findById(id)
                .map(p -> {
                    p.setName(productDetails.getName());
                    p.setDescription(productDetails.getDescription());
                    p.setPrice(productDetails.getPrice());
                    p.setCategory(productDetails.getCategory());
                    return repo.save(p);
                })
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }

    public void deleteProduct(Long id) {
        repo.deleteById(id);
    }
}

