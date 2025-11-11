package co.edu.uniquindio.gohost.integration;

import co.edu.uniquindio.gohost.dto.authDtos.LoginDTO;
import co.edu.uniquindio.gohost.dto.authDtos.TokenDTO;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AuthControllerIT extends IntegrationTestBase {

    @Test
    void registerHuesped_and_login_returns_token_and_role() throws Exception {
        String email = "it.huesped+" + java.util.UUID.randomUUID() + "@example.com";
        String password = "Aa123456";

        registerHuesped(email, password);

        LoginDTO loginDTO = new LoginDTO(email, password);
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andReturn();

        TokenDTO tokenDTO = objectMapper.readValue(result.getResponse().getContentAsString(), TokenDTO.class);
        assertThat(tokenDTO.token()).isNotBlank();
        assertThat(tokenDTO.rol()).isEqualTo("HUESPED");
        assertThat(tokenDTO.usuarioId()).isNotNull();
    }

    @Test
    void registerAnfitrion_and_login_returns_token_and_role() throws Exception {
        String email = "it.anfitrion+" + java.util.UUID.randomUUID() + "@example.com";
        String password = "Aa123456";

        registerAnfitrion(email, password);

        LoginDTO loginDTO = new LoginDTO(email, password);
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andReturn();

        TokenDTO tokenDTO = objectMapper.readValue(result.getResponse().getContentAsString(), TokenDTO.class);
        assertThat(tokenDTO.token()).isNotBlank();
        assertThat(tokenDTO.rol()).isEqualTo("ANFITRION");
        assertThat(tokenDTO.usuarioId()).isNotNull();
    }
}
