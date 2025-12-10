package users_auth.dto;

import java.util.List;

public record PaginatedUsersDTO(int page, int pageSize, int count, List<UserResult> users) {
}
