package school.faang.user_service.controller.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;
import school.faang.user_service.service.s3.s3ResponseKey;
import school.faang.user_service.service.user.UserAvatarService;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserAvatarController.class)
@ContextConfiguration(classes = {UserAvatarController.class})
class UserAvatarControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UserAvatarService userAvatarService;
    private s3ResponseKey responseKeyDTO;

    @BeforeEach
    void setUp() {
        responseKeyDTO = new s3ResponseKey(
                "1UserName/avatar.jpg",
                "1UserName/small_avatar.jpg");
    }

    @Test
    void testUploadAvatar() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes());

        when(userAvatarService.addAvatar(anyLong(), any(MultipartFile.class))).thenReturn(responseKeyDTO);

        mockMvc.perform(multipart("/users/1/avatar").file(file).with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.regularKey").value("1UserName/avatar.jpg"))
                .andExpect(jsonPath("$.smallKey").value("1UserName/small_avatar.jpg"));
    }

    @Test
    void testGetAvatar() throws Exception {
        byte[] avatarContent = "avatar content".getBytes();
        InputStream inputStream = new ByteArrayInputStream(avatarContent);

        when(userAvatarService.getAvatar(anyLong(), any(AvatarSize.class))).thenReturn(inputStream);

        mockMvc.perform(get("/users/1/avatar/REGULAR")
                        .contentType(MediaType.IMAGE_JPEG))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"avatar-1-regular.jpg\""))
                .andExpect(content().contentType(MediaType.IMAGE_JPEG))
                .andExpect(content().bytes(avatarContent));
    }

    @Test
    void testDeleteAvatar() throws Exception {
        Long momentId = 1L;
        mockMvc.perform(delete("/users/{id}/avatar", momentId))
                .andExpect(status().isOk());
    }
}