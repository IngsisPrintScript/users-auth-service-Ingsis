package users_auth.auth;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

    private String getManagementToken() {
        String url = "https://" + domain + "/oauth/token";

        Map<String, String> body = Map.of("client_id", clientId, "client_secret", clientSecret, "audience",
                "https://" + domain + "/api/v2/", "grant_type", "client_credentials");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

        if (response.getBody() == null || !response.getBody().containsKey("access_token")) {
            throw new RuntimeException("Failed to obtain management token");
        }

        return (String) response.getBody().get("access_token");
    }

    public boolean userExists(String userId) {
        String token = getManagementToken();
        String url = String.format("https://%s/api/v2/users/%s", domain, userId);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

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

    public List<UserResult> getUsersByName(String name) {
        String token = getManagementToken();
        // Auth0 v3 search engine tiene limitaciones con wildcards para búsquedas parciales
        // Solución: obtener usuarios sin filtro (o con filtro amplio) y filtrar en el backend
        // Obtenemos los primeros 100 usuarios y los filtramos localmente
        String url = String.format("https://%s/api/v2/users?per_page=100&page=0", domain);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<List> response = restTemplate.exchange(url, HttpMethod.GET, request, List.class);

            List<Map<String, Object>> rawUsers = response.getBody();
            if (rawUsers == null) {
                return List.of();
            }

            List<UserResult> results = new ArrayList<>();
            String searchLower = name.toLowerCase().trim();

            // Filtrar usuarios que contengan el texto en name, nickname o email (case-insensitive)
            for (Map<String, Object> user : rawUsers) {
                String userName = user.getOrDefault("name", user.getOrDefault("nickname", "Unknown")).toString();
                String userNickname = user.getOrDefault("nickname", "").toString();
                String userEmail = user.getOrDefault("email", "").toString();
                
                // Buscar en name, nickname y email (case-insensitive)
                boolean matches = userName.toLowerCase().contains(searchLower) 
                    || userNickname.toLowerCase().contains(searchLower)
                    || userEmail.toLowerCase().contains(searchLower);
                
                if (matches) {
                    String userId = (String) user.get("user_id");
                    results.add(new UserResult(userId, userName));
                }
            }

            return results;
        } catch (HttpClientErrorException e) {
            throw new RuntimeException("Error searching users in Auth0: " + e.getResponseBodyAsString(), e);
        }
    }

    public UserResult getUserById(String userId) {
        String token = getManagementToken();
        String url = String.format("https://%s/api/v2/users/%s", domain, userId);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, request, Map.class);

            Map<?, ?> user = response.getBody();
            if (user == null)
                return null;

            return new UserResult((String) user.get("user_id"),
                    user.get("name") != null
                            ? (String) user.get("name")
                            : (user.get("nickname") != null ? (String) user.get("nickname") : "Unknown"));
        } catch (HttpClientErrorException e) {
            throw new RuntimeException("Error getting user from Auth0: " + e.getResponseBodyAsString(), e);
        }
    }
}