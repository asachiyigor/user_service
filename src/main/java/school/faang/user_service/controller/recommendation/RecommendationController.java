package school.faang.user_service.controller.recommendation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
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
@Tag(name = "Recommendation API", description = "API for managing user recommendations")
@RequestMapping("/api/recommendations")
public class RecommendationController {
    private final RecommendationService recommendationService;

    @Operation(summary = "Get all recommendations", description = "Returns a list of all recommendations.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list", content = {@Content(mediaType = "application/json")})
    })
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<RecommendationDto> getAllRecommendations() {
        return recommendationService.getAllRecommendations();
    }

    @Operation(summary = "Get single recommendation by Id", description = "Returns a single recommendation by its Id.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved recommendation", content = {@Content(mediaType = "application/json")}),
            @ApiResponse(responseCode = "404", description = "Recommendation with provided id is not found", content = {@Content(mediaType = "application/json")}),
    })
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public RecommendationDto getRecommendationById(@PathVariable Long id) {
        return recommendationService.getRecommendationById(id);
    }

    @Operation(summary = "Get all recommendations by authorId", description = "Returns list of recommendations written by authorId.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list for provided authorId", content = {@Content(mediaType = "application/json")})
    })
    @GetMapping("/author/{authorId}")
    @ResponseStatus(HttpStatus.OK)
    public Page<RecommendationDto> getAllGivenRecommendations(@PathVariable Long authorId, @ParameterObject Pageable pageable) {
        return recommendationService.getAllGivenRecommendations(authorId, pageable);
    }

    @Operation(summary = "Get all recommendations for provided receiverId", description = "Returns list of recommendations for receiverId.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list for provided authorId", content = {@Content(mediaType = "application/json")})
    })
    @GetMapping("/receiver/{receiverId}")
    @ResponseStatus(HttpStatus.OK)
    public Page<RecommendationDto> getAllUserRecommendations(@PathVariable Long receiverId, @ParameterObject Pageable pageable) {
        return recommendationService.getAllUserRecommendations(receiverId, pageable);
    }

    @Operation(summary = "Create new recommendation", description = "Creates a new recommendation.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Recommendation created successfully", content = {@Content(mediaType = "application/json")}),
            @ApiResponse(responseCode = "400", description = "Bad Request for create recomendation", content = {@Content(mediaType = "application/json")}),
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RecommendationDto createRecommendation(@Valid @RequestBody RecommendationDto recommendationDto) {
        return recommendationService.createRecommendation(recommendationDto);
    }

    @Operation(summary = "Update existing recommendation", description = "Updates a recommendation.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Recommendation updated successfully", content = {@Content(mediaType = "application/json")}),
            @ApiResponse(responseCode = "400", description = "Bad Request for update recommendation", content = {@Content(mediaType = "application/json")}),
            @ApiResponse(responseCode = "404", description = "Recommendation Id was not found", content = {@Content(mediaType = "application/json")}),
    })
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public RecommendationDto updateRecommendation(@PathVariable Long id, @Valid @RequestBody RecommendationDto recommendationDto) {
        return recommendationService.updateRecommendation(id, recommendationDto);
    }

    @Operation(summary = "Delete existing recommendation", description = "Deletes a recommendation.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Recommendation deleted successfully", content = {@Content(mediaType = "application/json")}),
            @ApiResponse(responseCode = "404", description = "Recommendation Id was not found", content = {@Content(mediaType = "application/json")}),
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRecommendation(@PathVariable Long id) {
        recommendationService.deleteRecommendation(id);
    }
}
