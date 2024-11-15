package school.faang.user_service.entity.mentorship;

import lombok.Getter;

@Getter
public enum RequestError {
  REQUESTER_IS_MISSING("Requester in missing in DB"),
  RECEIVER_IS_MISSING("Receiver is missing in DB"),
  EARLY_REQUEST("Not allowed more than one request per valid period"),
  SELF_REQUEST("Not allowed to send self-request"),
  ALREADY_ACCEPTED("Receiver is mentor of the requester already!"),
  NOT_FOUND("Mentorship request not found, id=");

  private final String message;

  RequestError(String message) {
    this.message = message;
  }

}
