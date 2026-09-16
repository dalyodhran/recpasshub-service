package com.recpasshub.service.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.validator.constraints.UUID;

@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @UUID
    @Column(name = "event_guid", nullable = false, unique = true, updatable = false)
    private String eventGuid;

    @Column(name = "organization_guid")
    private String organizationGuid;

    @Column(name = "event_name", nullable = false)
    private String eventName;

    @Enumerated(EnumType.STRING)
    @Column(name = "sport_type", nullable = false)
    private SportType sportType;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private EventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false)
    @Builder.Default
    private EventState state = EventState.DRAFT;

    @Column(name = "start_date_time")
    private Instant startDateTime;

    @Column(name = "end_date_time")
    private Instant endDateTime;

    private Integer capacity;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Builder.Default
    @ElementCollection
    @CollectionTable(name = "event_faqs", joinColumns = @JoinColumn(name = "event_id"))
    private List<FAQ> frequentlyAskedQuestions = new ArrayList<>();

    @Column(name = "map_guid")
    private String mapGuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "map_guid", referencedColumnName = "map_guid", insertable = false, updatable = false)
    private Maps map;

    @Column(name = "waiver_guid")
    private String waiverGuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "waiver_guid", referencedColumnName = "waiver_guid", insertable = false, updatable = false)
    private Waiver waiver;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    public void generateGuid() {
        if (this.eventGuid == null) {
            this.eventGuid = java.util.UUID.randomUUID().toString();
        }
    }
}
