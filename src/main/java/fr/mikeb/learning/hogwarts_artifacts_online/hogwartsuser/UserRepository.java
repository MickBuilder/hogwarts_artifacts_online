package fr.mikeb.learning.hogwarts_artifacts_online.hogwartsuser;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<HogwartsUser, Long> {
  Optional<HogwartsUser> findByUsername(String username);
}
