package fr.mikeb.learning.hogwarts_artifacts_online.hogwartsuser;

import fr.mikeb.learning.hogwarts_artifacts_online.client.rediscache.RedisCacheClient;
import fr.mikeb.learning.hogwarts_artifacts_online.system.exception.NotFoundException;
import fr.mikeb.learning.hogwarts_artifacts_online.system.exception.PasswordChangeIllegalArgumentException;
import jakarta.transaction.Transactional;
import org.springframework.security.authentication.BadCredentialsException;
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
  private final RedisCacheClient redisCacheClient;

  public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, RedisCacheClient redisCacheClient) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.redisCacheClient = redisCacheClient;
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

      // Revoke this user's current JWT by deleting it from Redis
      redisCacheClient.delete("whitelist:" + userId);
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

  public void changePassword(long userId, String oldPassword, String newPassword, String confirmNewPassword) {
    var hogwartsUser = userRepository.findById(userId)
        .orElseThrow(() -> new NotFoundException("user", userId + ""));

    // If the old password is not correct, throw an exception.
    if (!passwordEncoder.matches(oldPassword, hogwartsUser.getPassword())) {
      throw new BadCredentialsException("Old password is incorrect.");
    }

    // If the new password and confirm new password do not match, throw an exception.
    if (!newPassword.equals(confirmNewPassword)) {
      throw new PasswordChangeIllegalArgumentException("New password and confirm new password do not match.");
    }

    // The new password must contain at least one digit, one lowercase letter, one uppercase letter, and be at least 8 characters long.
    var passwordPolicy = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$";
    if (!newPassword.matches(passwordPolicy)) {
      throw new PasswordChangeIllegalArgumentException("New password does not conform to password policy.");
    }

    // Encode and save the new password.
    hogwartsUser.setPassword(passwordEncoder.encode(newPassword));

    // Revoke this user's current JWT by deleting it from Redis
    redisCacheClient.delete("whitelist:" + userId);
    userRepository.save(hogwartsUser);
  }
}
