package co.edu.uniquindio.gohost.integration;

import co.edu.uniquindio.gohost.dto.authDtos.LoginDTO;
import co.edu.uniquindio.gohost.dto.authDtos.RegistroDTO;
import co.edu.uniquindio.gohost.dto.authDtos.TokenDTO;
import co.edu.uniquindio.gohost.model.TipoDocumento;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public abstract class IntegrationTestBase {

    @Autowired protected MockMvc mockMvc;
    @Autowired protected ObjectMapper objectMapper;

    protected static class AuthSession {
        public final String token;
        public final UUID usuarioId;
        public final String rol;

        public AuthSession(String token, UUID usuarioId, String rol) {
            this.token = token;
            this.usuarioId = usuarioId;
            this.rol = rol;
        }
    }

    @BeforeEach
    void setup() {
        assertThat(mockMvc).isNotNull();
        assertThat(objectMapper).isNotNull();
    }

    protected RegistroDTO buildRegistro(String email, String password) {
        return new RegistroDTO(
                email,
                "Test",
                "User",
                TipoDocumento.CC,
                UUID.randomUUID().toString().substring(0, 8),
                LocalDate.of(1990, 1, 1),
                "3001234567",
                "Armenia",
                "Colombia",
                password
        );
    }

    protected void registerHuesped(String email, String password) throws Exception {
        RegistroDTO dto = buildRegistro(email, password);
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    protected void registerAnfitrion(String email, String password) throws Exception {
        RegistroDTO dto = buildRegistro(email, password);
        mockMvc.perform(post("/api/auth/register/anfitrion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    protected AuthSession login(String email, String password) throws Exception {
        LoginDTO dto = new LoginDTO(email, password);
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andReturn();

        TokenDTO tokenDTO = objectMapper.readValue(result.getResponse().getContentAsString(), TokenDTO.class);
        assertThat(tokenDTO.token()).isNotBlank();
        return new AuthSession(tokenDTO.token(), tokenDTO.usuarioId(), tokenDTO.rol());
    }
}

