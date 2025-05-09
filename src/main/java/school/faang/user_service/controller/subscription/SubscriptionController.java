package school.faang.user_service.controller.subscription;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import school.faang.user_service.dto.user.UserDto;
import school.faang.user_service.dto.user.UserFilterDto;
import school.faang.user_service.exception.DataValidationException;
import school.faang.user_service.service.subscription.SubscriptionService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @Autowired
    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @PostMapping("subscriptions/users/{followeeId}")
    public void followUser(@RequestHeader("x-user-id") long followerId,
                           @PathVariable long followeeId) {
        if (followerId == followeeId) {
            throw new DataValidationException("Cannot follow yourself");
        }
        subscriptionService.followUser(followerId, followeeId);
    }

    @DeleteMapping("/unfollow/{followeeId}")
    public void unfollowUser(@RequestHeader("x-user-id") Long followerId, @PathVariable long followeeId) {
        if (followerId == followeeId) {
            throw new DataValidationException("Cannot unfollow yourself");
        }
        subscriptionService.unfollowUser(followerId, followeeId);
    }

    @GetMapping("/followers/{followeeId}")
    public List<UserDto> getFollowers(
            @PathVariable long followeeId,
            @RequestBody(required = false) UserFilterDto filter) {
        return subscriptionService.getFollowers(followeeId, filter);
    }

    @GetMapping("/following/{followerId}")
    public List<UserDto> getFollowing(
            @PathVariable long followerId,
            @RequestBody(required = false) UserFilterDto filter) {
        return subscriptionService.getFollowing(followerId, filter);
    }

    @GetMapping("/followers/count/{followeeId}")
    public long getFollowersCount(@PathVariable long followeeId) {
        return subscriptionService.getFollowersCount(followeeId);
    }

    @GetMapping("/following/count/{followerId}")
    public long getFollowingCount(@PathVariable long followerId) {
        return subscriptionService.getFollowingCount(followerId);
    }
}