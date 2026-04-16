package com.novainvesa.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "import_jobs",
        indexes = {
                @Index(name = "idx_job_id", columnList = "job_id"),
                @Index(name = "idx_admin",  columnList = "admin_id"),
                @Index(name = "idx_status", columnList = "status")
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImportJob {

    public enum Status { PENDING, PROCESSING, COMPLETED, FAILED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "INT UNSIGNED")
    private Long id;

    /** UUID único del job de importación */
    @Column(name = "job_id", nullable = false, unique = true, length = 100)
    private String jobId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "admin_id", nullable = false)
    private AdminUser admin;

    @Column(nullable = false)
    @Builder.Default
    private Integer total = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer processed = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer published = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer drafts = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer errors = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Status status = Status.PENDING;

    /** JSON con el detalle de cada producto importado */
    @Column(columnDefinition = "json")
    private String results;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
