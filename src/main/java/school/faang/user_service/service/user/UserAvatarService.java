package school.faang.user_service.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import school.faang.user_service.controller.user.AvatarSize;
import school.faang.user_service.entity.User;
import school.faang.user_service.entity.UserProfilePic;
import school.faang.user_service.exception.DataValidationException;
import school.faang.user_service.repository.UserRepository;
import school.faang.user_service.service.s3.s3ResponseKey;
import school.faang.user_service.service.s3.S3ServiceImpl;

import java.io.InputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserAvatarService {

    private final S3ServiceImpl s3Service;
    private final UserRepository userRepository;

    public s3ResponseKey addAvatar(Long id, MultipartFile file) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new DataValidationException("User not found. User id: " + id));

        UserProfilePic userProfilePicExisting = user.getUserProfilePic();
        if (userProfilePicExisting != null) {
            s3Service.deleteFile(userProfilePicExisting.getFileId());
            s3Service.deleteFile(userProfilePicExisting.getSmallFileId());
        }
        String folder = user.getId() + user.getUsername();
        s3ResponseKey s3ResponseKey = s3Service.uploadFile(file, folder);
        UserProfilePic userProfilePic = new UserProfilePic();
        userProfilePic.setFileId(s3ResponseKey.regularKey());
        userProfilePic.setSmallFileId(s3ResponseKey.smallKey());
        user.setUserProfilePic(userProfilePic);
        userRepository.save(user);

        return s3ResponseKey;
    }

    public InputStream getAvatar(Long id, AvatarSize size) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new DataValidationException("User not found. User id: " + id));
        validateUserProfilePicsExist(user, size);
        String fileId = size == AvatarSize.SMALL
                ? user.getUserProfilePic().getSmallFileId()
                : user.getUserProfilePic().getFileId();
        return s3Service.downloadFile(fileId);
    }

    public void deleteAvatar(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new DataValidationException("User not found. User id: " + id));

        if (user.getUserProfilePic() != null && user.getUserProfilePic().getFileId() != null) {
            String fileId = user.getUserProfilePic().getFileId();
            s3Service.deleteFile(fileId);
            user.getUserProfilePic().setFileId(null);
        }

        if (user.getUserProfilePic() != null && user.getUserProfilePic().getSmallFileId() != null) {
            String smallFileId = user.getUserProfilePic().getSmallFileId();
            s3Service.deleteFile(smallFileId);
            user.getUserProfilePic().setSmallFileId(null);
        }
        userRepository.save(user);
    }

    private void validateUserProfilePicsExist(User user, AvatarSize size) {
        String username = user.getUsername();
        if (user.getUserProfilePic() == null) {
            throw new DataValidationException("Profile pictures do not exist for the user: " + username);
        }
        if (size == AvatarSize.REGULAR && (user.getUserProfilePic().getFileId() == null || user.getUserProfilePic().getFileId().isEmpty())) {
            throw new DataValidationException("Main profile picture is missing for user " + username);
        }
        if (size == AvatarSize.SMALL && (user.getUserProfilePic().getSmallFileId() == null || user.getUserProfilePic().getSmallFileId().isEmpty())) {
            throw new DataValidationException("Small profile picture is missing for user " + username);
        }
    }
}