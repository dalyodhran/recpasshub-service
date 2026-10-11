package com.recpasshub.service.entity;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "maps")
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

    /** Parsed from {@link #location} on persist/update so proximity searches can use plain numeric comparisons. */
    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

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
        syncCoordinates();
    }

    @PreUpdate
    void syncCoordinates() {
        this.latitude = null;
        this.longitude = null;
        if (location == null) {
            return;
        }
        String[] parts = location.split(",");
        if (parts.length != 2) {
            return;
        }
        try {
            double lat = Double.parseDouble(parts[0].trim());
            double lon = Double.parseDouble(parts[1].trim());
            if (Double.isFinite(lat) && Double.isFinite(lon) && Math.abs(lat) <= 90 && Math.abs(lon) <= 180) {
                this.latitude = lat;
                this.longitude = lon;
            }
        } catch (NumberFormatException ignored) {
            // malformed location: leave coordinates null so the map is excluded from proximity searches
        }
    }
}
