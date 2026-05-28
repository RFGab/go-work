package ru.itis.raslgab.gowork.models;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "files", indexes = {
        @Index(name = "idx_file_path", columnList = "file_path", unique = true),
        @Index(name = "idx_file_type", columnList = "file_type")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class FileInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false)
    private String originalFileName;

    @Column(nullable = false)
    private String storageFileName;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private Long size;

    @Column(nullable = false)
    private String url;

}