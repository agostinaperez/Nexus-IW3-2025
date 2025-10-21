package edu.iua.nexus.auth.model;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserAuthRepository extends JpaRepository<User, Long>{ 
	public Optional<User> findOneByUsernameOrEmail(String username, String email);
}