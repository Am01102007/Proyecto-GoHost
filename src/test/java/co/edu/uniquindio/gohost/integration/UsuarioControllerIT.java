package co.edu.uniquindio.gohost.integration;

import co.edu.uniquindio.gohost.dto.usuarioDtos.ConfirmarResetPasswordDTO;
import co.edu.uniquindio.gohost.dto.usuarioDtos.ResetPasswordDTO;
import co.edu.uniquindio.gohost.service.mail.MailService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UsuarioControllerIT extends IntegrationTestBase {

    @MockBean
    private MailService mailService;

    // objectMapper is provided by IntegrationTestBase

    @Test
    void reset_and_confirm_password_flow_with_mocked_email() throws Exception {
        String email = "user.reset+" + java.util.UUID.randomUUID() + "@example.com";
        String oldPassword = "Aa123456";
        registerHuesped(email, oldPassword);

        final String[] capturedHtml = {null};
        doAnswer(invocation -> {
            // args: to, subject, html
            capturedHtml[0] = invocation.getArgument(2);
            return null;
        }).when(mailService).sendMail(anyString(), anyString(), anyString());

        ResetPasswordDTO resetDto = new ResetPasswordDTO(email);
        mockMvc.perform(post("/api/usuarios/password/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resetDto)))
                .andExpect(status().isAccepted());

        assertThat(capturedHtml[0]).isNotNull();
        // Extract 6-digit code from HTML
        Matcher m = Pattern.compile("<h1[^>]*>(\\d{6})</h1>").matcher(capturedHtml[0]);
        assertThat(m.find()).isTrue();
        String code = m.group(1);

        ConfirmarResetPasswordDTO confirmDto = new ConfirmarResetPasswordDTO(code, "Bb123456");
        mockMvc.perform(put("/api/usuarios/password/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(confirmDto)))
                .andExpect(status().isOk());

        // Login must work with new password
        AuthSession session = login(email, "Bb123456");
        assertThat(session.token).isNotBlank();
    }
}
