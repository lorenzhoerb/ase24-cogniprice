package ase.cogniprice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification")
@Data
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // possibly make it longer
    @Column(name = "info_text", nullable = false, length = 2056)
    private String infoText;

    @Column(name = "seen", nullable = false)
    private boolean seen;

    @ManyToOne
    @JoinColumn(name = "application_user_id", nullable = false)
    private ApplicationUser applicationUser;




}
