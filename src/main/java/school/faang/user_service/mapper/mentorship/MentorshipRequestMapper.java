package school.faang.user_service.mapper.mentorship;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import school.faang.user_service.dto.mentorship.MentorshipRequestDto;
import school.faang.user_service.entity.mentorship.MentorshipRequest;

@Mapper(componentModel = "spring")
public interface MentorshipRequestMapper {

  @Mapping(target = "requester", ignore = true)
  @Mapping(target = "receiver", ignore = true)
  @Mapping(target = "createdAt", source = "createdAt")
  @Mapping(target = "updatedAt", source = "updatedAt")
  MentorshipRequest toEntity(MentorshipRequestDto requestDto);

  @Mapping(target = "requesterId", source = "requester.id")
  @Mapping(target = "receiverId", source = "receiver.id")
  @Mapping(source = "createdAt", target = "createdAt", dateFormat = "yyyy-MM-dd HH:mm:ss")
  @Mapping(source = "updatedAt", target = "updatedAt", dateFormat = "yyyy-MM-dd HH:mm:ss")
  MentorshipRequestDto toDto(MentorshipRequest request);
}
