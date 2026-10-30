package com.zahid.inventoryapi.product;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Productos", description = "Gestión del catálogo de productos.")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    @Operation(summary = "Crear un producto", description = """
            Registra un producto con un SKU único y una categoría existente.
            El stock inicial es cero y el producto se crea activo: el stock se gestiona
            únicamente mediante movimientos de inventario.
            """)
    @ApiResponse(responseCode = "201", description = "Producto creado.")
    @ApiResponse(responseCode = "404", description = "La categoría indicada no existe.")
    @ApiResponse(responseCode = "409", description = "Ya existe un producto con ese SKU, sin distinguir mayúsculas.")
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request) {
        ProductResponse response = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar productos", description = "Devuelve todos los productos registrados, incluidos los inactivos.")
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        List<ProductResponse> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar un producto", description = "Busca un producto por su identificador.")
    @ApiResponse(responseCode = "404", description = "No existe un producto con ese identificador.")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        ProductResponse product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un producto", description = """
            Actualiza los datos editables del producto, incluido su estado activo.
            El SKU no se puede modificar y el stock se gestiona mediante movimientos de inventario.
            """)
    @ApiResponse(responseCode = "404", description = "No existe el producto o la categoría indicada.")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductUpdateRequest request) {
        ProductResponse response = productService.updateProduct(id, request);
        return ResponseEntity.ok(response);
    }
}