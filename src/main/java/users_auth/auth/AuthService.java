package users_auth.auth;

import com.nimbusds.jwt.JWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import users_auth.dto.UserResult;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
public class AuthService {

  @Value("${AUTH0_AUDIENCE}")
  private String domain;

  private final RestTemplate restTemplate = new RestTemplate();

  public boolean userExists(String userId,String jwt) {
    String url = String.format("https://%s/api/v2/users/%s", domain, userId);

    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(jwt);

    HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
            return response.getStatusCode() == HttpStatus.OK;
        } catch (HttpClientErrorException.NotFound e) {
            return false;
        } catch (Exception e) {
            throw new RuntimeException("Error querying Auth0", e);
        }
    }

    public UserResult getUserById(String userId, String jwt) {
        String url = String.format("https://%s/api/v2/users/%s", domain, URLEncoder.encode(userId, StandardCharsets.UTF_8));

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwt);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    request,
                    Map.class
            );

            Map<?, ?> user = response.getBody();
            if (user == null) return null;

            return new UserResult(
                    (String) user.get("user_id"),
                    user.get("name") != null ? (String) user.get("name")
                            : (user.get("nickname") != null ? (String) user.get("nickname") : "Unknown")
            );

        } catch (HttpClientErrorException e) {
            throw new RuntimeException("Error getting user from Auth0: " + e.getResponseBodyAsString(), e);
        }
    }
}
