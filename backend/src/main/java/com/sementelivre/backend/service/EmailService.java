package com.sementelivre.backend.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void enviarEmailRecuperacaoSenha(String para, String token) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(para);
            message.setSubject("Semente Livre - Recuperação de Senha");
            message.setText("Você solicitou a redefinição de senha.\n\n" +
                    "Utilize o seguinte token no aplicativo para redefinir sua senha:\n" +
                    token + "\n\n" +
                    "Este token expira em 15 minutos.");
            mailSender.send(message);
        } catch (Exception e) {
            // Em caso de falha no servidor SMTP em ambiente local, não interrompe o fluxo de geração de token
            System.err.println("Aviso: Falha ao enviar e-mail (SMTP): " + e.getMessage());
        }
    }
}
