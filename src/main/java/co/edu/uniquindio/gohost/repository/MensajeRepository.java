package co.edu.uniquindio.gohost.repository;

import co.edu.uniquindio.gohost.model.Mensaje;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface MensajeRepository extends JpaRepository<Mensaje, UUID> {

    @Query("""
        SELECT m FROM Mensaje m
        JOIN FETCH m.remitente rem
        JOIN FETCH m.reserva r
        JOIN FETCH r.alojamiento a
        JOIN FETCH a.anfitrion an
        WHERE r.id = :reservaId
        ORDER BY m.creadoEn ASC
    """)
    Page<Mensaje> findByReserva(@Param("reservaId") UUID reservaId, Pageable pageable);
}
