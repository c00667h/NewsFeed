package tw.readingclub.newsfeed.auth;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import tw.readingclub.newsfeed.auth.dto.CurrentUserResponse;
import tw.readingclub.newsfeed.auth.dto.LoginRequest;
import tw.readingclub.newsfeed.auth.dto.RegisterRequest;
import tw.readingclub.newsfeed.user.AppUser;
import tw.readingclub.newsfeed.user.AppUserRepository;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AppUserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;

  @PostMapping("/register")
  public ResponseEntity<CurrentUserResponse> register(
    @RequestBody @Valid RegisterRequest registerRequest, HttpServletResponse response) {

    String email = registerRequest.email().trim().toLowerCase();
    if (userRepository.existsByEmailIgnoreCase(email)) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "此 Email 已註冊");
    }
    AppUser registeredUser =
      userRepository.save(
        new AppUser(
          email,
          registerRequest.displayName().trim(),
          passwordEncoder.encode(registerRequest.password())));
    setAuthenticationToken(response, registeredUser.getId());

    return ResponseEntity.status(HttpStatus.CREATED).body(toMeResponse(registeredUser));
  }

  @PostMapping("/login")
  public CurrentUserResponse login(
    @RequestBody @Valid LoginRequest loginRequest, HttpServletResponse response) {

    AppUser authenticatedUser =
      userRepository
        .findByEmailIgnoreCase(loginRequest.email().trim())
        .filter(
          appUser ->
            passwordEncoder.matches(loginRequest.password(), appUser.getPasswordHash()))
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email 或密碼錯誤"));
    setAuthenticationToken(response, authenticatedUser.getId());

    return toMeResponse(authenticatedUser);
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout(HttpServletResponse response) {

    ResponseCookie cookie =
      ResponseCookie.from("auth_token", "")
        .httpOnly(true)
        .sameSite("Strict")
        .path("/")
        .maxAge(0)
        .build();
    response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

    return ResponseEntity.noContent().build();
  }

  @GetMapping("/me")
  public CurrentUserResponse me(Authentication authentication) {

    // 前端初次載入會用此 API 確認登入狀態；未登入時應回傳 401，而非拋出 NullPointerException。
    if (authentication == null || !(authentication.getPrincipal() instanceof UUID userId)) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
    }

    return toMeResponse(
      userRepository
        .findById(userId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED)));
  }

  private void setAuthenticationToken(HttpServletResponse response, UUID userId) {

    // issue(userId) 產出的是 JWT；瀏覽器只保存 HttpOnly Cookie，JavaScript 無法讀取它。
    ResponseCookie cookie =
      ResponseCookie.from("auth_token", jwtService.issue(userId))
        // 防止前端 JavaScript 竊取 token，降低 XSS 後 token 外流的風險。
        .httpOnly(true)
        // Demo 同站使用；限制瀏覽器不在跨站請求攜帶此 Cookie。
        .sameSite("Strict")
        // Cookie 對整個網站路徑都有效，發文與讀文都可自動攜帶。
        .path("/")
        // JWT 和 Cookie 都設定三十分鐘，避免其中一方先失效造成混淆。
        .maxAge(60 * 30)
        .build();

    response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
  }

  private CurrentUserResponse toMeResponse(AppUser user) {

    return new CurrentUserResponse(user.getId(), user.getEmail(), user.getDisplayName());
  }
}
