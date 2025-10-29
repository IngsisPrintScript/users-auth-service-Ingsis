package users_auth.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import users_auth.dto.UserResult;

import java.util.List;

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

    @GetMapping("/find")
    public List<UserResult> findUser(@RequestParam String search) {
        return authService.search(search);
    }
}
