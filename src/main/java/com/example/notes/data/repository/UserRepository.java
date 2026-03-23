package com.example.notes.data.repository;

import com.example.notes.data.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    /** Used for username autocomplete — finds all users whose username contains the query string */
    List<User> findByUsernameContainingIgnoreCase(String query);
}
