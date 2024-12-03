package school.faang.user_service.service.s3;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public interface S3Service {
    s3ResponseKey uploadFile(MultipartFile file, String folder);

    void deleteFile(String key);

    InputStream downloadFile(String key);
}