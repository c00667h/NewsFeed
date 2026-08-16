package tw.readingclub.newsfeed.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 註冊 API 的輸入資料；驗證規則集中在 DTO，Controller 不必手動檢查。
 */
public record RegisterRequest(
  @Email String email,
  @NotBlank @Size(max = 40) String displayName,
  @NotBlank @Size(min = 8, max = 100) String password) {}
