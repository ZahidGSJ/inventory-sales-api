package com.zahid.inventoryapi.user;

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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService service;

    private Role role(Long id, String name) {
        Role role = new Role();
        role.setId(id);
        role.setName(name);
        return role;
    }

    private User user(Long id, String email, Role role, boolean active) {
        User user = new User();
        user.setId(id);
        user.setName("Usuario " + id);
        user.setEmail(email);
        user.setPasswordHash("hash-viejo");
        user.setRole(role);
        user.setActive(active);
        return user;
    }

    @Test
    void crearUsuario_normalizaElEmailCifraLaContrasenaYLoDejaActivo() {
        Role vendedor = role(2L, "VENDEDOR");
        when(roleRepository.findByName("VENDEDOR")).thenReturn(Optional.of(vendedor));
        when(passwordEncoder.encode("ventas1234")).thenReturn("hash-bcrypt");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = service.createUser(new UserCreateRequest(
                "  Ana Vendedora ", "  Ana@Inventario.LOCAL ", "ventas1234", "VENDEDOR"));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();

        assertThat(saved.getEmail()).isEqualTo("ana@inventario.local");
        assertThat(saved.getName()).isEqualTo("Ana Vendedora");
        assertThat(saved.getPasswordHash()).isEqualTo("hash-bcrypt");
        assertThat(saved.getActive()).isTrue();
        assertThat(response.role()).isEqualTo("VENDEDOR");
    }

    @Test
    void crearUsuarioConEmailDuplicado_lanzaExcepcionYNoGuardaNada() {
        when(userRepository.existsByEmail("ana@inventario.local")).thenReturn(true);

        assertThatThrownBy(() -> service.createUser(new UserCreateRequest(
                "Ana", "ana@inventario.local", "ventas1234", "VENDEDOR")))
                .isInstanceOf(DuplicateEmailException.class);

        verify(userRepository, never()).save(any());
        verifyNoInteractions(passwordEncoder, roleRepository);
    }

    @Test
    void crearUsuarioConRolInexistente_lanzaExcepcionYNoGuardaNada() {
        when(roleRepository.findByName("SUPERVISOR")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createUser(new UserCreateRequest(
                "Ana", "ana@inventario.local", "ventas1234", "SUPERVISOR")))
                .isInstanceOf(RoleNotFoundException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void unAdminNoPuedeDesactivarseASiMismo() {
        Role admin = role(1L, "ADMIN");
        User me = user(1L, "admin@inventario.local", admin, true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(me));
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(admin));

        assertThatThrownBy(() -> service.updateUser(1L,
                new UserUpdateRequest("Zahid", "ADMIN", false), "admin@inventario.local"))
                .isInstanceOf(UserOperationNotAllowedException.class)
                .hasMessageContaining("desactivar");

        assertThat(me.getActive()).isTrue();
        verify(userRepository, never()).saveAndFlush(any());
    }

    @Test
    void unAdminNoPuedeCambiarSuPropioRol() {
        Role admin = role(1L, "ADMIN");
        Role vendedor = role(2L, "VENDEDOR");
        User me = user(1L, "admin@inventario.local", admin, true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(me));
        when(roleRepository.findByName("VENDEDOR")).thenReturn(Optional.of(vendedor));

        assertThatThrownBy(() -> service.updateUser(1L,
                new UserUpdateRequest("Zahid", "VENDEDOR", true), "admin@inventario.local"))
                .isInstanceOf(UserOperationNotAllowedException.class)
                .hasMessageContaining("rol");

        assertThat(me.getRole().getName()).isEqualTo("ADMIN");
        verify(userRepository, never()).saveAndFlush(any());
    }

    @Test
    void unAdminPuedeDesactivarAOtroUsuario() {
        Role vendedor = role(2L, "VENDEDOR");
        User ana = user(2L, "ana@inventario.local", vendedor, true);
        when(userRepository.findById(2L)).thenReturn(Optional.of(ana));
        when(roleRepository.findByName("VENDEDOR")).thenReturn(Optional.of(vendedor));
        when(userRepository.countByRoleNameAndActive("ADMIN", true)).thenReturn(1L);

        UserResponse response = service.updateUser(2L,
                new UserUpdateRequest("Ana Vendedora", "VENDEDOR", false), "admin@inventario.local");

        assertThat(ana.getActive()).isFalse();
        assertThat(response.active()).isFalse();
        verify(userRepository).saveAndFlush(ana);
    }

    @Test
    void siNoQuedaNingunAdminActivo_laOperacionSeRechaza() {
        Role admin = role(1L, "ADMIN");
        User otroAdmin = user(3L, "otro@inventario.local", admin, true);
        when(userRepository.findById(3L)).thenReturn(Optional.of(otroAdmin));
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(admin));
        // Escenario inalcanzable por la API: lo forzamos con el mock
        when(userRepository.countByRoleNameAndActive("ADMIN", true)).thenReturn(0L);

        assertThatThrownBy(() -> service.updateUser(3L,
                new UserUpdateRequest("Otro", "ADMIN", false), "admin@inventario.local"))
                .isInstanceOf(UserOperationNotAllowedException.class)
                .hasMessageContaining("al menos un administrador activo");
    }

    @Test
    void restablecerContrasena_guardaElNuevoHash() {
        User ana = user(2L, "ana@inventario.local", role(2L, "VENDEDOR"), true);
        when(userRepository.findById(2L)).thenReturn(Optional.of(ana));
        when(passwordEncoder.encode("nuevaClave123")).thenReturn("hash-nuevo");

        service.resetPassword(2L, new PasswordResetRequest("nuevaClave123"));

        assertThat(ana.getPasswordHash()).isEqualTo("hash-nuevo");
        verify(userRepository).save(ana);
    }

    @Test
    void restablecerContrasena_usuarioInexistente_lanzaExcepcionYNoGuardaNada() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.resetPassword(
                99L,
                new PasswordResetRequest("nuevaClave123")))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository).findById(99L);
        verifyNoInteractions(passwordEncoder);
        verify(userRepository, never()).save(any(User.class));
    }
}