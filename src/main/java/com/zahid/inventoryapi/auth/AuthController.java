package com.zahid.inventoryapi.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Inicio de sesión y obtención del token de acceso.")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @SecurityRequirements
    @Operation(summary = "Iniciar sesión", description = """
            Verifica el email y la contraseña y devuelve un token JWT de acceso.
            La respuesta indica cuánto tarda en caducar (expiresInSeconds).
            Para usarlo, envíalo en la cabecera Authorization con el formato: Bearer <token>.
            """)
    @ApiResponse(responseCode = "401", description = """
            Credenciales inválidas. El mensaje es el mismo si el email no existe,
            si la contraseña es incorrecta o si la cuenta está desactivada.
            """)
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}