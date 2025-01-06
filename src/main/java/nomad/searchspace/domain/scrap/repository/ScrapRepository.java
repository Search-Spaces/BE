package nomad.searchspace.domain.scrap.repository;


import nomad.searchspace.domain.scrap.domain.Scrap;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ScrapRepository extends JpaRepository<Scrap, Long>, ScrapQRepository {
    Optional<Scrap> findByPost_PostIdAndMemberId(Long postId, Long memberId);
}
