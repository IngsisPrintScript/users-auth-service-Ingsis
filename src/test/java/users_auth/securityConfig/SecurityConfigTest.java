package users_auth.securityConfig;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

class SecurityConfigTest {

    private SecurityConfig config;

    @BeforeEach
    void setUp() {
        config = new SecurityConfig();
        ReflectionTestUtils.setField(config, "issuerUri", "https://issuer.example/");
        ReflectionTestUtils.setField(config, "audience", "aud");
    }

    @Test
    void corsConfiguration_containsExpectedValues() {
        CorsConfigurationSource source = config.corsConfigurationSource();
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setMethod("GET");
        req.setRequestURI("/");
        CorsConfiguration c = source.getCorsConfiguration(req);
        assertNotNull(c);
        List<String> origins = c.getAllowedOrigins();
        assertTrue(origins.contains("http://localhost:5173"));
        assertTrue(c.getAllowedMethods().contains("GET"));
        assertTrue(c.getAllowedHeaders().contains("*"));
        assertTrue(Boolean.TRUE.equals(c.getAllowCredentials()));
        assertTrue(c.getExposedHeaders().contains("Authorization"));
    }

    @Test
    void jwtDecoder_setsValidator() {
        org.springframework.security.oauth2.jwt.NimbusJwtDecoder decoder = org.mockito.Mockito
                .mock(org.springframework.security.oauth2.jwt.NimbusJwtDecoder.class);
        try (org.mockito.MockedStatic<org.springframework.security.oauth2.jwt.JwtDecoders> m = org.mockito.Mockito
                .mockStatic(org.springframework.security.oauth2.jwt.JwtDecoders.class)) {
            m.when(() -> org.springframework.security.oauth2.jwt.JwtDecoders
                    .fromIssuerLocation("https://issuer.example/")).thenReturn(decoder);

            var d = config.jwtDecoder();
            org.mockito.Mockito.verify(decoder).setJwtValidator(org.mockito.ArgumentMatchers.any());
            assertNotNull(d);
        }
    }
}
