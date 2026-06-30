package com.sport.service.configurations;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfiguration {
    @Value("${jwt.secret}")
    private String secret;

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuer;

    @Bean
    @Order(1)
    SecurityFilterChain adminChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/admin/**")
                .authorizeHttpRequests(auth -> auth.anyRequest().hasRole("ADMIN"))
                .oauth2Login(oauth ->
                        oauth.userInfoEndpoint(userInfo ->
                                userInfo.oidcUserService(oidcUserService())
                        )
                )
                .logout(logout -> logout.logoutSuccessUrl("/"))
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    @Order(2)
    SecurityFilterChain actuatorChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/actuator/**")
                .authorizeHttpRequests(auth -> auth.anyRequest().hasRole("ADMIN"))
                .oauth2ResourceServer(oauth ->
                        oauth.jwt(jwt ->
                                jwt.decoder(keycloakJwtDecoder())
                                        .jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                )
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults());

        return http.build();
    }


    @Bean
    public OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService() {
        OidcUserService delegate = new OidcUserService();
        return userRequest -> {
            OidcUser oidcUser = delegate.loadUser(userRequest);
            Collection<GrantedAuthority> mappedAuthorities = new HashSet<>(oidcUser.getAuthorities());
            Map<String, Object> realmAccess = oidcUser.getAttribute("realm_access");
            if (realmAccess != null && realmAccess.containsKey("roles")) {
                ((List<String>) realmAccess.get("roles"))
                        .forEach(role -> mappedAuthorities.add(new SimpleGrantedAuthority("ROLE_" + role)));
            }
            return new DefaultOidcUser(mappedAuthorities, oidcUser.getIdToken(), oidcUser.getUserInfo());
        };
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthoritiesClaimName("realm_access.roles");
        grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return converter;
    }

    @Bean
    JwtDecoder keycloakJwtDecoder() {
        return JwtDecoders.fromIssuerLocation(issuer);
    }

    @Bean
    @Order(3)
    SecurityFilterChain apiSecurityChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/**", "/auth/**")
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm ->
                        sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/auth/telegram").permitAll()
                        .anyRequest().authenticated()
                )
                .requestCache(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable);
        return http.build();
    }

    @Bean
    @Order(99)
    SecurityFilterChain denyAll(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth.anyRequest().denyAll())
                .csrf(AbstractHttpConfigurer::disable);
        return http.build();
    }
}

//    @Bean
//    @Order(1)
//    SecurityFilterChain filterChainAdmin(HttpSecurity http) throws Exception {
//        http
//                .securityMatcher("/admin/**", "/actuator/**")
//                .authorizeHttpRequests(auth -> auth.anyRequest().hasRole("ADMIN"))
//                .oauth2Login(oauth -> oauth.userInfoEndpoint(userInfo -> userInfo.oidcUserService(oidcUserService())))
//                .oauth2ResourceServer(oauth -> oauth.jwt(jwt -> jwt
//                                .decoder(keycloakJwtDecoder())
//                                .jwtAuthenticationConverter(jwtAuthenticationConverter())))
//                .csrf(AbstractHttpConfigurer::disable)
//                .logout(logout -> logout.logoutSuccessUrl("/"))
//                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//                .requestCache(cache -> cache.disable())
//                .formLogin(form -> form.disable())
//                .httpBasic(basic -> basic.disable())
//                .cors(Customizer.withDefaults());
//        return http.build();
//    }