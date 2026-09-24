package br.edu.ufape.enzitech.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${api.mail.from}") 
    private String fromEmail;

    public void sendPasswordResetEmail(String to, String userName, String pinCode) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            Context context = new Context();
            context.setVariable("userName", userName);
            context.setVariable("pinCode", pinCode);

            String htmlContent = templateEngine.process("recover-password", context);

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("Enzitech - Recuperação de Senha");
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Falha ao enviar e-mail de recuperação: " + e.getMessage(), e);
        }
    }

    public void sendPasswordChangedEmail(String to, String userName, LocalDateTime changedAt) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            Context context = new Context();
            context.setVariable("userName", userName);
            context.setVariable("changedAt", changedAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm")));

            String htmlContent = templateEngine.process("password-changed", context);

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("Enzitech - Senha Alterada");
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Falha ao enviar e-mail de confirmação de senha alterada: " + e.getMessage(), e);
        }
    }
}