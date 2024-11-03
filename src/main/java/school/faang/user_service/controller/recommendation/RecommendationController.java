package school.faang.user_service.controller.recommendation;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import school.faang.user_service.dto.recommendation.RecommendationDto;
import school.faang.user_service.service.RecommendationService;

import java.util.List;

@Component
@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {
    @Autowired
    private RecommendationService recommendationService;

    @GetMapping
    public ResponseEntity<List<RecommendationDto>> getAllRecommendations() {
        List<RecommendationDto> recommendations = recommendationService.getAllRecommendations();
        return new ResponseEntity<>(recommendations, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecommendationDto> getRecommendationById(@PathVariable Long id) {
        RecommendationDto recommendation = recommendationService.getRecommendationById(id);
        return new ResponseEntity<>(recommendation, HttpStatus.OK);
    }

    @GetMapping("/author/{authorId}")
    public Page<RecommendationDto> getAllGivenRecommendations(@PathVariable Long authorId, Pageable pageable) {
        return recommendationService.getAllGivenRecommendations(authorId, pageable);
    }

    @GetMapping("/reciever/{receiverId}")
    public Page<RecommendationDto> getAllUserRecommendations(@PathVariable Long receiverId, Pageable pageable) {
        return recommendationService.getAllUserRecommendations(receiverId, pageable);
    }

    @PostMapping
    public ResponseEntity<RecommendationDto> createRecommendation(@Valid @RequestBody RecommendationDto recommendationDto) {
        RecommendationDto createdRecommendation = recommendationService.createRecommendation(recommendationDto);
        return new ResponseEntity<>(createdRecommendation, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RecommendationDto> updateRecommendation(@PathVariable Long id, @Valid @RequestBody RecommendationDto recommendationDto) {
        RecommendationDto updatedRecommendation = recommendationService.updateRecommendation(id, recommendationDto);
        return new ResponseEntity<>(updatedRecommendation, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecommendation(@PathVariable Long id) {
        recommendationService.deleteRecommendation(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
