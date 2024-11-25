package school.faang.user_service.service.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import school.faang.user_service.exception.FileException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class S3ServiceImplTest {
    @Mock
    private AmazonS3 s3Client;
    @InjectMocks
    private S3ServiceImpl s3Service;

    private final String bucketName = "corpbucket";

    private MultipartFile mockFile;

    @BeforeEach
    void setUp() throws IOException, NoSuchFieldException, IllegalAccessException {
        Field bucketNameField = S3ServiceImpl.class.getDeclaredField("bucketName");
        bucketNameField.setAccessible(true);
        bucketNameField.set(s3Service, bucketName);

        BufferedImage bufferedImage = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "jpg", os);
        byte[] content = os.toByteArray();
        mockFile = new MockMultipartFile(
                "file",
                "avatar.jpg",
                "image/jpeg",
                new ByteArrayInputStream(content));
    }

    @Test
    void testUploadFile() {
        when(s3Client.putObject(any(PutObjectRequest.class))).thenReturn(null);

        s3ResponseKey result = s3Service.uploadFile(mockFile, "folder");

        verify(s3Client, times(2)).putObject(any(PutObjectRequest.class));
        assertNotNull(result);
        assertEquals("folder/avatar.jpg", result.regularKey());
        assertEquals("folder/small_avatar.jpg", result.smallKey());
    }

    @Test
    void testUploadFileExceedsSizeLimit() {
        MultipartFile largeFile = new MockMultipartFile("file",
                "test.jpg",
                "image/jpeg", new byte[(5 * 1024 * 1024) + 1]);

        FileException exception = assertThrows(FileException.class, () -> s3Service.uploadFile(largeFile, "foder"));
        assertEquals("File size exceeds the maximum allowed size of 5MB.", exception.getMessage());
    }

    @Test
    void testUploadFileUnsupportedFormat() {
        MockMultipartFile gifFile = new MockMultipartFile("file",
                "test.txt",
                "text/plain",
                "not an image".getBytes());

        FileException exception = assertThrows(FileException.class, () -> s3Service.uploadFile(gifFile, "folder"));
        assertEquals("The uploaded file is not a valid image.", exception.getMessage());
    }

    @Test
    void testDeleteFile() {
        doNothing().when(s3Client).deleteObject(bucketName, "testKey");
        s3Service.deleteFile("testKey");
        verify(s3Client, times(1)).deleteObject(bucketName, "testKey");
    }

    @Test
    void testDownloadFile() {
        S3Object s3Object = mock(S3Object.class);
        S3ObjectInputStream s3ObjectInputStream = mock(S3ObjectInputStream.class);
        when(s3Object.getObjectContent()).thenReturn(s3ObjectInputStream);
        when(s3Client.getObject(anyString(), anyString())).thenReturn(s3Object);

        InputStream result = s3Service.downloadFile("testKey");

        verify(s3Client, times(1)).getObject(bucketName, "testKey");
        assertNotNull(result);
    }

    @Test
    void testDownloadFileThrowsException() {
        when(s3Client.getObject(anyString(), anyString())).thenThrow(new RuntimeException("S3 error"));

        FileException exception = assertThrows(FileException.class,
                () -> s3Service.downloadFile("testKey"));
        verify(s3Client, times(1)).getObject(bucketName, "testKey");
        assertEquals("Error occurred while dealing with file: testKey", exception.getMessage());
    }

    @Test
    void testValidateFileSizeExceedsLimit() {
        MultipartFile largeFile = new MockMultipartFile("file",
                "test.jpg",
                "image/jpeg", new byte[(5 * 1024 * 1024) + 1]);

        FileException exception = assertThrows(FileException.class, () -> s3Service.validateFile(largeFile));
        assertEquals("File size exceeds the maximum allowed size of 5MB.", exception.getMessage());
    }

    @Test
    public void testValidateFileNotAnImage() throws IOException {
        MultipartFile mockFile = Mockito.mock(MultipartFile.class);
        when(mockFile.getSize()).thenReturn(10L);
        when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));

        Exception exception = assertThrows(FileException.class, () -> s3Service.validateFile(mockFile));
        assert exception.getMessage().contains("The uploaded file is not a valid image.");
    }

    @Test
    public void testErrorReadingFile() throws IOException {
        MultipartFile mockFile = Mockito.mock(MultipartFile.class);
        when(mockFile.getSize()).thenReturn(10L);
        when(mockFile.getInputStream()).thenThrow(new IOException("IO error"));

        Exception exception = assertThrows(FileException.class, () -> s3Service.validateFile(mockFile));
        assert exception.getMessage().contains("Error reading the file. Ensure it is a valid image.");
    }
}