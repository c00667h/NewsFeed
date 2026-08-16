package tw.readingclub.newsfeed.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * 使用者帳號；密碼僅保存 BCrypt 雜湊，絕不保存明碼。
 */
@Entity
@Table(name = "app_users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AppUser {

  @Id
  private UUID id;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(name = "display_name", nullable = false)
  private String displayName;

  @Column(name = "password_hash", nullable = false)
  private String passwordHash;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  public AppUser(String email, String displayName, String passwordHash) {

    this.id = UUID.randomUUID();
    this.email = email;
    this.displayName = displayName;
    this.passwordHash = passwordHash;
    this.createdAt = Instant.now();
  }
}
