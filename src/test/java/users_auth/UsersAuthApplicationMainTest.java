package users_auth;

import static org.mockito.Mockito.mockStatic;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

class UsersAuthApplicationMainTest {

  @Test
  void main_callsSpringApplicationRun() {
    try (MockedStatic<SpringApplication> mocked = mockStatic(SpringApplication.class)) {
      UsersAuthApplication.main(new String[] {});
      mocked.verify(() -> SpringApplication.run(UsersAuthApplication.class, new String[] {}));
    }
  }
}
