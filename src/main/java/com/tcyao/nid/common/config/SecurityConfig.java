package com.tcyao.nid.common.config;

import com.tcyao.nid.common.middleware.TrailingSlashNormalizationFilter;
import com.tcyao.nid.identity.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.security.web.context.SecurityContextRepository;

import java.util.NoSuchElementException;

@Configuration
public class SecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, TrailingSlashNormalizationFilter trailingSlashNormalizationFilter) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .addFilterBefore(trailingSlashNormalizationFilter, SecurityContextHolderFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/user/register", "/user/login").permitAll()
                        .requestMatchers("/public","/status/**", "/error", "/csrf", "/swagger-ui/**", "/v3/**").permitAll()
                        .anyRequest().authenticated()
                )
//                .formLogin(form -> form.loginPage("/user/login").permitAll()) // Only usable if from a form (url-encoded)
                .logout(logout -> logout.logoutUrl("/user/logout").logoutSuccessUrl("/"));

        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

//    @Bean
//    UserDetailsService userDetailsService(UserRepository userRepository) {
//        return email -> userRepository
//                .findByEmail(email)
//                .map(user -> User.withUsername(user.getEmail())
//                        .password(user.getHashedPassword())
//                        .roles("USER")
//                        .build())
//                .orElseThrow(() -> new UsernameNotFoundException(email));
//    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }
}
