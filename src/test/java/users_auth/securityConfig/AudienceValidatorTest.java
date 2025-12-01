package users_auth.securityConfig;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

class AudienceValidatorTest {

    @Test
    void validate_withAudience_succeeds() {
        Jwt jwt = new Jwt("t", Instant.now(), Instant.now().plusSeconds(3600), Map.of("alg", "none"),
                Map.of("aud", List.of("my-aud")));
        AudienceValidator v = new AudienceValidator("my-aud");
        OAuth2TokenValidatorResult res = v.validate(jwt);
        assertFalse(res.hasErrors());
    }

    @Test
    void validate_withoutAudience_fails() {
        Jwt jwt = new Jwt("t", Instant.now(), Instant.now().plusSeconds(3600), Map.of("alg", "none"),
                Map.of("aud", List.of("other")));
        AudienceValidator v = new AudienceValidator("my-aud");
        var res = v.validate(jwt);
        assertTrue(res.hasErrors());
    }
}
