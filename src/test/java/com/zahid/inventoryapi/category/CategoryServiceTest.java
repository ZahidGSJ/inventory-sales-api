package com.zahid.inventoryapi.category;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService service;

    private Category category(Long id, String name, String description) {
        Category category = new Category();
        category.setId(id);
        category.setName(name);
        category.setDescription(description);
        return category;
    }

    @Test
    void crearCategoriaConNombreDuplicado_lanzaExcepcionYNoGuarda() {
        when(categoryRepository.existsByName("Accesorios")).thenReturn(true);

        assertThatThrownBy(() -> service.createCategory(new CategoryRequest("Accesorios", "x")))
                .isInstanceOf(DuplicateCategoryNameException.class);

        verify(categoryRepository, never()).save(any());
    }

    @Test
    void actualizarCategoria_cambiaNombreYDescripcion() {
        Category existing = category(1L, "Electrónica", "Descripción vieja");
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(categoryRepository.existsByNameAndIdNot("Computadoras", 1L)).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CategoryResponse response = service.updateCategory(1L,
                new CategoryRequest("Computadoras", "Descripción nueva"));

        assertThat(existing.getName()).isEqualTo("Computadoras");
        assertThat(response.description()).isEqualTo("Descripción nueva");
    }

    @Test
    void actualizarCategoria_conElNombreDeOtraCategoria_lanzaExcepcionYNoModificaNada() {
        Category existing = category(1L, "Electrónica", "Descripción");
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(categoryRepository.existsByNameAndIdNot("Accesorios", 1L)).thenReturn(true);

        assertThatThrownBy(() -> service.updateCategory(1L, new CategoryRequest("Accesorios", "Otra")))
                .isInstanceOf(DuplicateCategoryNameException.class);

        assertThat(existing.getName()).isEqualTo("Electrónica");
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void actualizarCategoria_inexistente_lanzaCategoryNotFoundException() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateCategory(99L, new CategoryRequest("Nueva", "x")))
                .isInstanceOf(CategoryNotFoundException.class);

        verify(categoryRepository, never()).save(any());
    }
}