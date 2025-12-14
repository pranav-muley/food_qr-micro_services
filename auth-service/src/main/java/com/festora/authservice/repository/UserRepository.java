package com.festora.authservice.repository;

import com.festora.authservice.enums.Role;
import com.festora.authservice.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> getUserByEmail(String email);
    List<User> getUsersByRole(Role role);
    Optional<User> getUserByUserId(String userId);
}
