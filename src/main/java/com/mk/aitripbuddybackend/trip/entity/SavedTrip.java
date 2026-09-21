package com.mk.aitripbuddybackend.trip.entity;

import com.mk.aitripbuddybackend.user.entity.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "saved_trips")
@Getter
@NoArgsConstructor
public class SavedTrip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String destination;

    private String durationLabel;

    @Column(columnDefinition = "TEXT")
    private String planJson;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Builder
    public SavedTrip(User user, String destination, String durationLabel, String planJson) {
        this.user = user;
        this.destination = destination;
        this.durationLabel = durationLabel;
        this.planJson = planJson;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public void update(String destination, String durationLabel, String planJson) {
        this.destination = destination;
        this.durationLabel = durationLabel;
        this.planJson = planJson;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isOwnedBy(Long userId) {
        return this.user.getId().equals(userId);
    }
}