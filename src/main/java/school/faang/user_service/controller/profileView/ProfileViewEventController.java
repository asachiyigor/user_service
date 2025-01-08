package school.faang.user_service.controller.profileView;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import school.faang.user_service.service.profileView.ProfileViewEventService;

@RestController
@RequestMapping("/profiles")
@RequiredArgsConstructor
public class ProfileViewEventController {
    private final ProfileViewEventService profileViewEventService;

    @PostMapping("/{id}/view")
    public void viewProfile(@PathVariable("id") Long userId,
                            @RequestParam("userIdViewing") Long userIdViewing) {
        profileViewEventService.viewUserProfile(userId, userIdViewing);
    }
}
