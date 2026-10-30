package com.zahid.inventoryapi.inventory;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/inventory-movements")
@Tag(name = "Inventario", description = "Movimientos manuales de inventario y su historial por producto.")
public class InventoryMovementController {

    private final InventoryMovementService movementService;

    public InventoryMovementController(InventoryMovementService movementService) {
        this.movementService = movementService;
    }

    @PostMapping
    @Operation(summary = "Registrar un movimiento de inventario", description = """
            Registra una entrada, una salida o un ajuste y actualiza el stock del producto
            en una sola transacción: si algo falla, no queda ningún cambio.

            - ENTRADA: suma la cantidad, que debe ser positiva.
            - SALIDA: resta la cantidad, que debe ser positiva.
            - AJUSTE: suma la cantidad con su signo; puede ser negativa, pero nunca cero.

            El stock nunca puede quedar por debajo de cero y el motivo es obligatorio.
            Las ventas generan sus propias salidas de forma automática: este endpoint
            es para movimientos manuales, como entradas por compra o ajustes de inventario.
            """)
    @ApiResponse(responseCode = "201", description = "Movimiento registrado y stock actualizado.")
    @ApiResponse(responseCode = "400", description = "La cantidad no es válida para el tipo de movimiento (por ejemplo, una entrada con cantidad cero).")
    @ApiResponse(responseCode = "404", description = "No existe un producto con ese identificador.")
    @ApiResponse(responseCode = "409", description = "El movimiento dejaría el stock por debajo de cero.")
    public ResponseEntity<InventoryMovementResponse> registerMovement(
            @Valid @RequestBody InventoryMovementRequest request) {

        InventoryMovementResponse response = movementService.registerMovement(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/product/{productId}")
    @Operation(summary = "Consultar el historial de un producto", description = """
            Devuelve los movimientos registrados para el producto, incluidas las salidas
            generadas por ventas, del más reciente al más antiguo. Si el producto existe
            pero no tiene movimientos, devuelve una lista vacía.
            """)
    @ApiResponse(responseCode = "404", description = "No existe un producto con ese identificador.")
    public ResponseEntity<List<InventoryMovementResponse>> getMovementsByProduct(
            @PathVariable Long productId) {

        List<InventoryMovementResponse> movements = movementService.getMovementsByProduct(productId);

        return ResponseEntity.ok(movements);
    }
}