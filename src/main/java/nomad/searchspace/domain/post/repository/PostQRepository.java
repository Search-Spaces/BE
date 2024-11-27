package nomad.searchspace.domain.post.repository;

import nomad.searchspace.domain.post.DTO.PostRequest;
import nomad.searchspace.domain.post.entity.Post;

import java.util.List;
import java.util.Optional;

public interface PostQRepository {
    List<Post> findByCussor(PostRequest postRequest, int lastLikes, double lastDistance);
}
