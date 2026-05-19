package com.homecare.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String documentName;

    private String documentType;

    private String fileName;

    private String filePath;

    private String contentType;

    private Long fileSize;

    private LocalDate expirationDate;

    private String approvalStatus;

    private String rejectionReason;

    @ManyToOne
    @JoinColumn(name = "uploaded_by_user_id")
    private User uploadedBy;

    @ManyToOne
    @JoinColumn(name = "client_id")
    private Client client;

    private LocalDateTime uploadedAt;

    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        this.uploadedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        if (this.approvalStatus == null) {
            this.approvalStatus = "PENDING";
        }
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}