package tw.readingclub.newsfeed.feed;

import java.time.Instant;
import java.util.UUID;

/**
 * Queue 內只傳遞必要資訊，避免重複搬運完整貼文內容。
 */
public record PostCreatedEvent(UUID postId, UUID authorId, Instant createdAt) {}
