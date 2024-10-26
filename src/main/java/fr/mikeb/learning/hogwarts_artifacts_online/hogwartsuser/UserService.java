package fr.mikeb.learning.hogwarts_artifacts_online.hogwartsuser;

import fr.mikeb.learning.hogwarts_artifacts_online.system.exception.NotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
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

  public HogwartsUser save(HogwartsUser user) {
    return userRepository.save(user);
  }

  public HogwartsUser update(long userId, HogwartsUser update) {
    return userRepository.findById(userId).map(user -> {
      user.setUsername(update.getUsername());
      user.setEnabled(update.isEnabled());
      user.setRoles(update.getRoles());

      return userRepository.save(user);
    }).orElseThrow(() -> new NotFoundException("user", userId + ""));
  }

  public void delete(long userId) {
    userRepository.findById(userId)
        .orElseThrow(() -> new NotFoundException("user", userId + ""));

    userRepository.deleteById(userId);
  }
}
