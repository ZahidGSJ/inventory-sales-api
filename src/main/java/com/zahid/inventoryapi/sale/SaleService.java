package com.zahid.inventoryapi.sale;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import com.zahid.inventoryapi.inventory.InsufficientStockException;
import com.zahid.inventoryapi.inventory.InventoryMovementRequest;
import com.zahid.inventoryapi.inventory.InventoryMovementService;
import com.zahid.inventoryapi.inventory.MovementType;
import com.zahid.inventoryapi.product.InactiveProductException;
import com.zahid.inventoryapi.product.Product;
import com.zahid.inventoryapi.product.ProductNotFoundException;
import com.zahid.inventoryapi.product.ProductRepository;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SaleService {

    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;
    private final InventoryMovementService inventoryMovementService;

    public SaleService(SaleRepository saleRepository,
            ProductRepository productRepository,
            InventoryMovementService inventoryMovementService) {
        this.saleRepository = saleRepository;
        this.productRepository = productRepository;
        this.inventoryMovementService = inventoryMovementService;
    }

    @Transactional
    public SaleResponse createSale(SaleRequest request) {
        Sale sale = new Sale();
        BigDecimal total = BigDecimal.ZERO;

        Map<Long, Product> products = new HashMap<>();
        Map<Long, Integer> requestedByProduct = new LinkedHashMap<>();

        for (SaleItemRequest itemRequest : request.items()) {
            Product product = productRepository.findById(itemRequest.productId()).orElseThrow(
                    () -> new ProductNotFoundException("Producto no encontrado con id: " + itemRequest.productId()));

            if (!product.getActive()) {
                throw new InactiveProductException(
                        "El producto '" + product.getName() + "' está inactivo y no puede venderse");
            }

            products.put(product.getId(), product);
            requestedByProduct.merge(product.getId(), itemRequest.quantity(), Integer::sum);

            SaleItem item = new SaleItem();
            item.setProduct(product);
            item.setQuantity(itemRequest.quantity());
            item.setUnitPrice(product.getPrice());
            sale.addItem(item);

            total = total.add(item.calculateSubTotal());
        }
        sale.setTotal(total);

        List<String> shortages = new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : requestedByProduct.entrySet()) {
            Product product = products.get(entry.getKey());
            if (product.getStock() < entry.getValue()) {
                shortages.add(product.getName() + " (disponible: " + product.getStock() + ", solicitado: "
                        + entry.getValue() + ")");
            }
        }
        if (!shortages.isEmpty()) {
            throw new InsufficientStockException(
                    "Stock insuficiente para: " + String.join("; ", shortages));
        }

        for (Map.Entry<Long, Integer> entry : requestedByProduct.entrySet()) {
            inventoryMovementService.registerMovement(new InventoryMovementRequest(
                    entry.getKey(),
                    MovementType.SALIDA,
                    entry.getValue(),
                    "Venta"));
        }

        Sale savedSale = saleRepository.save(sale);

        return SaleResponse.from(savedSale);
    }

    @Transactional(readOnly = true)
    public SaleResponse getSaleById(Long id) {
        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new SaleNotFoundException("Venta no encontrada con id: " + id));
        return SaleResponse.from(sale);
    }

    @Transactional(readOnly = true)
    public List<SaleResponse> getAllSales() {
        return saleRepository.findAllWithItems()
                .stream()
                .map(SaleResponse::from)
                .toList();
    }
}
