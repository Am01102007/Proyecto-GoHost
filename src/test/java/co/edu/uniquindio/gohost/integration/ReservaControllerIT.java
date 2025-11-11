package co.edu.uniquindio.gohost.integration;

import co.edu.uniquindio.gohost.dto.alojamientosDtos.CrearAlojDTO;
import co.edu.uniquindio.gohost.dto.reservaDtos.CrearReservaDTO;
import co.edu.uniquindio.gohost.model.ServicioAlojamiento;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ReservaControllerIT extends IntegrationTestBase {

    private String crearAlojamientoComoAnfitrion(String email, String password) throws Exception {
        registerAnfitrion(email, password);
        AuthSession session = login(email, password);
        CrearAlojDTO dto = new CrearAlojDTO(
                "Apto moderno",
                "Cerca al parque",
                "Armenia",
                "Colombia",
                "Calle 20 #10-15",
                "630002",
                new BigDecimal("150.00"),
                4,
                List.of("https://example.com/apto.jpg"),
                List.of(ServicioAlojamiento.WIFI)
        );
        MvcResult res = mockMvc.perform(post("/api/alojamientos")
                        .header("Authorization", "Bearer " + session.token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andReturn();
        var node = objectMapper.readTree(res.getResponse().getContentAsString());
        return node.get("id").asText();
    }

    @Test
    void huesped_can_create_list_and_cancel_reservation() throws Exception {
        // Setup alojamiento
        String hostEmail = "host.reserva+" + java.util.UUID.randomUUID() + "@example.com";
        String alojamientoIdStr = crearAlojamientoComoAnfitrion(hostEmail, "Aa123456");
        UUID alojamientoId = UUID.fromString(alojamientoIdStr);

        // Setup huesped
        String huespedEmail = "huesped.reserva+" + java.util.UUID.randomUUID() + "@example.com";
        String password = "Aa123456";
        registerHuesped(huespedEmail, password);
        AuthSession huesped = login(huespedEmail, password);

        // Crear reserva
        CrearReservaDTO crearReserva = new CrearReservaDTO(
                alojamientoId,
                null,
                LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(8),
                2
        );

        MvcResult crear = mockMvc.perform(post("/api/reservas")
                        .header("Authorization", "Bearer " + huesped.token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(crearReserva)))
                .andExpect(status().isOk())
                .andReturn();

        var resJson = objectMapper.readTree(crear.getResponse().getContentAsString());
        String reservaId = resJson.get("id").asText();
        assertThat(reservaId).isNotBlank();

        // Listar mis reservas
        mockMvc.perform(get("/api/reservas/mias")
                        .header("Authorization", "Bearer " + huesped.token))
                .andExpect(status().isOk());

        // Cancelar
        mockMvc.perform(post("/api/reservas/" + reservaId + "/cancelar")
                        .header("Authorization", "Bearer " + huesped.token))
                .andExpect(status().isNoContent());
    }
}
