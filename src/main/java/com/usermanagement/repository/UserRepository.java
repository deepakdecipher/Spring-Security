package com.usermanagement.repository;

import com.usermanagement.modelentity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<List<User>> findByUserFullName(String fullName);

    Optional<User> findByUserName(String userName);

    /** Finds only verified users — used for login. */
    @Query(value = "SELECT * FROM users WHERE email = :email AND is_verified = true", nativeQuery = true)
    Optional<User> findByEmail(@Param("email") String email);

    /** Finds a user regardless of verification status — used during OTP verification flow. */
    @Query(value = "SELECT * FROM users WHERE email = :email", nativeQuery = true)
    Optional<User> findByEmailIncludingUnverified(@Param("email") String email);

    boolean existsByEmail(String email);

    boolean existsByUserName(String userName);
}
