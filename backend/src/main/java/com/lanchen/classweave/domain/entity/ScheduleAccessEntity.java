package com.lanchen.classweave.domain.entity;

import com.lanchen.classweave.domain.enums.AccessRole;
import com.lanchen.classweave.domain.enums.AccessSource;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "schedule_accesses")
public class ScheduleAccessEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "schedule_id", nullable = false)
    private ScheduleEntity schedule;

    @Enumerated(EnumType.STRING)
    @Column(name = "access_role", nullable = false, length = 20)
    private AccessRole accessRole;

    @Enumerated(EnumType.STRING)
    @Column(name = "access_source", nullable = false, length = 20)
    private AccessSource accessSource;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "share_link_id")
    private ScheduleShareLinkEntity shareLink;

    @Column(name = "display_name_override", length = 100)
    private String displayNameOverride;

    @Column(name = "display_color", length = 20)
    private String displayColor;

    @Column(name = "display_opacity", nullable = false, precision = 4, scale = 2)
    private BigDecimal displayOpacity = new BigDecimal("0.85");

    @Column(name = "is_visible_default", nullable = false)
    private Boolean visibleDefault = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
