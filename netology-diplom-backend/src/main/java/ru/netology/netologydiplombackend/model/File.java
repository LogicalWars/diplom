package ru.netology.netologydiplombackend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "files")
public class File {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String filename;

    @Column
    private String hash;

    @Column
    private String contentType;

    @Column
    private Long size;

    @Column
    private byte[] data;

    @Column
    private Timestamp uploadedAt;

    @ManyToOne
    @JoinColumn
    private User user;

    @PrePersist
    protected void onCreate() {
        this.uploadedAt = Timestamp.valueOf(LocalDateTime.now());
    }
}
