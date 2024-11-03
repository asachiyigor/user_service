package school.faang.user_service.mapper.invitation;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import school.faang.user_service.dto.goal.GoalInvitationDto;
import school.faang.user_service.entity.goal.GoalInvitation;

@Mapper(componentModel="spring", unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface GoalInvitationMapper {

    @Mapping(source = "id", target = "id")
    @Mapping(source = "inviter.id", target = "inviterId")
    @Mapping(source = "invited.id", target = "invitedUserId")
    @Mapping(source = "goal.id", target = "goalId")
    @Mapping(source = "status", target = "status")
    GoalInvitationDto toDto(GoalInvitation invitation);

    GoalInvitation toEntity(GoalInvitationDto invitationDto);
}