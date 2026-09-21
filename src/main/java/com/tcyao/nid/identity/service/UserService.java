package com.tcyao.nid.identity.service;

import com.tcyao.nid.identity.dto.CreateUserRequest;
import com.tcyao.nid.identity.dto.CreateUserResponse;
import com.tcyao.nid.identity.dto.GetUserRequest;
import com.tcyao.nid.identity.dto.GetUserResponse;
import com.tcyao.nid.identity.entity.Role;
import com.tcyao.nid.identity.entity.User;
import com.tcyao.nid.identity.exception.UserEmailExistsException;
import com.tcyao.nid.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder encoder;

    @Transactional
    public CreateUserResponse createNewUser(CreateUserRequest user) {
        Optional<User> foundUser = userRepository.findByEmail(user.email());
        if (foundUser.isPresent()) {
            throw new UserEmailExistsException(user.email());
        }
        User newUser = new User();
        newUser.setEmail(user.email());
        newUser.setHashedPassword(encoder.encode(user.password()));
        newUser.setRole(Role.USER);
        userRepository.save(newUser);
        return new CreateUserResponse(newUser.getId(), newUser.getEmail());
    }

    @Transactional(readOnly = true)
    public GetUserResponse getUser(GetUserRequest user) {
        User foundUser = userRepository.findById(user.id()).orElseThrow(() -> new NoSuchElementException(user.id().toString()));
        return new GetUserResponse(foundUser.getId(), foundUser.getEmail());
    }
}
