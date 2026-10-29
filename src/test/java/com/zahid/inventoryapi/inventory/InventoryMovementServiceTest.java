package com.zahid.inventoryapi.inventory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.zahid.inventoryapi.product.Product;
import com.zahid.inventoryapi.product.ProductNotFoundException;
import com.zahid.inventoryapi.product.ProductRepository;

@ExtendWith(MockitoExtension.class)
class InventoryMovementServiceTest {

        @Mock
        private InventoryMovementRepository movementRepository;

        @Mock
        private ProductRepository productRepository;

        @InjectMocks
        private InventoryMovementService service;

        private Product productWithStock(int stock) {
                Product product = new Product();
                product.setId(1L);
                product.setName("Laptop Lenovo");
                product.setStock(stock);
                return product;
        }

        @Test
        void entradaValida_aumentaElStockYGuardaElMovimiento() {
                Product product = productWithStock(10);
                when(productRepository.findById(1L)).thenReturn(Optional.of(product));
                when(movementRepository.save(any(InventoryMovement.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                InventoryMovementResponse response = service.registerMovement(
                                new InventoryMovementRequest(1L, MovementType.ENTRADA, 5, "Compra"));

                assertThat(product.getStock()).isEqualTo(15);
                assertThat(response.quantity()).isEqualTo(5);

                ArgumentCaptor<InventoryMovement> captor = ArgumentCaptor.forClass(InventoryMovement.class);
                verify(movementRepository).save(captor.capture());
                assertThat(captor.getValue().getType()).isEqualTo(MovementType.ENTRADA);
                assertThat(captor.getValue().getReason()).isEqualTo("Compra");
        }

        @Test
        void ajusteNegativoQueDejaStockNegativo_lanzaExcepcionYNoGuardaNada() {
                Product product = productWithStock(5);
                when(productRepository.findById(1L)).thenReturn(Optional.of(product));

                InventoryMovementRequest request = new InventoryMovementRequest(
                                1L,
                                MovementType.AJUSTE,
                                -8,
                                "Corrección de inventario");

                assertThatThrownBy(() -> service.registerMovement(request))
                                .isInstanceOf(InsufficientStockException.class)
                                .hasMessageContaining("-3");

                assertThat(product.getStock()).isEqualTo(5);

                verify(movementRepository, never()).save(any());
        }

        @Test
        void salidaMayorAlStock_lanzaExcepcionYNoGuardaNada() {
                Product product = productWithStock(5);
                when(productRepository.findById(1L)).thenReturn(Optional.of(product));

                assertThatThrownBy(() -> service.registerMovement(
                                new InventoryMovementRequest(1L, MovementType.SALIDA, 8, "Venta")))
                                .isInstanceOf(InsufficientStockException.class)
                                .hasMessageContaining("-3");

                assertThat(product.getStock()).isEqualTo(5);
                verify(movementRepository, never()).save(any());
        }

        @Test
        void salidaExactamenteIgualAlStock_permiteQuedarEnCeroYGuardaMovimiento() {
                Product product = productWithStock(5);
                when(productRepository.findById(1L)).thenReturn(Optional.of(product));
                when(movementRepository.save(any(InventoryMovement.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                InventoryMovementResponse response = service.registerMovement(
                                new InventoryMovementRequest(1L, MovementType.SALIDA, 5, "Venta"));

                assertThat(product.getStock()).isZero();
                assertThat(response.quantity()).isEqualTo(5);

                verify(movementRepository).save(any(InventoryMovement.class));
        }

        @ParameterizedTest(name = "{0} con cantidad {1} es inválido")
        @CsvSource({
                        "ENTRADA, 0",
                        "ENTRADA, -5",
                        "SALIDA, 0",
                        "SALIDA, -1",
                        "AJUSTE, 0"
        })
        void cantidadInvalidaSegunElTipo_lanzaExcepcionYNoGuardaNada(MovementType type, int quantity) {
                when(productRepository.findById(1L)).thenReturn(Optional.of(productWithStock(10)));

                assertThatThrownBy(() -> service.registerMovement(
                                new InventoryMovementRequest(1L, type, quantity, "Prueba")))
                                .isInstanceOf(InvalidMovementQuantityException.class);

                verify(movementRepository, never()).save(any());
        }

        @Test
        void productoInexistente_lanzaProductNotFoundException() {
                when(productRepository.findById(99L)).thenReturn(Optional.empty());

                assertThatThrownBy(() -> service.registerMovement(
                                new InventoryMovementRequest(99L, MovementType.ENTRADA, 1, "Prueba")))
                                .isInstanceOf(ProductNotFoundException.class);

                verifyNoInteractions(movementRepository);
        }
}