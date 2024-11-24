package school.faang.user_service.controller.user;

import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import school.faang.user_service.service.s3.s3ResponseKey;
import school.faang.user_service.service.user.UserAvatarService;

import java.io.IOException;
import java.io.InputStream;

@Api(tags = "User Avatar API")
@RequiredArgsConstructor
@RestController
@Tag(name = "User Avatar API", description = "API for managing users' Avatars")
@RequestMapping("/users")
public class UserAvatarController {
    private final UserAvatarService userAvatarService;

    @Operation(
            summary = "Upload user avatar",
            description = "Uploads an avatar for the specified user. The uploaded file should be a valid image in `multipart/form-data` format."
    )
    @PutMapping(path = "/{id}/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public s3ResponseKey uploadAvatar(@PathVariable Long id,
                                      @RequestParam("file") MultipartFile file) {
        return userAvatarService.addAvatar(id, file);
    }

    @Operation(
            summary = "Get user avatar",
            description = "Retrieve the avatar of a user by their ID and the specified size (REGULAR or SMALL)."
    )
    @GetMapping("/{id}/avatar/{size}")
    public ResponseEntity<byte[]> getAvatar(@PathVariable Long id, @PathVariable AvatarSize size) {
        try (InputStream inputStream = userAvatarService.getAvatar(id, size)) {
            byte[] avatarContent = inputStream.readAllBytes();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"avatar-" + id + "-" + size.name().toLowerCase() + ".jpg\"")
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(avatarContent);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error while retrieving user avatar", e);
        }
    }

    @Operation(
            summary = "Delete user avatar",
            description = "Deletes the avatar of the specified user by their ID."
    )
    @DeleteMapping(path = "/{id}/avatar")
    public void deleteAvatar(@PathVariable Long id) {
        userAvatarService.deleteAvatar(id);
    }
}