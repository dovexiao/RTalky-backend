package com.example.rtalky.domain.repository;

import com.example.rtalky.domain.model.User;
import org.jmolecules.ddd.annotation.Repository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Cacheable(value = "users", key = "#userId")
    Optional<User> findByUserId(String userId);
}
