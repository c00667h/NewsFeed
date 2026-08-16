package tw.readingclub.newsfeed.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface AppUserRepository extends JpaRepository<AppUser, UUID> {

  Optional<AppUser> findByEmailIgnoreCase(String email);

  boolean existsByEmailIgnoreCase(String email);
}
