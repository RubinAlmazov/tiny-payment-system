package com.me.tiny_payment_system.domain;

import com.me.tiny_payment_system.api.dto.CreateUserRequest;
import com.me.tiny_payment_system.api.dto.UserDto;
import com.me.tiny_payment_system.domain.db.UserEntity;
import com.me.tiny_payment_system.domain.db.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public UserDto createUser(CreateUserRequest request) {
        boolean exists = userRepository.existsByEmailIgnoreCase(request.email());
        if (exists) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "User already exists: " + request.email()
            );
        }

        UserEntity user = new UserEntity(request.email());
        UserEntity saved = userRepository.save(user);
        return toResponse(saved);
    }

    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public UserDto getUserOrThrow(Long id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User with id=" + id + " not found"));
        return toResponse(user);
    }

    private UserDto toResponse(UserEntity user) {
        return new UserDto(user.getId(), user.getEmail());
    }
}
