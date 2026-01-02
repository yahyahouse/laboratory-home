package com.yahya.repository;

import com.yahya.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsersRepository extends JpaRepository<Users, String> {
    @Query(value = "SELECT * FROM users", nativeQuery = true)
    List<Users> getAllUsers();

    Users findByEmailAndPassword(String email, String password);
    Optional<Users> findByEmail(String email);

}
