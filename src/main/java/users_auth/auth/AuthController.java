package users_auth.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import users_auth.dto.PaginatedUsers;
import users_auth.dto.UserResult;

@RestController
@RequestMapping
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

    @GetMapping("/users/{userId}")
    public String findUserName(@AuthenticationPrincipal Jwt jwt, @PathVariable String userId) {
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
    public ResponseEntity<PaginatedUsers> getUsers(@AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) String name, @RequestParam(defaultValue = "0") int page,
            @RequestParam(name = "page_size", defaultValue = "10") int pageSize) {
        PaginatedUsers result = authService.getUsers(name, page, pageSize);
        return ResponseEntity.ok(result);
    }

}