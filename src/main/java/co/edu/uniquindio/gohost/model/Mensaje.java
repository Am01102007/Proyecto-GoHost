package co.edu.uniquindio.gohost.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "mensaje")
public class Mensaje {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "reserva_id", nullable = false)
    private Reserva reserva;

    @ManyToOne(optional = false)
    @JoinColumn(name = "remitente_id", nullable = false)
    private Usuario remitente;

    @Column(name = "contenido", nullable = false, length = 2000)
    private String contenido;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;
}

