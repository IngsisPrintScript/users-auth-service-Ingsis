package users_auth.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import users_auth.dto.UserResult;

class AuthServiceTest {

  private AuthService service;
  private RestTemplate restTemplate;

  @BeforeEach
  void setUp() {
    service = new AuthService();
    restTemplate = mock(RestTemplate.class);
    ReflectionTestUtils.setField(service, "restTemplate", restTemplate);
    ReflectionTestUtils.setField(service, "domain", "example.com");
    ReflectionTestUtils.setField(service, "clientId", "id");
    ReflectionTestUtils.setField(service, "clientSecret", "secret");
    ReflectionTestUtils.setField(service, "audience", "aud");
  }

  @Test
  void getManagementApiToken_success() {
    when(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
        .thenReturn(new ResponseEntity<>(Map.of("access_token", "tk"), HttpStatus.OK));

    String token = (String) ReflectionTestUtils.invokeMethod(service, "getManagementApiToken");
    assertEquals("tk", token);
  }

  @Test
  void userExists_whenPresent_returnsTrue() {
    when(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
        .thenReturn(new ResponseEntity<>(Map.of("access_token", "tk"), HttpStatus.OK));

    when(restTemplate.exchange(
            anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
        .thenReturn(new ResponseEntity<>("{}", HttpStatus.OK));

    assertTrue(service.userExists("user1"));
  }

  @Test
  void userExists_whenNotFound_returnsFalse() {
    when(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
        .thenReturn(new ResponseEntity<>(Map.of("access_token", "tk"), HttpStatus.OK));

    when(restTemplate.exchange(
            anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
        .thenThrow(
            HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found", null, null, null));

    assertFalse(service.userExists("missing"));
  }

  @Test
  void getUserById_returnsNameWhenPresent() {
    when(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
        .thenReturn(new ResponseEntity<>(Map.of("access_token", "tk"), HttpStatus.OK));

    when(restTemplate.exchange(
            anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)))
        .thenReturn(new ResponseEntity<>(Map.of("user_id", "u1", "name", "Alice"), HttpStatus.OK));

    UserResult res = service.getUserById("u1");
    assertNotNull(res);
    assertEquals("u1", res.userId());
    assertEquals("Alice", res.name());
  }

  @Test
  void getUserById_usesNicknameWhenNameMissing() {
    when(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
        .thenReturn(new ResponseEntity<>(Map.of("access_token", "tk"), HttpStatus.OK));

    when(restTemplate.exchange(
            anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)))
        .thenReturn(
            new ResponseEntity<>(Map.of("user_id", "u2", "nickname", "nick"), HttpStatus.OK));

    UserResult res = service.getUserById("u2");
    assertNotNull(res);
    assertEquals("u2", res.userId());
    assertEquals("nick", res.name());
  }

  @Test
  void getUserById_nullBody_returnsNull() {
    when(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
        .thenReturn(new ResponseEntity<>(Map.of("access_token", "tk"), HttpStatus.OK));

    when(restTemplate.exchange(
            anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)))
        .thenReturn(new ResponseEntity<>(null, HttpStatus.OK));

    assertNull(service.getUserById("u3"));
  }

  @Test
  void getUserById_httpError_throwsRuntime() {
    when(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
        .thenReturn(new ResponseEntity<>(Map.of("access_token", "tk"), HttpStatus.OK));

    when(restTemplate.exchange(
            anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)))
        .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Bad"));

    assertThrows(RuntimeException.class, () -> service.getUserById("u4"));
  }

  @Test
  void userExists_whenTokenFetchFails_throwsRuntime() {
    when(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
        .thenThrow(new RuntimeException("no network"));

    assertThrows(RuntimeException.class, () -> service.userExists("u1"));
  }

  @Test
  void getUserById_whenTokenFetchFails_throwsRuntime() {
    when(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
        .thenThrow(new RuntimeException("no network"));

    assertThrows(RuntimeException.class, () -> service.getUserById("u1"));
  }
}
