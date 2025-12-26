package kh.mes.finalsample251217.dto;

import kh.mes.finalsample251217.entity.WorkOrder;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class WorkOrderResponseDto {
    private Long id;
    private String productCode;
    private int targetQty;
    private int currentQty; // 이 필드를 추가하고 엔티티에서 값을 가져와야 합니다.
    private String status;
    private LocalDateTime orderDate;

    public static WorkOrderResponseDto fromEntity(WorkOrder entity) {
        return WorkOrderResponseDto.builder()
                .id(entity.getId())
                .productCode(entity.getProductCode())
                .targetQty(entity.getTargetQty())
                .currentQty(entity.getCurrentQty()) // 엔티티에 해당 필드가 있다고 가정
                .status(entity.getStatus())
                .orderDate(entity.getCreatedAt())
                .build();
    }
}
