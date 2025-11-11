package co.edu.uniquindio.gohost.integration;

import co.edu.uniquindio.gohost.service.image.ImageService;
import co.edu.uniquindio.gohost.service.image.ImageUploadResult;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ImageControllerIT extends IntegrationTestBase {

    @MockBean
    private ImageService imageService;

    @Test
    void authenticated_user_can_upload_and_delete_image() throws Exception {
        String email = "img.user+" + java.util.UUID.randomUUID() + "@example.com";
        String password = "Aa123456";
        registerHuesped(email, password);
        AuthSession session = login(email, password);

        // Mock ImageService behavior
        when(imageService.subirImagen(any())).thenReturn(
                new ImageUploadResult(
                        "gohost/imagenes/demo123",
                        "http://cloud.example/demo.jpg",
                        "https://cloud.example/demo.jpg",
                        800,
                        600,
                        "jpg",
                        12345L
                )
        );
        doNothing().when(imageService).eliminarImagen(anyString());

        MockMultipartFile file = new MockMultipartFile(
                "file", "foto.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[]{1,2,3}
        );

        mockMvc.perform(multipart("/api/images")
                        .file(file)
                        .header("Authorization", "Bearer " + session.token))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/api/images/public-id-demo")
                        .header("Authorization", "Bearer " + session.token))
                .andExpect(status().isNoContent());
    }
}
