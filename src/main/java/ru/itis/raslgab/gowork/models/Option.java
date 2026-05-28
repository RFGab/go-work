package ru.itis.raslgab.gowork.models;

import jakarta.persistence.*;
import lombok.*;
import ru.itis.raslgab.gowork.models.enums.OptionCategory;

// оборудование
@Entity
@Table(name = "options", indexes = {
        @Index(name = "idx_option_name", columnList = "name", unique = true),
        @Index(name = "idx_option_category", columnList = "category")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Option {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String name;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private OptionCategory category;
}
