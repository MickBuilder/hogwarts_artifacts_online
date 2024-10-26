package fr.mikeb.learning.hogwarts_artifacts_online.hogwartsuser;

import fr.mikeb.learning.hogwarts_artifacts_online.system.exception.NotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
  private final UserRepository userRepository;

  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public List<HogwartsUser> findAll() {
    return userRepository.findAll();
  }

  public HogwartsUser findById(long userId) {
    return userRepository.findById(userId)
        .orElseThrow(() -> new NotFoundException("user", userId + ""));
  }
}
