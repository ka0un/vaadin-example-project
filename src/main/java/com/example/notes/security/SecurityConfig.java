package com.example.notes.security;

import com.example.notes.views.LoginView;
import com.vaadin.flow.spring.security.VaadinWebSecurity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@EnableWebSecurity
@Configuration
public class SecurityConfig extends VaadinWebSecurity {

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http.authorizeHttpRequests(auth -> auth
                // ✅ Public pages
                .requestMatchers("/register").permitAll()
                .requestMatchers("/image-upload").permitAll()
                .requestMatchers("/upload").permitAll()
                .requestMatchers("/api/images/**").permitAll()
                .requestMatchers("/uploads/**").permitAll()
        );

        // ✅ Vaadin default security config
        super.configure(http);

        // ✅ Login view
        setLoginView(http, LoginView.class);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}