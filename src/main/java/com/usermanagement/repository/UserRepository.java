package com.usermanagement.repository;

import com.usermanagement.modelentity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<List<User>> findByUserFullName(String fullName);

    Optional<User> findByUserName(String userName);
    Optional<User> findByEmail(String email);


}
