package kh.mes.finalsample251217.controller;

import kh.mes.finalsample251217.dto.MaterialInboundDto;
import kh.mes.finalsample251217.dto.ProductionReportDto;
import kh.mes.finalsample251217.dto.WorkOrderRequestDto;
import kh.mes.finalsample251217.dto.WorkOrderResponseDto;
import kh.mes.finalsample251217.entity.Material;
import kh.mes.finalsample251217.entity.WorkOrder;
import kh.mes.finalsample251217.service.ProductionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mes")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
@Slf4j
public class MesController {
    private final ProductionService productionService;

    // --- [Web Dashboard API] ---
    @PostMapping("/material/inbound")
    public ResponseEntity<Material> inboundMaterial(@RequestBody MaterialInboundDto dto) {
        log.info("inbound material inbound dto {}", dto);
        return ResponseEntity
                .ok(productionService.inboundMaterial(dto.getCode(), dto.getName(), dto.getAmount()));
    }
    @GetMapping("/materials")
    public ResponseEntity<List<Material>> getAllMaterials() {
        return ResponseEntity.ok(productionService.getMaterialStock());
    }
    @PostMapping("/order")
    public ResponseEntity<WorkOrderResponseDto> createOrder(@RequestBody WorkOrderRequestDto dto) {
        WorkOrder order = productionService.createWorkOrder(dto.getProductCode(), dto.getTargetQty());
        return ResponseEntity.ok(WorkOrderResponseDto.fromEntity(order));
    }
    @GetMapping("/orders")
    public ResponseEntity<List<WorkOrderResponseDto>> getAllOrders() {
        List<WorkOrderResponseDto> dtos = productionService.getAllWorkOrders()
                .stream()
                .map(WorkOrderResponseDto::fromEntity)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    // --- [Machine / PLC API] ---
    @GetMapping("/machine/poll")
    public ResponseEntity<WorkOrderResponseDto> pollWork(@RequestParam String machineId) {
        WorkOrder work = productionService.assignWorkToMachine(machineId);
        return (work != null) ? ResponseEntity.ok(WorkOrderResponseDto.fromEntity(work))
                : ResponseEntity.noContent().build();
    }
    @PostMapping("/machine/report")
    public ResponseEntity<String> reportProduction(@RequestBody ProductionReportDto dto) {
        productionService.reportProduction(dto.getOrderId(), dto.getMachineId(),
                dto.getResult(), dto.getDefectCode());
        return ResponseEntity.ok("ACK");
    }
}
