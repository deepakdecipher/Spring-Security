package com.usermanagement.modelentity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Role entity. Each role record is owned by exactly one user (user_id FK).
 * Roles are eagerly fetched as part of the User aggregate.
 */
@Entity
@Table(name = "roles")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String roleName;

    /** Convenience method for Spring Security's {@code GrantedAuthority}. */
    public String getAuthority() {
        return roleName;
    }
}
