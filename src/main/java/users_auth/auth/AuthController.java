package users_auth.auth;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import users_auth.dto.UserResult;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/users/exists/{userId}")
    public boolean userExists(@AuthenticationPrincipal Jwt jwt, @PathVariable String userId) {
        return authService.userExists(userId);
    }

    @GetMapping("/users/name")
    public String findUserName(@AuthenticationPrincipal Jwt jwt, @RequestParam String userId) {
        try {
            UserResult userResult = authService.getUserById(userId);
            if (userResult == null) {
                return "Unknown";
            }
            return userResult.name();
        } catch (Exception e) {
            return "Unknown";
        }
    }

    @GetMapping("/users")
    public List<UserResult> getFriends(@AuthenticationPrincipal Jwt jwt, 
            @RequestParam(required = false, defaultValue = "") String name) {
        try {
            if (name == null || name.trim().isEmpty()) {
                return List.of();
            }
            
            return authService.getUsersByName(name);
        } catch (Exception e) {
            throw e;
        }
    }
}