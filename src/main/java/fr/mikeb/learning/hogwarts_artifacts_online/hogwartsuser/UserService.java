package fr.mikeb.learning.hogwarts_artifacts_online.hogwartsuser;

import fr.mikeb.learning.hogwarts_artifacts_online.system.exception.NotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
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
    var oldUser = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("user", userId + ""));
    var authentication = SecurityContextHolder.getContext().getAuthentication();

    // if the user is not an admin, then the user can only update her username
    if (authentication.getAuthorities().stream().noneMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_admin"))) {
      oldUser.setUsername(update.getUsername());
    } else { // If the user is an admin, then the user can update username, enabled, and roles.
      oldUser.setUsername(update.getUsername());
      oldUser.setEnabled(update.isEnabled());
      oldUser.setRoles(update.getRoles());
    }

    return userRepository.save(oldUser);
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
