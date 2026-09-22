package com.tcyao.nid.identity.controller;

import com.tcyao.nid.identity.dto.*;
import com.tcyao.nid.identity.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/user")
@AllArgsConstructor
public class UserController {
    private final UserService userService;
    private final SecurityContextRepository securityContextRepository;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public ResponseEntity<CreateUserResponse> registerUser(
            @RequestBody CreateUserRequest request
            ) {
        CreateUserResponse res = userService.createNewUser(request);
        return ResponseEntity.status(201).body(res);
    }

    @PostMapping("/login")
    public ResponseEntity<Void> loginUser(
            @RequestBody LoginUserRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
            ) {

        Authentication auth = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(
                        request.email(),
                        request.password()
                )
        );

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(
                context,
                httpRequest,
                httpResponse
        );
        return ResponseEntity.ok().build();
    }

    @GetMapping("")
    public ResponseEntity<GetUserResponse> getUser(Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        GetUserResponse res = userService.getUser(new GetUserRequest((principal.getId())));
        return ResponseEntity.ok(res);
    }

}
