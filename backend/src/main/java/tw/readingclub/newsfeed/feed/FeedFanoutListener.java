package tw.readingclub.newsfeed.feed;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import tw.readingclub.newsfeed.post.PostRepository;

/**
 * 第一階段更新全站 feed；日後在此改為取得追蹤者並寫入 feed:{userId}。
 */
@Component
@RequiredArgsConstructor
public class FeedFanoutListener {

  private final FeedService feedService;
  private final PostRepository postRepository;

  @RabbitListener(queues = RabbitConfig.QUEUE)
  public void onPost(PostCreatedEvent postCreatedEvent) {

    postRepository.findById(postCreatedEvent.postId()).ifPresent(feedService::cachePost);
  }
}
