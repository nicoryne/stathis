package edu.cit.stathis.auth.service;

import edu.cit.stathis.auth.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class WebhookService {

  @Autowired private SimpMessagingTemplate messagingTemplate;

  public void notifyUserEvent(User user, String event) {
    String message = String.format("User %s (%s) %s", user.getEmail(), user.getPhysicalId(), event);
    messagingTemplate.convertAndSend("/topic/user/" + user.getUserId(), message);
  }
}
