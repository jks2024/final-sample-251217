package kh.mes.finalsample251217.repository;

import kh.mes.finalsample251217.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

}
