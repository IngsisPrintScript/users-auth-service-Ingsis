package users_auth;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import org.junit.jupiter.api.Test;

class UsersAuthApplicationUnitTest {

  @Test
  void mainMethod_exists_andIsStatic() throws NoSuchMethodException {
    Method m = UsersAuthApplication.class.getMethod("main", String[].class);
    assertNotNull(m);
    assertTrue(Modifier.isStatic(m.getModifiers()));
  }
}
