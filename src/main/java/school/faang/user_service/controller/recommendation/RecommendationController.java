package school.faang.user_service.controller.recommendation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import school.faang.user_service.dto.recommendation.RecommendationDto;
import school.faang.user_service.service.recommendation.RecommendationService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recommendations")
public class RecommendationController {
    private final RecommendationService recommendationService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<RecommendationDto> getAllRecommendations() {
        return recommendationService.getAllRecommendations();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public RecommendationDto getRecommendationById(@PathVariable Long id) {
        return recommendationService.getRecommendationById(id);
    }

    @GetMapping("/author/{authorId}")
    @ResponseStatus(HttpStatus.OK)
    public Page<RecommendationDto> getAllGivenRecommendations(@PathVariable Long authorId, Pageable pageable) {
        return recommendationService.getAllGivenRecommendations(authorId, pageable);
    }

    @GetMapping("/receiver/{receiverId}")
    @ResponseStatus(HttpStatus.OK)
    public Page<RecommendationDto> getAllUserRecommendations(@PathVariable Long receiverId, Pageable pageable) {
        return recommendationService.getAllUserRecommendations(receiverId, pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RecommendationDto createRecommendation(@Valid @RequestBody RecommendationDto recommendationDto) {
        return recommendationService.createRecommendation(recommendationDto);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public RecommendationDto updateRecommendation(@PathVariable Long id, @Valid @RequestBody RecommendationDto recommendationDto) {
        return recommendationService.updateRecommendation(id, recommendationDto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRecommendation(@PathVariable Long id) {
        recommendationService.deleteRecommendation(id);
    }
}
