package kh.mes.finalsample251217.repository;

import kh.mes.finalsample251217.entity.Bom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BomRepository extends JpaRepository<Bom, Long> {
    List<Bom> findAllByProductCode(String productCode);
}
