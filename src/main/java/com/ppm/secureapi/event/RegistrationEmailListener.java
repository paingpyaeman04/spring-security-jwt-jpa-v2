package com.ppm.secureapi.event;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.MessageSource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
//import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import com.ppm.secureapi.model.User;
import com.ppm.secureapi.model.VerificationToken;
import com.ppm.secureapi.repository.VerificationTokenRepository;


@Component
public class RegistrationEmailListener implements ApplicationListener<OnRegistrationSuccessEvent> {

	@Autowired
	private MessageSource messages;
	
	@Autowired
	private VerificationTokenRepository verificationTokenRepository;
	
	@Autowired
	private JavaMailSender mailSender;
	
//	@Autowired
//	private MailSender mailSender;

	@Override
	public void onApplicationEvent(OnRegistrationSuccessEvent event) {
		this.confirmRegistration(event);
		
	}

	private void confirmRegistration(OnRegistrationSuccessEvent event) {
		User user = event.getUser();
		String token = UUID.randomUUID().toString();
		
		VerificationToken verificationToken = new VerificationToken(token, user);
		verificationTokenRepository.saveAndFlush(verificationToken);
		
		String recipient = user.getEmail();
		String subject = "Registration Confirmation";
        String url 
          = event.getAppUrl() + "/api/auth/confirmRegistration?token=" + token;
        String message = messages.getMessage("Registration Successful! Click the link below to verify your email\n", null, event.getLocale());
         
        SimpleMailMessage email = new SimpleMailMessage();
        email.setTo(recipient);
        email.setSubject(subject);
        email.setText(message + url);
        mailSender.send(email);
        System.out.println(url);
		
	}
	
	
}