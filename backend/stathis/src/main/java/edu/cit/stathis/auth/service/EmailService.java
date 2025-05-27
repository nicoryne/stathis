package edu.cit.stathis.auth.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

  @Autowired private JavaMailSender mailSender;

  @Value("${MAIL_USERNAME}")
  private String mailFrom;

  public void sendVerificationEmail(String to, String token) throws MessagingException {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true);
      helper.setFrom(mailFrom);
      helper.setTo(to);
      helper.setSubject("Verify Your Email");
    // Use localhost for local development
    helper.setText(
        "Please verify your email by clicking the link: "
            + "http://188.166.246.153:8080/api/auth/verify-email?token="
            + token,
        true);
      mailSender.send(message);
  }

  public void sendPasswordResetEmail(String to, String token) throws MessagingException {
    MimeMessage message = mailSender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(message, true);
    helper.setFrom(mailFrom);
    helper.setTo(to);
    helper.setSubject("Reset Your Password");
    // Use localhost for local development
    helper.setText(
        "Please reset your password by clicking the link: "
            + "http://188.166.246.153:8080/api/auth/reset-password?token="
            + token,
        true);
    mailSender.send(message);
  }
}
