package com.homecare.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "client_family_access")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientFamilyAccess {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "family_user_id", nullable = false)
    private User familyUser;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    private Boolean active;

    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.active == null) this.active = true;
    }
}