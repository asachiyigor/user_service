package school.faang.user_service.service.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import school.faang.user_service.exception.ErrorMessage;
import school.faang.user_service.exception.FileException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3ServiceImpl implements S3Service {

    public final AmazonS3 s3Client;

    @Value("${services.s3.bucketName}")
    private String bucketName;

    private static final long MAX_FILE_SIZE_MB = 5 * 1024 * 1024;
    private static final int MAX_BIG_SIDE_REGULAR = 1080;
    private static final int MAX_BIG_SIDE_SMALL = 170;

    @Override
    public s3ResponseKey uploadFile(MultipartFile file, String folder) {
        validateFile(file);
        try {
            BufferedImage originalImage = ImageIO.read(file.getInputStream());
            BufferedImage regularImage = resizeImage(originalImage, MAX_BIG_SIDE_REGULAR);
            BufferedImage smallImage = resizeImage(originalImage, MAX_BIG_SIDE_SMALL);

            String fileNameKey = folder + "/" + file.getOriginalFilename();
            String fileNameKeySmall = folder + "/small_" + file.getOriginalFilename();

            uploadResizedImageToS3(regularImage, file.getContentType(), fileNameKey);
            uploadResizedImageToS3(smallImage, file.getContentType(), fileNameKeySmall);

            log.info("File uploaded successfully: regular={}, small={}", fileNameKey, fileNameKeySmall);
            return new s3ResponseKey(fileNameKey, fileNameKeySmall);
        } catch (IOException e) {
            log.error("Error processing image file: {}", e.getMessage(), e);
            throw new FileException("Error processing the uploaded file.");
        }
    }

    @Override
    public void deleteFile(String key) {
        log.info("Deleting file with key: {}", key);
        s3Client.deleteObject(bucketName, key);
    }

    @Override
    public InputStream downloadFile(String key) {
        try {
            S3Object s3Object = s3Client.getObject(bucketName, key);
            return s3Object.getObjectContent();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new FileException(String.format(ErrorMessage.FILE_ERROR, key));
        }
    }

    public void validateFile(MultipartFile file) {
        if (file.getSize() > MAX_FILE_SIZE_MB) {
            throw new FileException("File size exceeds the maximum allowed size of 5MB.");
        }

        try {
            BufferedImage image = ImageIO.read(file.getInputStream());
            if (image == null) {
                throw new FileException("The uploaded file is not a valid image.");
            }
        } catch (IOException e) {
            throw new FileException("Error reading the file. Ensure it is a valid image.");
        }
    }

    private BufferedImage resizeImage(BufferedImage originalImage, int maxSize) throws IOException {
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();

        if (width <= maxSize && height <= maxSize) {
            return originalImage;
        }

        if (width > height) {
            return Thumbnails.of(originalImage).size(maxSize, (int) ((double) height / width * maxSize)).asBufferedImage();
        } else {
            return Thumbnails.of(originalImage).size((int) ((double) width / height * maxSize), maxSize).asBufferedImage();
        }
    }

    private void uploadResizedImageToS3(BufferedImage image, String contentType, String key) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(image, getImageFormat(contentType), os);

        byte[] byteArray = os.toByteArray();
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(byteArray.length);
        metadata.setContentType(contentType);

        ByteArrayInputStream inputStream = new ByteArrayInputStream(byteArray);
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, key, inputStream, metadata);
        s3Client.putObject(putObjectRequest);
    }

    private String getImageFormat(String contentType) {
        return switch (contentType) {
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            default -> throw new FileException("Unsupported image format. Only JPEG and PNG are allowed.");
        };
    }
}
