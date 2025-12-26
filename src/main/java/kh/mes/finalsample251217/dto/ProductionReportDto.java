package kh.mes.finalsample251217.dto;

import lombok.Data;

@Data
public class ProductionReportDto {
    private Long orderId;    // 작업지시 ID
    private String machineId;  // 설비 ID
    private String result;     // "OK" or "NG"
    private String defectCode; // 불량 코드
}
