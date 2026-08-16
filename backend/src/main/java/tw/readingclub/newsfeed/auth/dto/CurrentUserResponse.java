package tw.readingclub.newsfeed.auth.dto;

import java.util.UUID;

/**
 * 成功註冊、登入或讀取目前使用者時回傳的公開資料；不包含密碼雜湊。
 */
public record CurrentUserResponse(UUID id, String email, String displayName) {}
