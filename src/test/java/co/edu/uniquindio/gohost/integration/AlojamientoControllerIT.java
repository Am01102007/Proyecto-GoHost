package co.edu.uniquindio.gohost.integration;

import co.edu.uniquindio.gohost.dto.alojamientosDtos.CrearAlojDTO;
import co.edu.uniquindio.gohost.model.ServicioAlojamiento;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AlojamientoControllerIT extends IntegrationTestBase {

    private CrearAlojDTO sampleCrearAloj() {
        return new CrearAlojDTO(
                "Casa centro",
                "Cómodo alojamiento en el centro",
                "Armenia",
                "Colombia",
                "Calle 10 #20-30",
                "630001",
                new BigDecimal("120.00"),
                3,
                List.of("https://example.com/img1.jpg"),
                List.of(ServicioAlojamiento.WIFI, ServicioAlojamiento.COCINA)
        );
    }

    @Test
    void host_can_create_and_fetch_metrics() throws Exception {
        String email = "host.metrics+" + java.util.UUID.randomUUID() + "@example.com";
        String password = "Aa123456";
        registerAnfitrion(email, password);
        AuthSession session = login(email, password);

        CrearAlojDTO dto = sampleCrearAloj();
        MvcResult crearRes = mockMvc.perform(post("/api/alojamientos")
                        .header("Authorization", "Bearer " + session.token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andReturn();

        // Extract alojamientoId from response JSON
        var json = crearRes.getResponse().getContentAsString();
        var node = objectMapper.readTree(json);
        var alojamientoId = node.get("id").asText();
        assertThat(alojamientoId).isNotEmpty();

        mockMvc.perform(get("/api/alojamientos/" + alojamientoId + "/metricas")
                        .header("Authorization", "Bearer " + session.token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/alojamientos/metricas")
                        .header("Authorization", "Bearer " + session.token))
                .andExpect(status().isOk());
    }
}
