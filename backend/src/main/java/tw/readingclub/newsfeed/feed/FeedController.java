package tw.readingclub.newsfeed.feed;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tw.readingclub.newsfeed.feed.dto.CreatePostRequest;
import tw.readingclub.newsfeed.feed.dto.PostResponse;
import tw.readingclub.newsfeed.post.Post;
import tw.readingclub.newsfeed.user.AppUser;
import tw.readingclub.newsfeed.user.AppUserRepository;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FeedController {

  private final FeedService feedService;
  private final AppUserRepository userRepository;

  @PostMapping("/posts")
  public ResponseEntity<PostResponse> create(
    @RequestBody @Valid CreatePostRequest createPostRequest, Authentication authentication) {

    AppUser currentUser =
      userRepository.findById((UUID) authentication.getPrincipal()).orElseThrow();
    Post createdPost = feedService.createPost(currentUser, createPostRequest.content());

    return ResponseEntity.status(HttpStatus.CREATED).body(toPostResponse(createdPost));
  }

  @GetMapping("/feed")
  public List<PostResponse> latest(@RequestParam(defaultValue = "30") @Min(1) @Max(100) int limit) {

    return feedService.getLatestPosts(limit).stream().map(this::toPostResponse).toList();
  }

  private PostResponse toPostResponse(Post post) {

    return new PostResponse(
      post.getId(), post.getContent(), post.getCreatedAt(), post.getAuthor().getDisplayName());
  }
}
