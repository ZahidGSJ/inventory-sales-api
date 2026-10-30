package com.zahid.inventoryapi.category;

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
@RequestMapping("/api/categories")
@Tag(name = "Categorías", description = "Gestión de las categorías que clasifican los productos.")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    @Operation(summary = "Crear una categoría", description = "Registra una categoría con un nombre único.")
    @ApiResponse(responseCode = "201", description = "Categoría creada.")
    @ApiResponse(responseCode = "409", description = "Ya existe una categoría con ese nombre, sin distinguir mayúsculas.")
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CategoryRequest request) {
        CategoryResponse response = categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar categorías", description = "Devuelve todas las categorías ordenadas por nombre.")
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar una categoría", description = "Busca una categoría por su identificador.")
    @ApiResponse(responseCode = "404", description = "No existe una categoría con ese identificador.")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar una categoría", description = """
            Actualiza el nombre y la descripción de la categoría.
            Se puede conservar el nombre actual y cambiar solo la descripción.
            """)
    @ApiResponse(responseCode = "404", description = "No existe una categoría con ese identificador.")
    @ApiResponse(responseCode = "409", description = "Ya existe otra categoría con ese nombre.")
    public ResponseEntity<CategoryResponse> updateCategory(@PathVariable Long id,
            @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(categoryService.updateCategory(id, request));
    }
}