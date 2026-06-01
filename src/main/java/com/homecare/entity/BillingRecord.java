package com.homecare.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "billing_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillingRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "timesheet_id", nullable = false, unique = true)
    private Timesheet timesheet;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne
    @JoinColumn(name = "authorization_id")
    private ClientAuthorization authorization;

    private LocalDate serviceDate;

    private Double units;

    private Double billingRate;

    private Double amount;

    private String status;
    // DRAFT, READY_TO_CLAIM, CLAIM_SUBMITTED, PAID, DENIED, VOID

    private String claimNumber;

    private Double paidAmount;

    private LocalDate paidDate;

    @Column(columnDefinition = "TEXT")
    private String notes;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        if (this.status == null) {
            this.status = "DRAFT";
        }

        if (this.paidAmount == null) {
            this.paidAmount = 0.0;
        }
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}