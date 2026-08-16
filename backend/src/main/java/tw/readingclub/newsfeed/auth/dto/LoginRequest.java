package tw.readingclub.newsfeed.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * 登入 API 的輸入資料。
 */
public record LoginRequest(@Email String email, @NotBlank String password) {}
