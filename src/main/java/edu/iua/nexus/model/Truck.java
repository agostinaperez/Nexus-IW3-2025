package edu.iua.nexus.model;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "trucks")
@Inheritance(strategy = InheritanceType.JOINED)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Truck {
    @Schema(hidden = true) //esto lo pongo para q cuando el swagger me haga la documentación, no se incluya el id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(length = 100, unique = true, nullable = false)
    private String licensePlate;

    private String description;

    @OneToMany(mappedBy = "truck",cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Tank> tankers = new HashSet<>();
}
