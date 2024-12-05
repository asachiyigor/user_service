package school.faang.user_service.service.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import school.faang.user_service.controller.user.AvatarSize;
import school.faang.user_service.entity.User;
import school.faang.user_service.entity.UserProfilePic;
import school.faang.user_service.exception.DataValidationException;
import school.faang.user_service.repository.UserRepository;
import school.faang.user_service.service.s3.S3ServiceImpl;
import school.faang.user_service.service.s3.s3ResponseKey;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserAvatarServiceTest {
    @Mock
    private S3ServiceImpl s3Service;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private UserAvatarService userAvatarService;

    private User user;
    private school.faang.user_service.service.s3.s3ResponseKey s3ResponseKey;
    MultipartFile file;
    String regularKey, smallKey;

    @BeforeEach
    void setUp() {
        String fileName = "avatar.jpg";
        regularKey = "1testUser/" + fileName;
        smallKey = "1testUser/small_" + fileName;

        UserProfilePic userProfilePic = new UserProfilePic();
        userProfilePic.setFileId(regularKey);
        userProfilePic.setSmallFileId(smallKey);

        user = new User();
        user.setId(1L);
        user.setUsername("testUser");
        user.setUserProfilePic(userProfilePic);

        s3ResponseKey = new s3ResponseKey(regularKey, smallKey);

        file = new MockMultipartFile("file", fileName,
                "image/jpeg",
                "test image content".getBytes());
    }

    @Test
    void testAddAvatarNewImage() {
        user.setUserProfilePic(null);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(s3Service.uploadFile(file, "1testUser")).thenReturn(s3ResponseKey);
        when(userRepository.save(any(User.class))).thenReturn(user);

        school.faang.user_service.service.s3.s3ResponseKey result = userAvatarService.addAvatar(1L, file);

        verify(s3Service, times(1)).uploadFile(file, "1testUser");
        verify(userRepository, times(1)).save(user);
        assertEquals(s3ResponseKey.regularKey(), result.regularKey());
        assertEquals(s3ResponseKey.smallKey(), result.smallKey());
    }

    @Test
    void testAddAvatarUpdateImage() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(s3Service.uploadFile(file, "1testUser")).thenReturn(s3ResponseKey);
        when(userRepository.save(any(User.class))).thenReturn(user);

        school.faang.user_service.service.s3.s3ResponseKey result = userAvatarService.addAvatar(1L, file);

        verify(s3Service, times(1)).deleteFile(regularKey);
        verify(s3Service, times(1)).deleteFile(smallKey);
        verify(s3Service, times(1)).uploadFile(file, "1testUser");
        verify(userRepository, times(1)).save(user);
        assertEquals(s3ResponseKey.regularKey(), result.regularKey());
        assertEquals(s3ResponseKey.smallKey(), result.smallKey());
    }

    @Test
    void testGetAvatar() {
        InputStream inputStream = new ByteArrayInputStream("avatar content".getBytes());
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(s3Service.downloadFile(anyString())).thenReturn(inputStream);

        InputStream resultReg = userAvatarService.getAvatar(1L, AvatarSize.REGULAR);
        InputStream resultSmall = userAvatarService.getAvatar(1L, AvatarSize.SMALL);

        assertNotNull(resultReg);
        verify(s3Service, times(1)).downloadFile(regularKey);

        assertNotNull(resultSmall);
        verify(s3Service, times(1)).downloadFile(smallKey);
    }

    @Test
    void testGetAvatarNotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());
        DataValidationException exception = assertThrows(DataValidationException.class, () -> {
            userAvatarService.getAvatar(1L, AvatarSize.REGULAR);
        });
        assertEquals("User not found. User id: 1", exception.getMessage());
    }

    @Test
    void testGetAvatarUserProfilePicDoesNotExist() {
        user.setUserProfilePic(null);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

        DataValidationException exception = assertThrows(DataValidationException.class, () -> {
            userAvatarService.getAvatar(1L, AvatarSize.REGULAR);
        });
        assertEquals("Profile pictures do not exist for the user: testUser", exception.getMessage());
    }

    @Test
    void testGetAvatarUserMainPicDoesNotExist() {
        user.getUserProfilePic().setFileId(null);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

        DataValidationException exception = assertThrows(DataValidationException.class, () -> {
            userAvatarService.getAvatar(1L, AvatarSize.REGULAR);
        });
        assertEquals("Main profile picture is missing for user testUser", exception.getMessage());
    }

    @Test
    void testGetAvatarUserSmallPicDoesNotExist() {
        user.getUserProfilePic().setSmallFileId(null);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

        DataValidationException exception = assertThrows(DataValidationException.class, () -> {
            userAvatarService.getAvatar(1L, AvatarSize.SMALL);
        });
        assertEquals("Small profile picture is missing for user testUser", exception.getMessage());
    }

    @Test
    void testDeleteAvatar() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

        userAvatarService.deleteAvatar(1L);

        verify(s3Service, times(1)).deleteFile(regularKey);
        verify(s3Service, times(1)).deleteFile(smallKey);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testDeleteAvatarNotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());
        DataValidationException exception = assertThrows(DataValidationException.class, () -> {
            userAvatarService.deleteAvatar(1L);
        });
        assertEquals("User not found. User id: 1", exception.getMessage());
    }
}