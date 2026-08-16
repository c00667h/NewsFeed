package tw.readingclub.newsfeed.post;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import tw.readingclub.newsfeed.user.AppUser;

import java.time.Instant;
import java.util.UUID;

/**
 * 第一階段只保存純文字；圖片、按讚與回覆留給第二階段。
 */
@Entity
@Table(name = "posts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post {

  @Id
  private UUID id;

  // 第一階段的 feed 讀取量很小，直接帶作者資料可讓 DTO 組裝更單純。
  @ManyToOne(fetch = FetchType.EAGER, optional = false)
  @JoinColumn(name = "author_id")
  private AppUser author;

  @Column(nullable = false, length = 500)
  private String content;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  public Post(AppUser author, String content) {

    this.id = UUID.randomUUID();
    this.author = author;
    this.content = content;
    this.createdAt = Instant.now();
  }
}
