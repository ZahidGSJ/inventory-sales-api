package com.zahid.inventoryapi.user;

import java.util.List;
import java.util.Locale;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private static final String ADMIN_ROLE = "ADMIN";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse createUser(UserCreateRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);

        if (userRepository.existsByEmail(email)) {
            throw new DuplicateEmailException("Ya existe un usuario con el email: " + email);
        }

        Role role = findRole(request.role());

        User user = new User();
        user.setName(request.name().trim());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setActive(true);
        user.setRole(role);

        return UserResponse.from(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        return UserResponse.from(findUser(id));
    }

    @Transactional
    public UserResponse updateUser(Long id, UserUpdateRequest request, String currentUserEmail) {
        User user = findUser(id);
        Role newRole = findRole(request.role());

        if (user.getEmail().equals(currentUserEmail)) {
            if (!request.active()) {
                throw new UserOperationNotAllowedException("No puedes desactivar tu propia cuenta");
            }
            if (!newRole.getId().equals(user.getRole().getId())) {
                throw new UserOperationNotAllowedException("No puedes cambiar tu propio rol");
            }
        }

        user.setName(request.name().trim());
        user.setRole(newRole);
        user.setActive(request.active());
        userRepository.saveAndFlush(user);

        ensureAtLeastOneActiveAdmin();

        return UserResponse.from(user);
    }

    @Transactional
    public void resetPassword(Long id, PasswordResetRequest request) {
        User user = findUser(id);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        userRepository.save(user);
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado con id: " + id));
    }

    private Role findRole(String name) {
        return roleRepository.findByName(name.trim().toUpperCase(Locale.ROOT))
                .orElseThrow(() -> new RoleNotFoundException("Rol no encontrado: " + name));
    }

    private void ensureAtLeastOneActiveAdmin() {
        if (userRepository.countByRoleNameAndActive(ADMIN_ROLE, true) == 0) {
            throw new UserOperationNotAllowedException("Debe existir al menos un administrador activo");
        }
    }
}