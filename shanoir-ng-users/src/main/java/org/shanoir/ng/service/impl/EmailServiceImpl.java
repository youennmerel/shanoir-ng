package org.shanoir.ng.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.shanoir.ng.model.User;
import org.shanoir.ng.repository.UserRepository;
import org.shanoir.ng.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * Implementation of email service.
 * 
 * @author msimon
 *
 */
@Service
public class EmailServiceImpl implements EmailService {

	/**
	 * Logger
	 */
	private static final Logger LOG = LoggerFactory.getLogger(EmailServiceImpl.class);

	@Autowired
    private JavaMailSender mailSender;
	
	@Autowired
	private TemplateEngine templateEngine;
	
	@Autowired
	private UserRepository userRepository;
    
	@Value("${server.email-off}")
	private boolean emailOff;

	@Value("${front.server.address}")
	private String shanoirServerAddress;

	@Override
	public void notifyAdminAccountRequest(final User user) {
		if (emailOff) {
			return;
		}
		
		// Get admins emails
		final List<String> adminEmails = userRepository.findAdminEmails();
		
		MimeMessagePreparator messagePreparator = mimeMessage -> {
			final MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
	        messageHelper.setTo(adminEmails.toArray(new String[0]));
	        messageHelper.setSubject("New user account request from " + shanoirServerAddress);
	        final Map<String, Object> variables = new HashMap<String, Object>();
	        variables.put("user", user);
	        variables.put("serverAddress", shanoirServerAddress);
	        final String content = build("notifyAdminAccountRequest", variables);
	        messageHelper.setText(content, true);
	    };
	    try {
	        mailSender.send(messagePreparator);
	    } catch (MailException e) {
	    	LOG.error("Error while sending email to new user " + user.getEmail(), e);
	    }
	}
	
	@Override
	public void notifyNewUser(final User user, final String password) {
		if (emailOff) {
			return;
		}
		
		MimeMessagePreparator messagePreparator = mimeMessage -> {
			final MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
	        messageHelper.setTo(user.getEmail());
	        messageHelper.setSubject("Shanoir Account Creation");
	        final Map<String, Object> variables = new HashMap<String, Object>();
	        variables.put("firstname", user.getFirstName());
	        variables.put("lastname", user.getLastName());
	        variables.put("password", password);
	        variables.put("username", user.getUsername());
	        final String content = build("notifyNewUser", variables);
	        messageHelper.setText(content, true);
	    };
	    try {
	        mailSender.send(messagePreparator);
	    } catch (MailException e) {
	    	LOG.error("Error while sending email to new user " + user.getEmail(), e);
	    }
	}
	
	@Override
	public void notifyUserAccountRequestAccepted(final User user) {
		if (emailOff) {
			return;
		}
		
		MimeMessagePreparator messagePreparator = mimeMessage -> {
			final MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
	        messageHelper.setTo(user.getEmail());
	        messageHelper.setSubject("Granted: Your Shanoir account has been activated");
	        final Map<String, Object> variables = new HashMap<String, Object>();
	        variables.put("firstname", user.getFirstName());
	        variables.put("lastname", user.getLastName());
	        variables.put("serverAddress", shanoirServerAddress);
	        final String content = build("notifyUserAccountRequestAccepted", variables);
	        messageHelper.setText(content, true);
	    };
	    try {
	        mailSender.send(messagePreparator);
	    } catch (MailException e) {
	    	LOG.error("Error while sending email to new user " + user.getEmail(), e);
	    }
	}
	
	private String build(final String templateFile, final Map<String, Object> variables) {
        final Context context = new Context();
        if (variables != null) {
        	for (final String variable : variables.keySet()) {
        		context.setVariable(variable, variables.get(variable));
        	}
        }
        return templateEngine.process(templateFile, context);
    }

}
