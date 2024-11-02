package school.faang.user_service.controller.recommendation;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<RecommendationRequestDto> requestRecommendation(@Valid @RequestBody RecommendationRequestDto recommendationRequestDto) {
        RecommendationRequestDto newDto = recommendationRequestService.create(recommendationRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(newDto);
    }

    @PostMapping("/filter")
    public ResponseEntity<List<RecommendationRequestDto>> getRecommendationRequests(@Valid @RequestBody @NotNull RequestFilterDto filterDto) {
        List<RecommendationRequestDto> requestsDto = recommendationRequestService.getRequests(filterDto);
        return ResponseEntity.ok(requestsDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecommendationRequestDto> getRecommendationRequest(@PathVariable("id") @NotNull @Min(1) Long id) {
        RecommendationRequestDto requestDto = recommendationRequestService.getRequest(id);
        return ResponseEntity.ok(requestDto);
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<RejectionDto> rejectRequest(@PathVariable @NotNull @Min(1) Long id, @Valid @RequestBody @NotNull RejectionDto rejectionDto) {
        RejectionDto newDto = recommendationRequestService.rejectRequest(id, rejectionDto);
        return ResponseEntity.ok(newDto);
    }
}
