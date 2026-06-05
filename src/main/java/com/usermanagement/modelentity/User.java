package com.usermanagement.modelentity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Default concrete user entity provided by the library.
 *
 * <p>Contains all fields from {@link BaseUser} (id, email, password, roles, etc.).
 * If you need to add application-specific fields, extend {@link BaseUser} directly
 * in your project — the library will back off its own beans automatically.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class User extends BaseUser {
    // All fields inherited from BaseUser.
    // Extend BaseUser in your own project to add custom fields.
}
