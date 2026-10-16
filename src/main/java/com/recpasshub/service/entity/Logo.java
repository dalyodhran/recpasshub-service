package com.recpasshub.service.entity;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "logos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Logo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "logo_guid", nullable = false, unique = true, updatable = false)
    private String logoGuid;

    @Column(name = "s3_file_location", nullable = false)
    private String s3FileLocation;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    public void generateGuid() {
        if (this.logoGuid == null) {
            this.logoGuid = java.util.UUID.randomUUID().toString();
        }
    }
}
