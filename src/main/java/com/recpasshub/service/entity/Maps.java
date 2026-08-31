package com.recpasshub.service.entity;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "mapsf")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Maps {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "map_guid", nullable = false, unique = true, updatable = false)
    private String mapGuid;

    @Column(name = "s3_file_location", nullable = false)
    private String s3FileLocation;

    @Column(name = "distance")
    private Double distance;

    @Column(name = "elevation")
    private Double elevation;

    @Column(name = "location")
    private String location;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    public void generateGuid() {
        if (this.mapGuid == null) {
            this.mapGuid = java.util.UUID.randomUUID().toString();
        }
    }
}
