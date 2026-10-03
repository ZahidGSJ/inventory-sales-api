package com.zahid.inventoryapi.inventory;

import com.zahid.inventoryapi.product.Product;
import com.zahid.inventoryapi.product.ProductNotFoundException;
import com.zahid.inventoryapi.product.ProductRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryMovementService {

    private final InventoryMovementRepository movementRepository;
    private final ProductRepository productRepository;

    public InventoryMovementService(InventoryMovementRepository movementRepository,
            ProductRepository productRepository) {
        this.movementRepository = movementRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public InventoryMovementResponse registerMovement(InventoryMovementRequest request) {
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ProductNotFoundException(
                        "Producto no encontrado con id: " + request.productId()));

        validateQuantity(request.type(), request.quantity());

        int newStock = request.type().applyTo(product.getStock(), request.quantity());

        if (newStock < 0) {
            throw new InsufficientStockException(
                    "Stock insuficiente: el movimiento dejaría el stock en " + newStock);
        }

        InventoryMovement movement = new InventoryMovement();
        movement.setProduct(product);
        movement.setType(request.type());
        movement.setQuantity(request.quantity());
        movement.setReason(request.reason());

        product.setStock(newStock);

        InventoryMovement savedMovement = movementRepository.save(movement);

        return InventoryMovementResponse.from(savedMovement);
    }

    private void validateQuantity(MovementType type, Integer quantity) {
        switch (type) {
            case ENTRADA, SALIDA -> {
                if (quantity <= 0) {
                    throw new InvalidMovementQuantityException(
                            type + " requiere una cantidad positiva, se recibió: " + quantity);
                }
            }
            case AJUSTE -> {
                if (quantity == 0) {
                    throw new InvalidMovementQuantityException(
                            "AJUSTE no puede tener cantidad igual a 0");
                }
            }
        }
    }

    public List<InventoryMovementResponse> getMovementsByProduct(Long productId) {
        productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(
                        "Producto no encontrado con id: " + productId));

        return movementRepository.findByProductId(productId)
                .stream()
                .map(InventoryMovementResponse::from)
                .toList();
    }
}