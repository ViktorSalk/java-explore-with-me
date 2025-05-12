package ru.practicum.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@EqualsAndHashCode
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Stat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long statId;

    @Column(name = "app")
    private String app;

    @Column(name = "ip")
    private String ip;

    @Column(name = "time_stamp")
    private LocalDateTime timestamp;

    @Column(name = "uri")
    private String uri;
}