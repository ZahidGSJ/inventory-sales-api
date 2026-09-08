package com.zahid.inventoryapi.product;

import java.util.List;
import org.springframework.stereotype.Service;
import com.zahid.inventoryapi.category.CategoryNotFoundException;
import com.zahid.inventoryapi.category.CategoryRepository;
import com.zahid.inventoryapi.category.Category;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    public ProductResponse createProduct(ProductRequest request) {
        if (productRepository.existsBySku(request.sku())) {
            throw new DuplicateSkuException("Ya existe un producto con el SKU: " + request.sku());
        }

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new CategoryNotFoundException(
                        "Categoría no encontrada con id: " + request.categoryId()));

        Product product = new Product();
        product.setSku(request.sku());
        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setMinimumStock(request.minimumStock());
        product.setStock(0);
        product.setActive(true);
        product.setCategory(category);

        Product savedProduct = productRepository.save(product);

        return ProductResponse.from(savedProduct);
    }

    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(ProductResponse::from)
                .toList();
    }

    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Producto no encontrado con id: " + id));

        return ProductResponse.from(product);
    }

    public ProductResponse updateProduct(Long id, ProductUpdateRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Producto no encontrado con id: " + id));

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new CategoryNotFoundException(
                        "Categoría no encontrada con id: " + request.categoryId()));

        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setMinimumStock(request.minimumStock());
        product.setActive(request.active());
        product.setCategory(category);

        Product updatedProduct = productRepository.save(product);

        return ProductResponse.from(updatedProduct);
    }
}