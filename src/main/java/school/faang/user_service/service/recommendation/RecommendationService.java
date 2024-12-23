package school.faang.user_service.service.recommendation;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.faang.user_service.dto.recommendation.RecommendationDto;
import school.faang.user_service.dto.recommendation.RecommendationEventDto;
import school.faang.user_service.dto.recommendation.SkillOfferDto;
import school.faang.user_service.entity.recommendation.Recommendation;
import school.faang.user_service.exception.DataValidationException;
import school.faang.user_service.mapper.recommendation.RecommendationMapper;
import school.faang.user_service.publisher.RecommendationEventPublisher;
import school.faang.user_service.repository.SkillRepository;
import school.faang.user_service.repository.UserRepository;
import school.faang.user_service.repository.UserSkillGuaranteeRepository;
import school.faang.user_service.repository.recommendation.RecommendationRepository;
import school.faang.user_service.repository.recommendation.SkillOfferRepository;


@Service
@RequiredArgsConstructor
public class RecommendationService {

  private final RecommendationRepository recommendationRepository;

  private final SkillOfferRepository skillOfferRepository;

  private final UserRepository userRepository;

  private final SkillRepository skillRepository;

  private final RecommendationMapper recommendationMapper;

  private final UserSkillGuaranteeRepository userSkillGuaranteeRepository;

  private final RecommendationEventPublisher recommendationEventPublisher;

  @Transactional
  public RecommendationDto createRecommendation(RecommendationDto recommendationDto) {
    Long receiverId = recommendationDto.getReceiverId();
    Long authorId = recommendationDto.getAuthorId();
    Optional<List<SkillOfferDto>> skillOffers = Optional.ofNullable(
        recommendationDto.getSkillOffers());
    LocalDateTime currentDateTime = LocalDateTime.now();

    validateUserIdExists(authorId);
    validateUserIdExists(receiverId);
    validateRecommendationProvidedInLast6Months(authorId, receiverId, currentDateTime);

    Long recommendationId = recommendationRepository.create(authorId, receiverId,
        recommendationDto.getContent());

    skillOffers.ifPresent(offers -> {
      validateSkillsAreUnique(offers);
      validateSkillIdExists(offers);
      saveSkillOffersAndGuarantee(recommendationId, offers, receiverId, authorId);
    });

    recommendationDto.setCreatedAt(currentDateTime);
    recommendationDto.setId(recommendationId);

    recommendationEventPublisher.publish(RecommendationEventDto.builder()
        .id(recommendationDto.getId())
        .authorId(recommendationDto.getAuthorId())
        .receiverId(recommendationDto.getReceiverId())
        .receivedAt(recommendationDto.getCreatedAt().toString())
        .build());

    return recommendationDto;
  }

  @Transactional
  public RecommendationDto updateRecommendation(Long recommendationId,
      RecommendationDto recommendationDto) {
    validateRecommendationIdExists(recommendationId);

    Long receiverId = recommendationDto.getReceiverId();
    Long authorId = recommendationDto.getAuthorId();
    recommendationDto.setId(recommendationId);
    Optional<List<SkillOfferDto>> skillOffersNew = Optional.ofNullable(
        recommendationDto.getSkillOffers());
    validateUserIdExists(authorId);
    validateUserIdExists(receiverId);

    skillOffersNew.ifPresent(offers -> {
      validateSkillsAreUnique(offers);
      validateSkillIdExists(offers);
      saveSkillOffersAndGuarantee(recommendationId, offers, receiverId, authorId);
    });

    skillOfferRepository.deleteAllByRecommendationId(recommendationId);
    recommendationRepository.update(recommendationId, authorId, receiverId,
        recommendationDto.getContent());
    return recommendationDto;
  }

  @Transactional
  public void deleteRecommendation(Long recommendationId) {
    Recommendation recommendation = getRecommendationIfExists(recommendationId);
    recommendationRepository.delete(recommendation);
  }

  public RecommendationDto getRecommendationById(Long id) {
    Recommendation recommendation = recommendationRepository.findById(id)
        .orElseThrow(() -> new DataValidationException(("Recommendation Id not found. Id " + id)));
    return recommendationMapper.toDto(recommendation);
  }

  public List<RecommendationDto> getAllRecommendations() {
    List<Recommendation> recommendations = (List<Recommendation>) recommendationRepository.findAll();
    return recommendations.stream().map(recommendationMapper::toDto).collect(Collectors.toList());
  }

  public Page<RecommendationDto> getAllUserRecommendations(Long receiverId, Pageable pageable) {
    Page<Recommendation> recommendations = recommendationRepository.findAllByReceiverId(receiverId,
        pageable);
    return recommendations.map(recommendationMapper::toDto);
  }

  public Page<RecommendationDto> getAllGivenRecommendations(Long authorId, Pageable pageable) {
    Page<Recommendation> recommendations = recommendationRepository.findAllByAuthorId(authorId,
        pageable);
    return recommendations.map(recommendationMapper::toDto);
  }

  private Recommendation getRecommendationIfExists(Long recommendationId) {
    return recommendationRepository
        .findById(recommendationId)
        .orElseThrow(() -> new DataValidationException(
            "Recommendation Id not found. Id " + recommendationId));
  }

  private void saveSkillOffersAndGuarantee(Long recommendationId, List<SkillOfferDto> skillOffers,
      Long receiverId, Long
          authorId) {
    for (SkillOfferDto skillOffer : skillOffers) {
      Long skillId = skillOffer.getSkillId();
      skillOfferRepository.create(skillId, recommendationId);
      if (skillRepository.findUserSkill(skillId, receiverId).isPresent()
          && !userSkillGuaranteeRepository.existsGuarantorForUserAndSkill(receiverId, skillId,
          authorId)) {
        userSkillGuaranteeRepository.addSkillGuarantee(receiverId, skillId, authorId);
      }
    }
  }

  private void validateRecommendationIdExists(Long recommendationId) {
    if (!recommendationRepository.existsById(recommendationId)) {
      throw new DataValidationException(
          "Recommendation not found with id: " + recommendationId);
    }
  }

  private void validateRecommendationProvidedInLast6Months(Long authorId, Long receiverId,
      LocalDateTime
          currentDateTime) {
    Optional<Recommendation> recentRecommendation = recommendationRepository.findFirstByAuthorIdAndReceiverIdOrderByCreatedAtDesc(
        authorId, receiverId);
    if (recentRecommendation.isPresent()) {
      LocalDateTime createdAt = recentRecommendation.get().getCreatedAt();
      LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6);
      if (createdAt.isAfter(sixMonthsAgo)) {
        throw new DataValidationException(
            "Recommendation already exists within the last 6 months.");
      }
    }
  }

  private void validateSkillIdExists(List<SkillOfferDto> skillOffers) {
    for (SkillOfferDto skillOffer : skillOffers) {
      if (!skillRepository.existsById(skillOffer.getSkillId())) {
        throw new DataValidationException("Skill ID does not exist: " + skillOffer.getSkillId());
      }
    }
  }

  private void validateSkillsAreUnique(List<SkillOfferDto> skillOffers) {
    Set<SkillOfferDto> skillOffer = new HashSet<>(skillOffers);
    if (skillOffer.size() != skillOffers.size()) {
      throw new DataValidationException("Skill Offer list has duplicate skills");
    }
  }

  private void validateUserIdExists(Long userId) {
    if (!userRepository.existsById(userId)) {
      throw new DataValidationException("User with specified ID does not exist. Id: " + userId);
    }
  }
}