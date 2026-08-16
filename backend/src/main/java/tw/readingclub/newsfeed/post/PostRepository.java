package tw.readingclub.newsfeed.post;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface PostRepository extends JpaRepository<Post, UUID> {

  List<Post> findAllByIdIn(Collection<UUID> ids);

  List<Post> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
