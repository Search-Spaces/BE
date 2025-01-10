package nomad.searchspace.domain.member.repository;

import nomad.searchspace.domain.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);

//    @Query("select m from Member m join fetch m.scraps")
//    Optional<Member> findByEmailwithScraps(String email);
}
