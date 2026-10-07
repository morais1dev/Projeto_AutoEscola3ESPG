package br.com.fiap3espg.autoescola3espg.infra.security;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity //Decisão de gerenciar autorizações pelo controller
@EnableMethodSecurity //Decisão de gerenciar autorizações pelo controller
@RequiredArgsConstructor
public class SecurityConfig {
    private final SecurityFilter securityFilter;

    //Origens (front-ends) autorizadas a consumir a API via navegador - configurável no application.properties
    @Value("${api.cors.allowed-origins:http://localhost:3000,http://127.0.0.1:3000,http://localhost:5500,http://127.0.0.1:5500}")
    private String[] allowedOrigins;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        return http
                .csrf(csfr -> csfr.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(sm ->
                        sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth ->
                        auth
                                .requestMatchers("/login").permitAll()
                                .requestMatchers("/health-check").permitAll()
                                .requestMatchers("/error").permitAll()
                                .requestMatchers(
                                        "/v3/api-docs.yaml",
                                        "/v3/api-docs/**",
                                        "/swagger-ui.html",
                                        "/swagger-ui/**"
                                ).permitAll()
                                //Opção de gerenciamento de autorizações pelo config (mais profissional)
                                /*.requestMatchers(HttpMethod.POST, "/instrutores").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.GET, "/instrutores").hasAnyRole("ADMIN", "USER")
                                .requestMatchers(HttpMethod.GET, "/instrutores/{id}").hasRole("ADMIN")
                                .requestMatchers("/instrutores").hasRole("ADMIN")
                                ... */
                                .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        //Requisição sem token (ou com token inválido/expirado) -> 401
                        .authenticationEntryPoint((request, response, authException) ->
                                escreverErro(response, HttpServletResponse.SC_UNAUTHORIZED,
                                        "Token JWT ausente, inválido ou expirado!"))
                        //Usuário autenticado, mas sem permissão -> 403
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                escreverErro(response, HttpServletResponse.SC_FORBIDDEN,
                                        "Acesso negado: seu perfil não tem permissão para esta operação!"))
                )
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList(allowedOrigins));
        configuration.setAllowedMethods(List.of(
                "GET",
                "POST",
                "PUT",
                "PATCH",
                "DELETE",
                "OPTIONS",
                "HEAD"
        ));
        configuration.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "Accept",
                "Origin",
                "X-Requested-With"
        ));
        configuration.setExposedHeaders(List.of("Location"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private static void escreverErro(HttpServletResponse response, int status, String mensagem) throws IOException {
        response.setStatus(status);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.getWriter().write("{\"message\":\"" + mensagem + "\"}");
    }
}
