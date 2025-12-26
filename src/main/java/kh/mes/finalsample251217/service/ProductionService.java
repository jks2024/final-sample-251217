package kh.mes.finalsample251217.service;

import jakarta.transaction.Transactional;
import kh.mes.finalsample251217.entity.*;
import kh.mes.finalsample251217.exception.CustomException;
import kh.mes.finalsample251217.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductionService {
    private final WorkOrderRepository orderRepo;
    private final ProductionRepository logRepo;
    private final MaterialRepository matRepo;
    private final BomRepository bomRepo;

    // [1] 자재 입고
    @Transactional
    public Material inboundMaterial(String code, String name, int amount) {
        Material material = matRepo.findByCode(code)
                .orElse(Material.builder().code(code).name(name).currentStock(0).build());
        material.setCurrentStock(material.getCurrentStock() + amount);
        return matRepo.save(material);
    }

    // [2] 작업 지시 생성
    @Transactional
    public WorkOrder createWorkOrder(String productCode, int targetQty) {
        WorkOrder order = WorkOrder.builder()
                .productCode(productCode).targetQty(targetQty).currentQty(0).status("WAITING").build();
        return orderRepo.save(order);
    }

    // [3] 설비 작업 할당 (C# 폴링 대응)
    @Transactional
    public WorkOrder assignWorkToMachine(String machineId) {

        // 1. 해당 설비가 이미 하고 있는 일이 있는지 확인
        return orderRepo.findByStatusAndAssignedMachineId("IN_PROGRESS", machineId)
                .orElseGet(() -> {
                    // 2. 없다면 'WAITING' 상태인 가장 오래된 지시를 하나 가져옴
                    WorkOrder waiting = orderRepo.findFirstByStatusOrderByIdAsc("WAITING").orElse(null);
                    if (waiting != null) {
                        // 자재가 있는지 먼저 확인
                        if (!isMaterialAvailable(waiting.getProductCode())) {
                            return null; // 자재가 없으면 할당하지 않음 (C#은 NoContent 응답을 받음)
                        }

                        waiting.setStatus("IN_PROGRESS");
                        waiting.setAssignedMachineId(machineId);
                        // save()를 명시하지 않아도 변경 감지로 인해 업데이트됨
                    }
                    return waiting;
                });
    }

    // 2. 생산 실적 보고 (MES의 핵심: 실적기록 + 자재차감 + 수량증가)
    @Transactional
    public void reportProduction(Long orderId, String machineId, String result, String defectCode) {
        WorkOrder order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("작업 지시를 찾을 수 없습니다. ID: " + orderId));

        if ("COMPLETED".equals(order.getStatus())) return;

        // 3. 생산 이력(ProductionLog) 저장 - 5M1E 데이터 수집
        String serialNo = generateSerial(order.getProductCode());
        logRepo.save(ProductionLog.builder()
                .workOrderNo("WO-" + order.getId())
                .productCode(order.getProductCode())
                .machineId(machineId)
                .serialNo(serialNo)
                .result(result)
                .defectCode("NG".equals(result) ? defectCode : null)
                .producedAt(LocalDateTime.now())
                .build());

        // 4. 자재 차감 (Backflushing) - 양품일 때만 차감하는 것이 일반적
        if ("OK".equals(result)) {
            List<Bom> boms = bomRepo.findAllByProductCode(order.getProductCode());
            for (Bom bom : boms) {
                Material mat = bom.getMaterial();
                int required = bom.getRequiredQty();
                int current = mat.getCurrentStock();

                // [핵심 추가] 차감 전 재고 확인
                if (current < required) {
                    // 자재가 부족하면 예외를 던져 전체 프로세스를 롤백시킵니다.
                    // 메시지에 부족한 자재명을 담아 설비나 UI에 알릴 수 있습니다.
                    throw new CustomException("SHORTAGE", "MATERIAL_SHORTAGE:" + mat.getName());
                }

                // 재고가 충분할 때만 차감 실행
                mat.setCurrentStock(current - required);
                log.info("[Backflushing] 자재: {}, 차감후 재고: {}", mat.getName(), mat.getCurrentStock());
            }
        } else {
            log.info("생산 불량 !!!!, 자재 차감 하지 않음");
        }

        // 6. 수량 업데이트 및 완료 처리
        order.setCurrentQty(order.getCurrentQty() + 1);
        if (order.getCurrentQty() >= order.getTargetQty()) {
            order.setStatus("COMPLETED");
        }

        log.info("[생산보고] {} - 수량: {}/{}", order.getProductCode(), order.getCurrentQty(), order.getTargetQty());
    }

    // 시리얼 번호 생성 유틸리티
    private String generateSerial(String productCode) {
        return productCode + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public List<WorkOrder> getAllWorkOrders() { return orderRepo.findAllByOrderByIdDesc(); }
    public List<Material> getMaterialStock() { return matRepo.findAll(); }

    // 자재 현황 체크 로직
    private boolean isMaterialAvailable(String productCode) {
        List<Bom> boms = bomRepo.findAllByProductCode(productCode);
        for (Bom bom : boms) {
            // 현재 재고 < 1개 생산 당 소요량 이면 생산 불가
            if (bom.getMaterial().getCurrentStock() < bom.getRequiredQty()) {
                log.error("자재 부족: {} (현재: {}, 필요: {})",
                        bom.getMaterial().getName(), bom.getMaterial().getCurrentStock(), bom.getRequiredQty());
                return false;
            }
        }
        return true;
    }
}