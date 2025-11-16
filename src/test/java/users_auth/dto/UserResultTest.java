package users_auth.dto;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class UserResultTest {

  @Test
  void record_accessorsReturnValues() {
    UserResult ur = new UserResult("u1", "Alice");
    assertEquals("u1", ur.userId());
    assertEquals("Alice", ur.name());
  }
}
