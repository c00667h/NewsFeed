package tw.readingclub.newsfeed.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * 從 HttpOnly cookie 讀取 auth_token，轉為 Spring Security 的目前使用者。
 */
@Component
@RequiredArgsConstructor
public class AuthTokenFilter extends OncePerRequestFilter {

  private final JwtService jwtService;

  @Override
  protected void doFilterInternal(
    HttpServletRequest request, HttpServletResponse response, FilterChain chain)
    throws ServletException, IOException {
    // 一個 request 通常會有多個 cookie（例如語系、分析工具、session）；只挑 auth_token。
    if (request.getCookies() != null) {
      for (Cookie cookie : request.getCookies()) {
        if ("auth_token".equals(cookie.getName())) {
          try {
            UUID userId = jwtService.userId(cookie.getValue());
            // 把已驗證的 userId 放入 SecurityContext，後續 Controller 可從 Authentication 取得它。
            SecurityContextHolder.getContext()
              .setAuthentication(
                new UsernamePasswordAuthenticationToken(userId, null, List.of()));
          } catch (Exception ignored) {
            // 無效或過期 token 視為未登入，並要求瀏覽器立即刪除無效的 auth_token。
            clearAuthenticationToken(response);
          }
        }
      }
    }
    chain.doFilter(request, response);
  }

  private void clearAuthenticationToken(HttpServletResponse response) {

    ResponseCookie expiredAuthenticationToken =
      ResponseCookie.from("auth_token", "")
        .httpOnly(true)
        .sameSite("Strict")
        .path("/")
        .maxAge(0)
        .build();
    response.addHeader(HttpHeaders.SET_COOKIE, expiredAuthenticationToken.toString());
  }
}
