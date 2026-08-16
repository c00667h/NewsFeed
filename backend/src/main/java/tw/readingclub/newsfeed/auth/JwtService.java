package tw.readingclub.newsfeed.auth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

/**
 * 發出與驗證第一階段使用的 JWT auth_token。
 */
@Service
public class JwtService {

  // HMAC 簽章用的對稱金鑰；只有後端可簽發與驗證 token。
  private final SecretKey key;

  public JwtService(@Value("${app.jwt.secret}") String secret) {
    // 將設定檔字串轉成可供 JJWT 使用的 HMAC key。
    this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
  }

  public String issue(UUID userId) {

    Instant now = Instant.now();
    // JWT payload 包含 subject（使用者 ID）、簽發時間、三十分鐘到期時間；signWith(key) 是簽章。

    return Jwts.builder()
      .subject(userId.toString())
      .issuedAt(Date.from(now))
      .expiration(Date.from(now.plusSeconds(60 * 30)))
      .signWith(key)
      .compact();
  }

  public UUID userId(String token) {
    // verifyWith(key) 會先驗簽與驗過期；驗證失敗會丟例外，交給 filter 視為未登入。

    return UUID.fromString(
      Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().getSubject());
  }
}
