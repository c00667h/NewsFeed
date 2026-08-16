package tw.readingclub.newsfeed.feed.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * 動態牆回傳的貼文資料，不直接暴露 JPA Entity。
 */
public record PostResponse(UUID id, String content, Instant createdAt, String authorName) {}
