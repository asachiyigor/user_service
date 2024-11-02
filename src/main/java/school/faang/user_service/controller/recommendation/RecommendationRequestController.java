package school.faang.user_service.controller.recommendation;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import school.faang.user_service.dto.recommendation.RecommendationRequestDto;
import school.faang.user_service.dto.recommendation.RejectionDto;
import school.faang.user_service.dto.recommendation.RequestFilterDto;
import school.faang.user_service.service.recommendation.RecommendationRequestService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user-service/recommendation-requests")
@Validated
public class RecommendationRequestController {
    private final RecommendationRequestService recommendationRequestService;

    @PostMapping("/create")
    public RecommendationRequestDto requestRecommendation(@RequestBody @NotNull RecommendationRequestDto recommendationRequestDto) {
        return recommendationRequestService.create(recommendationRequestDto);
    }

    @PostMapping("/filter")
    public List<RecommendationRequestDto> getRecommendationRequests(@RequestBody @NotNull RequestFilterDto filterDto) {
        return recommendationRequestService.getRequests(filterDto);
    }

    @GetMapping("/{id}")
    public RecommendationRequestDto getRecommendationRequest(@PathVariable @Min(1) Long id) {
        return recommendationRequestService.getRequest(id);
    }

    @PostMapping("/{id}/reject")
    public RejectionDto rejectRequest(@PathVariable @Min(1) Long id, @RequestBody @NotNull RejectionDto rejectionDto) {
        return recommendationRequestService.rejectRequest(id, rejectionDto);
    }
}
