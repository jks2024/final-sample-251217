package kh.mes.finalsample251217.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WorkOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String productCode; // 생산할 제품
    private int targetQty;      // 목표 수량
    private int currentQty;     // 현재 생산량
    private String status;      // WAITING, IN_PROGRESS, COMPLETED

    private String assignedMachineId;  //[추가] 설비 할당 정보 (MES 핵심)


    private LocalDateTime createdAt;  // 생성시점

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
