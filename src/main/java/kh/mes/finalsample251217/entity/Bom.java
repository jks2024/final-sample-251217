package kh.mes.finalsample251217.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Bom {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String productCode;

    @ManyToOne
    private Material material;  // 필요한 자재
    private int requiredQty;       // 소요량
}
