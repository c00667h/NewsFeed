package tw.readingclub.newsfeed.auth;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * 集中定義認證規則：密碼如何雜湊、哪些路徑公開、token filter 在何時執行。
 */
@Configuration
public class SecurityConfig {

  @Bean
  PasswordEncoder passwordEncoder() {
    // BCrypt 會為每次雜湊加入 salt，登入時由 matches() 安全比對。

    return new BCryptPasswordEncoder();
  }

  @Bean
  SecurityFilterChain security(HttpSecurity httpSecurity, AuthTokenFilter authTokenFilter)
    throws Exception {
    // Demo 暫時關閉 CSRF：前端與 API 同網域且 Cookie 為 SameSite=Strict。
    // 正式服務若允許跨站請求，必須改為 CSRF token 等完整防護。

    return httpSecurity
      .csrf(csrfConfiguration -> csrfConfiguration.disable())
      // JWT 本身帶有登入狀態，不把使用者資料存進伺服器 HttpSession。
      .sessionManagement(
        sessionManagement ->
          sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
      .authorizeHttpRequests(
        authorization ->
          authorization
            // 註冊、登入與健康檢查必須在尚未登入時可呼叫。
            .requestMatchers("/api/auth/**", "/actuator/health", "/actuator/info")
            .permitAll()
            // 其餘 API（例如發文）都須由 auth_token 驗證。
            .anyRequest()
            .authenticated())
      // 在 Spring 的帳密登入 filter 前，先從 Cookie 建立 Authentication。
      .addFilterBefore(authTokenFilter, UsernamePasswordAuthenticationFilter.class)
      .build();
  }
}
