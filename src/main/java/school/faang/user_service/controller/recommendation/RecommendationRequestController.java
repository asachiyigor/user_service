package school.faang.user_service.controller.recommendation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import school.faang.user_service.dto.recommendation.RecommendationRequestDto;
import school.faang.user_service.dto.recommendation.RejectionDto;
import school.faang.user_service.dto.recommendation.RequestFilterDto;
import school.faang.user_service.service.recommendation.RecommendationRequestService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class RecommendationRequestController {
    private final RecommendationRequestService recommendationRequestService;

    @PostMapping("/recommendation-requests/create")
    public RecommendationRequestDto requestRecommendation(@RequestBody RecommendationRequestDto recommendationRequestDto) {
        if (recommendationRequestDto.getMessage() == null || recommendationRequestDto.getMessage().isEmpty()) {
            log.error("Recommendation request message cannot be empty");
            throw new IllegalArgumentException("Recommendation request message cannot be empty");
        }
        return recommendationRequestService.create(recommendationRequestDto);
    }

    @PostMapping("/recommendation-requests/filter")
    public List<RecommendationRequestDto> getRecommendationRequests(@RequestBody RequestFilterDto filterDto) {
        return recommendationRequestService.getRequests(filterDto);
    }

    @GetMapping("/recommendation-requests/{id}")
    public RecommendationRequestDto getRecommendationRequest(@PathVariable Long id) {
        return recommendationRequestService.getRequest(id);
    }

    @PostMapping("/recommendation-requests/{id}/reject")
    public RejectionDto rejectRequest(@PathVariable Long id, @RequestBody RejectionDto rejectionDto) {
        return recommendationRequestService.rejectRequest(id, rejectionDto);
    }




}
