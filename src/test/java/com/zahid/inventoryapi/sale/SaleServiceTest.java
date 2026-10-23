package com.zahid.inventoryapi.sale;

import com.zahid.inventoryapi.inventory.InsufficientStockException;
import com.zahid.inventoryapi.inventory.InventoryMovementRequest;
import com.zahid.inventoryapi.inventory.InventoryMovementService;
import com.zahid.inventoryapi.inventory.MovementType;
import com.zahid.inventoryapi.product.InactiveProductException;
import com.zahid.inventoryapi.product.Product;
import com.zahid.inventoryapi.product.ProductNotFoundException;
import com.zahid.inventoryapi.product.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SaleServiceTest {

    @Mock
    private SaleRepository saleRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private InventoryMovementService inventoryMovementService;

    @InjectMocks
    private SaleService service;

    private Product product(Long id, String name, String price, int stock, boolean active) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        product.setPrice(new BigDecimal(price));
        product.setStock(stock);
        product.setActive(active);
        return product;
    }

    private SaleRequest saleOf(SaleItemRequest... items) {
        return new SaleRequest(List.of(items));
    }

    @Test
    void ventaValida_calculaElTotalConElPrecioActualYRegistraUnaSalidaPorProducto() {
        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product(1L, "Laptop Lenovo", "15000.00", 10, true)));
        when(productRepository.findById(2L))
                .thenReturn(Optional.of(product(2L, "Monitor LG", "3500.00", 20, true)));
        when(saleRepository.save(any(Sale.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SaleResponse response = service.createSale(saleOf(
                new SaleItemRequest(1L, 2),
                new SaleItemRequest(2L, 1)));

        assertThat(response.total()).isEqualByComparingTo("33500.00");
        assertThat(response.items()).hasSize(2);
        assertThat(response.items().get(0).unitPrice()).isEqualByComparingTo("15000.00");

        verify(inventoryMovementService).registerMovement(
                new InventoryMovementRequest(1L, MovementType.SALIDA, 2, "Venta"));
        verify(inventoryMovementService).registerMovement(
                new InventoryMovementRequest(2L, MovementType.SALIDA, 1, "Venta"));
    }

    @Test
    void mismoProductoEnDosLineas_guardaDosItemsPeroRegistraUnaSolaSalidaConLaSuma() {
        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product(1L, "Laptop Lenovo", "15000.00", 10, true)));
        when(saleRepository.save(any(Sale.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SaleResponse response = service.createSale(saleOf(
                new SaleItemRequest(1L, 3),
                new SaleItemRequest(1L, 3)));

        assertThat(response.items()).hasSize(2);
        assertThat(response.total()).isEqualByComparingTo("90000.00");

        verify(inventoryMovementService, times(1)).registerMovement(any());
        verify(inventoryMovementService).registerMovement(
                new InventoryMovementRequest(1L, MovementType.SALIDA, 6, "Venta"));
    }

    @Test
    void mismoProductoRepetido_validaElStockContraLaSumaYNoContraCadaLinea() {
        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product(1L, "Laptop Lenovo", "15000.00", 8, true)));

        assertThatThrownBy(() -> service.createSale(saleOf(
                new SaleItemRequest(1L, 5),
                new SaleItemRequest(1L, 5))))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("disponible: 8, solicitado: 10");

        verifyNoInteractions(inventoryMovementService);
        verify(saleRepository, never()).save(any());
    }

    @Test
    void variosProductosSinStock_elMensajeListaTodosLosFaltantes() {
        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product(1L, "Laptop Lenovo", "15000.00", 8, true)));
        when(productRepository.findById(2L))
                .thenReturn(Optional.of(product(2L, "Monitor LG", "3500.00", 0, true)));

        assertThatThrownBy(() -> service.createSale(saleOf(
                new SaleItemRequest(1L, 50),
                new SaleItemRequest(2L, 20))))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Laptop Lenovo")
                .hasMessageContaining("Monitor LG");

        verifyNoInteractions(inventoryMovementService);
    }

    @Test
    void productoInactivo_lanzaExcepcionYNoRegistraNingunaSalida() {
        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product(1L, "Laptop Lenovo", "15000.00", 10, false)));

        assertThatThrownBy(() -> service.createSale(saleOf(new SaleItemRequest(1L, 1))))
                .isInstanceOf(InactiveProductException.class)
                .hasMessageContaining("Laptop Lenovo");

        verifyNoInteractions(inventoryMovementService);
        verify(saleRepository, never()).save(any());
    }

    @Test
    void productoInexistente_lanzaProductNotFoundException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createSale(saleOf(new SaleItemRequest(99L, 1))))
                .isInstanceOf(ProductNotFoundException.class);

        verifyNoInteractions(inventoryMovementService);
    }

    @Test
    void segundoProductoInexistente_noRegistraMovimientosNiGuardaLaVenta() {
        when(productRepository.findById(1L))
                .thenReturn(Optional.of(
                        product(1L, "Laptop Lenovo", "15000.00", 10, true)));

        when(productRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createSale(saleOf(
                new SaleItemRequest(1L, 2),
                new SaleItemRequest(99L, 1))))
                .isInstanceOf(ProductNotFoundException.class);

        verifyNoInteractions(inventoryMovementService);

        verify(saleRepository, never()).save(any(Sale.class));
    }
}