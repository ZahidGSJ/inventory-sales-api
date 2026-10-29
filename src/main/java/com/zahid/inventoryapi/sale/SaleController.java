package com.zahid.inventoryapi.sale;

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
@RequestMapping("/api/sales")
@Tag(name = "Ventas", description = "Registro y consulta de ventas.")
public class SaleController {

    private final SaleService saleService;

    public SaleController(SaleService saleService) {
        this.saleService = saleService;
    }

    @PostMapping
    @Operation(summary = "Registrar una venta", description = """
            Registra una venta con una o más líneas (producto y cantidad).

            - El precio de cada línea es el precio vigente del producto en ese momento y queda
              guardado: no cambia aunque el producto se modifique después.
            - El total lo calcula el servidor.
            - El stock se descuenta mediante salidas de inventario, todo en una sola transacción:
              si algo falla, no queda ningún cambio.
            - Si un mismo producto aparece en varias líneas, el stock se valida contra la suma
              de sus cantidades.
            - No se pueden vender productos inactivos.
            """)
    @ApiResponse(responseCode = "201", description = "Venta registrada y stock descontado.")
    @ApiResponse(responseCode = "404", description = "Alguno de los productos no existe.")
    @ApiResponse(responseCode = "409", description = "Algún producto está inactivo o no tiene stock suficiente. Si faltan existencias de varios productos, el mensaje los lista todos.")
    public ResponseEntity<SaleResponse> createSale(@Valid @RequestBody SaleRequest request) {
        SaleResponse response = saleService.createSale(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar ventas", description = "Devuelve todas las ventas con sus líneas, de la más reciente a la más antigua.")
    public ResponseEntity<List<SaleResponse>> getAllSales() {
        return ResponseEntity.ok(saleService.getAllSales());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar una venta", description = "Busca una venta y sus líneas por su identificador.")
    @ApiResponse(responseCode = "404", description = "No existe una venta con ese identificador.")
    public ResponseEntity<SaleResponse> getSaleById(@PathVariable Long id) {
        return ResponseEntity.ok(saleService.getSaleById(id));
    }
}