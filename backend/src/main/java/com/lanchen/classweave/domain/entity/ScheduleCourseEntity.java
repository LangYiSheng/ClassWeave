package com.lanchen.classweave.domain.entity;

import com.lanchen.classweave.domain.enums.WeekType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "schedule_courses")
public class ScheduleCourseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "schedule_id", nullable = false)
    private ScheduleEntity schedule;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private Integer weekday;

    @Column(name = "start_week", nullable = false)
    private Integer startWeek;

    @Column(name = "end_week", nullable = false)
    private Integer endWeek;

    @Column(name = "start_period", nullable = false)
    private Integer startPeriod;

    @Column(name = "end_period", nullable = false)
    private Integer endPeriod;

    @Enumerated(EnumType.STRING)
    @Column(name = "week_type", nullable = false, length = 20)
    private WeekType weekType = WeekType.ALL;

    @Column(length = 100)
    private String teacher;

    @Column(length = 100)
    private String location;

    @Column(length = 500)
    private String note;

    @Column(name = "is_temporary", nullable = false)
    private Boolean temporary = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
