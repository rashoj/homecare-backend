package com.homecare.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "organization_compliance_rules",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_org_compliance_rule",
                        columnNames = {"organization_id", "record_type"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizationComplianceRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(name = "record_type", nullable = false)
    private String recordType;

    private String displayName;

    private Boolean required;

    private Integer weight;

    private Boolean blockScheduling;

    private Boolean active;

    private Integer warningDaysBeforeExpiration;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();

        if (required == null) required = true;
        if (weight == null) weight = 0;
        if (blockScheduling == null) blockScheduling = false;
        if (active == null) active = true;
        if (warningDaysBeforeExpiration == null) warningDaysBeforeExpiration = 30;
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}