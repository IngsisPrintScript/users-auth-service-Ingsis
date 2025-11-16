package users_auth.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
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
  public boolean userExists(@AuthenticationPrincipal Jwt jwt, @PathVariable String userId) {
    return authService.userExists(userId);
  }

  @GetMapping("/users/name")
  public String findUserName(@AuthenticationPrincipal Jwt jwt, @RequestParam String userId) {
    return authService.getUserById(userId).name();
  }
}
