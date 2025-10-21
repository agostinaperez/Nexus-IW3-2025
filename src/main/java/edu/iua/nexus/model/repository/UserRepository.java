package edu.iua.nexus.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.iua.nexus.auth.model.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByUsernameAndIdUserNot(String username, long id);

}