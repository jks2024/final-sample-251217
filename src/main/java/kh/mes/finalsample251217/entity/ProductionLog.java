package kh.mes.finalsample251217.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
// 생산 이력 (가장 중요한 테이블: 5M1E 정보의 집약체)
public class ProductionLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String workOrderNo; // 작업 지시 번호
    private String productCode; // 제품 코드
    private String machineId;   // 설비 ID

    @Column(unique = true)
    private String serialNo;  // 추적성(Traceability) 핵심: 제품 고유 ID

    private String result;     // OK or NG
    private String defectCode; // 불량인 경우 사유 (예: ERR-001)

    private LocalDateTime producedAt; // 생산 시간
}
