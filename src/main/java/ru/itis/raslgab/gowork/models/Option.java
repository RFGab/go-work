package ru.itis.raslgab.gowork.models;

import jakarta.persistence.*;
import lombok.*;
import ru.itis.raslgab.gowork.models.enums.OptionCategory;

// оборудование
@Entity
@Table(name = "options")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Option {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String name;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private OptionCategory category;
}
