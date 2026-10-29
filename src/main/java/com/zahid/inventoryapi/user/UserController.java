package com.zahid.inventoryapi.user;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
@RequestMapping("/api/users")
@Tag(name = "Usuarios", description = "Gestión de las cuentas de acceso. No existe registro público: solo un administrador crea usuarios.")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @Operation(summary = "Crear un usuario", description = """
            Crea una cuenta activa con el rol indicado (ADMIN o VENDEDOR).
            La contraseña inicial la define el administrador (mínimo 8 caracteres);
            se guarda cifrada y nunca se devuelve en ninguna respuesta.
            El email se normaliza a minúsculas y no se puede cambiar después.
            """)
    @ApiResponse(responseCode = "201", description = "Usuario creado.")
    @ApiResponse(responseCode = "404", description = "El rol indicado no existe.")
    @ApiResponse(responseCode = "409", description = "Ya existe un usuario con ese email.")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(request));
    }

    @GetMapping
    @Operation(summary = "Listar usuarios", description = "Devuelve todos los usuarios, activos e inactivos.")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar un usuario", description = "Busca un usuario por su identificador.")
    @ApiResponse(responseCode = "404", description = "No existe un usuario con ese identificador.")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un usuario", description = """
            Actualiza el nombre, el rol y el estado activo de un usuario. El email no se puede modificar.
            Un administrador no puede desactivar su propia cuenta ni cambiar su propio rol,
            y siempre debe quedar al menos un administrador activo.
            Desactivar a un usuario corta su acceso de inmediato, aunque su token no haya caducado.
            """)
    @ApiResponse(responseCode = "404", description = "No existe el usuario o el rol indicado.")
    @ApiResponse(responseCode = "409", description = "La operación dejaría el sistema sin un administrador activo, o el administrador intenta modificar su propia cuenta.")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(userService.updateUser(id, request, authentication.getName()));
    }

    @PutMapping("/{id}/password")
    @Operation(summary = "Restablecer la contraseña de un usuario", description = """
            El administrador define una contraseña nueva (mínimo 8 caracteres) sin necesidad de conocer la anterior.
            La contraseña anterior deja de funcionar. Los tokens ya emitidos siguen siendo válidos hasta que caduquen.
            """)
    @ApiResponse(responseCode = "204", description = "Contraseña restablecida.")
    @ApiResponse(responseCode = "404", description = "No existe un usuario con ese identificador.")
    public ResponseEntity<Void> resetPassword(@PathVariable Long id,
            @Valid @RequestBody PasswordResetRequest request) {
        userService.resetPassword(id, request);
        return ResponseEntity.noContent().build();
    }
}