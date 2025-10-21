package users_auth.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class AuthService {

  @Value("${AUTH0_AUDIENCE}")
  private String domain;

  @Value("${AUTH0_MANAGEMENT_TOKEN}")
  private String managementToken;

  private final RestTemplate restTemplate = new RestTemplate();

  public boolean userExists(String userId) {
    String url = String.format("https://%s/api/v2/users/%s", domain, userId);

    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(managementToken);

    HttpEntity<Void> request = new HttpEntity<>(headers);

    try {
      ResponseEntity<String> response =
          restTemplate.exchange(url, HttpMethod.GET, request, String.class);
      return response.getStatusCode() == HttpStatus.OK;
    } catch (HttpClientErrorException.NotFound e) {
      return false;
    } catch (Exception e) {
      throw new RuntimeException("Error consultando Auth0", e);
    }
  }
}
