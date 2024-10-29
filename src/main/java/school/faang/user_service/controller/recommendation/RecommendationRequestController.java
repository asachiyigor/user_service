package school.faang.user_service.controller.recommendation;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import school.faang.user_service.dto.RecommendationRequestDto;
import school.faang.user_service.service.RecommendationRequestService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/recommendation-requests")
@Validated
public class RecommendationRequestController {


    private final RecommendationRequestService recommendationRequestService;

    public RecommendationRequestDto requestRecommendation(@RequestBody @NotNull @Valid RecommendationRequestDto recommendationRequestDto) {
        return recommendationRequestService.create(recommendationRequestDto);
    }
}
