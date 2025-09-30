package users_auth.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/users/exists/{userId}")
    public boolean userExists(@PathVariable String userId) {
        return authService.userExists(userId);
    }
}
