package tw.readingclub.newsfeed.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 將每次 API 請求的結果輸出到後端 console，方便本機除錯與 Demo 觀察資料流。
 *
 * <p>刻意不記錄 request body、Cookie、Authorization header 或 query string，以避免密碼與 JWT 出現在日誌。</p>
 */
@Component
@Order(1)
@Slf4j
public class ApiRequestLoggingFilter extends OncePerRequestFilter {

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {

    return !request.getRequestURI().startsWith("/api/");
  }

  @Override
  protected void doFilterInternal(
    HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
    throws ServletException, IOException {

    long startedAtNanos = System.nanoTime();
    try {
      filterChain.doFilter(request, response);
    } finally {
      long elapsedMilliseconds = (System.nanoTime() - startedAtNanos) / 1_000_000;
      log.info(
        "API {} {} -> {} ({} ms)",
        request.getMethod(),
        request.getRequestURI(),
        response.getStatus(),
        elapsedMilliseconds);
    }
  }
}
