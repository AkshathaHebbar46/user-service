package org.userservice.user_service.spec;

import org.springframework.data.jpa.domain.Specification;
import org.userservice.user_service.entity.UserEntity;

public class UserSpecifications {

    public static Specification<UserEntity> usernameContains(String username) {
        return (root, query, cb) ->
                (username == null || username.isBlank())
                        ? null
                        : cb.like(cb.lower(root.get("username")), "%" + username.toLowerCase() + "%");
    }

    public static Specification<UserEntity> emailContains(String email) {
        return (root, query, cb) ->
                (email == null || email.isBlank())
                        ? null
                        : cb.like(cb.lower(root.get("email")), "%" + email.toLowerCase() + "%");
    }

    public static Specification<UserEntity> statusEquals(Boolean active) {
        return (root, query, cb) ->
                (active == null)
                        ? null
                        : cb.equal(root.get("active"), active);
    }

    public static Specification<UserEntity> roleEquals(String role) {
        return (root, query, cb) ->
                (role == null || role.isBlank())
                        ? null
                        : cb.equal(cb.lower(root.get("role")), role.toLowerCase());
    }
}
