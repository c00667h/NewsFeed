package tw.readingclub.newsfeed.feed;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

  public static final String EXCHANGE = "newsfeed.events",
    QUEUE = "feed.fanout",
    KEY = "post.created";

  @Bean
  TopicExchange feedExchange() {

    return new TopicExchange(EXCHANGE);
  }

  @Bean
  Queue feedQueue() {

    return new Queue(QUEUE, true);
  }

  @Bean
  Binding feedBinding(Queue feedQueue, TopicExchange feedExchange) {

    return BindingBuilder.bind(feedQueue).to(feedExchange).with(KEY);
  }

  /**
   * JSON 事件讓 RabbitMQ 管理介面與後續其他 consumer 都容易觀察。
   */
  @Bean
  MessageConverter messageConverter() {

    return new Jackson2JsonMessageConverter();
  }
}
