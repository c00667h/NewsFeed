package tw.readingclub.newsfeed.feed;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import tw.readingclub.newsfeed.post.Post;
import tw.readingclub.newsfeed.post.PostRepository;
import tw.readingclub.newsfeed.user.AppUser;

import java.util.*;
import java.util.stream.*;

@Service
@RequiredArgsConstructor
public class FeedService {

  private static final String GLOBAL_FEED_KEY = "feed:global";
  private final PostRepository postRepository;
  private final StringRedisTemplate redisTemplate;
  private final RabbitTemplate rabbitTemplate;

  public Post createPost(AppUser author, String content) {

    Post createdPost = postRepository.save(new Post(author, content.trim()));
    rabbitTemplate.convertAndSend(
      RabbitConfig.EXCHANGE,
      RabbitConfig.KEY,
      new PostCreatedEvent(createdPost.getId(), author.getId(), createdPost.getCreatedAt()));

    return createdPost;
  }

  public List<Post> getLatestPosts(int limit) {

    Set<String> cachedPostIds =
      redisTemplate.opsForZSet().reverseRange(GLOBAL_FEED_KEY, 0, limit - 1);
    if (cachedPostIds == null || cachedPostIds.isEmpty()) {
      postRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, 500)).forEach(this::cachePost);
      cachedPostIds = redisTemplate.opsForZSet().reverseRange(GLOBAL_FEED_KEY, 0, limit - 1);
    }
    List<UUID> postIds =
      (cachedPostIds == null ? Set.<String>of() : cachedPostIds)
        .stream().map(UUID::fromString).toList();
    Map<UUID, Post> postsById =
      postRepository.findAllByIdIn(postIds).stream()
        .collect(Collectors.toMap(Post::getId, post -> post));

    return postIds.stream().map(postsById::get).filter(Objects::nonNull).toList();
  }

  public void cachePost(Post post) {

    redisTemplate
      .opsForZSet()
      .add(GLOBAL_FEED_KEY, post.getId().toString(), post.getCreatedAt().toEpochMilli());
    redisTemplate.opsForZSet().removeRange(GLOBAL_FEED_KEY, 0, -501);
  }
}
