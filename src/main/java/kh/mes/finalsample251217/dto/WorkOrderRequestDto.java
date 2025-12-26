package kh.mes.finalsample251217.dto;

import lombok.Data;

@Data
public class WorkOrderRequestDto {
    private String productCode;
    private int targetQty;
}
