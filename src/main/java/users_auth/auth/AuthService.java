package users_auth.auth;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import users_auth.dto.UserResult;

@Service
public class AuthService {

  @Value("${AUTH0_DOMAIN}")
  private String domain;

  @Value("${AUTH0_MGMT_CLIENT_ID}")
  private String clientId;

  @Value("${AUTH0_MGMT_CLIENT_SECRET}")
  private String clientSecret;

  @Value("${AUTH0_MGMT_AUDIENCE}")
  private String audience;

  private final RestTemplate restTemplate = new RestTemplate();

  private String getManagementApiToken() {
    String url = String.format("https://%s/oauth/token", domain);

    Map<String, String> body = new HashMap<>();
    body.put("client_id", clientId);
    body.put("client_secret", clientSecret);
    body.put("audience", audience);
    body.put("grant_type", "client_credentials");

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

    try {
      ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
      return (String) response.getBody().get("access_token");
    } catch (Exception e) {
      throw new RuntimeException("Error obtaining Auth0 Management API token", e);
    }
  }

  /** Verifica si un usuario existe en Auth0 */
  public boolean userExists(String userId) {
    String token = getManagementApiToken();
    String url = String.format("https://%s/api/v2/users/%s", domain, userId);

    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(token);

    HttpEntity<Void> request = new HttpEntity<>(headers);

    try {
      ResponseEntity<String> response =
          restTemplate.exchange(url, HttpMethod.GET, request, String.class);
      return response.getStatusCode() == HttpStatus.OK;
    } catch (HttpClientErrorException.NotFound e) {
      return false;
    } catch (Exception e) {
      throw new RuntimeException("Error querying Auth0", e);
    }
  }

  public UserResult getUserById(String userId) {
    String token = getManagementApiToken();
    String url = String.format("https://%s/api/v2/users/%s", domain, userId);

    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(token);
    headers.setContentType(MediaType.APPLICATION_JSON);

    HttpEntity<Void> request = new HttpEntity<>(headers);

    try {
      ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, request, Map.class);

      Map<?, ?> user = response.getBody();
      if (user == null) return null;

      return new UserResult(
          (String) user.get("user_id"),
          user.get("name") != null
              ? (String) user.get("name")
              : (user.get("nickname") != null ? (String) user.get("nickname") : "Unknown"));
    } catch (HttpClientErrorException e) {
      throw new RuntimeException(
          "Error getting user from Auth0: " + e.getResponseBodyAsString(), e);
    }
  }
}
