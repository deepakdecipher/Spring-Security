package com.usermanagement.modelentity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;

/**
 * Extensible base for the library's User entity.
 *
 * <b>Out of the box</b>: just include the library — {@link User} extends this
 * and all endpoints work immediately with no extra code.
 *
 * <b>To add custom fields</b> in your application, create your own entity:
 * <pre>
 * {@literal @}Entity
 * {@literal @}Table(name = "users")
 * public class AppUser extends BaseUser {
 *     private String phoneNumber;
 *     // getter/setter via Lombok or manually
 * }
 * </pre>
 * Then provide a {@code JpaRepository<AppUser, Long>} bean and a
 * {@code UserDetailsService} bean — the library's defaults back off via
 * {@code @ConditionalOnMissingBean}.
 */
@MappedSuperclass
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public abstract class BaseUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userFullName;

    @Column(unique = true)
    private String userName;

    private String password;

    @Column(unique = true, nullable = false)
    private String email;

    @Builder.Default
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private Set<Role> roles = new HashSet<>();

    /** False until the user verifies their email OTP. */
    @Builder.Default
    @Column(name = "is_verified", nullable = false)
    private boolean verified = false;
}
