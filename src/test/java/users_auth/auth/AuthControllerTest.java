package users_auth.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import users_auth.dto.UserResult;

class AuthControllerTest {

  private AuthService authService;
  private AuthController controller;

  @BeforeEach
  void setUp() {
    authService = mock(AuthService.class);
    controller = new AuthController(authService);
  }

  @Test
  void userExists_delegatesToService() {
    when(authService.userExists("u1")).thenReturn(true);
    boolean r = controller.userExists(null, "u1");
    assertTrue(r);
    verify(authService).userExists("u1");
  }

  @Test
  void findUserName_returnsNameFromService() {
    when(authService.getUserById("u2")).thenReturn(new UserResult("u2", "Bob"));
    String name = controller.findUserName(null, "u2");
    assertEquals("Bob", name);
  }
}
