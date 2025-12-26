package kh.mes.finalsample251217.repository;

import kh.mes.finalsample251217.entity.ProductionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductionRepository extends JpaRepository<ProductionLog, Long> {
}
