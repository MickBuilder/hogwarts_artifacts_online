package fr.mikeb.learning.hogwarts_artifacts_online.hogwartsuser;

import fr.mikeb.learning.hogwarts_artifacts_online.system.exception.NotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class UserService implements UserDetailsService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  public List<HogwartsUser> findAll() {
    return userRepository.findAll();
  }

  public HogwartsUser findById(long userId) {
    return userRepository.findById(userId)
        .orElseThrow(() -> new NotFoundException("user", userId + ""));
  }

  public HogwartsUser save(HogwartsUser newUser) {
    newUser.setPassword(passwordEncoder.encode(newUser.getPassword()));
    return userRepository.save(newUser);
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

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    return userRepository.findByUsername(username).map(UserPrincipal::new)
        .orElseThrow(() -> new UsernameNotFoundException("username " + username + " not found"));
  }
}
