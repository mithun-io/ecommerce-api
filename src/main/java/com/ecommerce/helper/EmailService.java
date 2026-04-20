package com.ecommerce.helper;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;

    @Async
    public void sendOtp(String name, String email, Integer otp) throws MessagingException, UnsupportedEncodingException {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage);

        mimeMessageHelper.setFrom("admin", "admin@ecommerce.com");
        mimeMessageHelper.setTo(email);
        mimeMessageHelper.setSubject("one time password for account creation");

        String text = "<html><body>"
                + "<h3>hello <b>" + name + "</b>,</h3>"
                + "<h3>your one time password is <b>" + otp + "</b></h3>"
                + "<h3>otp will be valid for only <b>5 minutes</b>."
                + "</body></html>";

        mimeMessageHelper.setText(text, true);
        javaMailSender.send(mimeMessage);
    }

    @Async
    public void sendConfirmation(String name, String email, String password) throws MessagingException, UnsupportedEncodingException {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage);

        mimeMessageHelper.setFrom("admin", "admin@ecommerce.com");
        mimeMessageHelper.setTo(email);
        mimeMessageHelper.setSubject("registration successful");

        String text = "<html><body>"
                + "<h3>hello <b>" + name + "</b>,</h3>"
                + "<h3>your account has been successfully registered.</h3>"
                + "<table>"
                + "<tr><td>email:</td><td><b>" + email + "</b></td></tr>"
                + "<tr><td>password:</td><td><b>" + password + "</b></td></tr>"
                + "</table>"
                + "</body></html>";

        mimeMessageHelper.setText(text, true);
        javaMailSender.send(mimeMessage);
    }

    @Async
    public void sendPaymentConfirmation(String email, Long orderId, Double amount) throws MessagingException, UnsupportedEncodingException {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage);

        mimeMessageHelper.setFrom("admin", "admin@ecommerce.com");
        mimeMessageHelper.setTo(email);
        mimeMessageHelper.setSubject("payment successful");

        String text = "<html><body>"
                + "<h3>payment successful</h3>"
                + "<table>"
                + "<tr><td>order id:</td><td><b>" + orderId + "</b></td></tr>"
                + "<tr><td>amount:</td><td><b>" + amount + "</b></td></tr>"
                + "</table>"
                + "</body></html>";

        mimeMessageHelper.setText(text, true);
        javaMailSender.send(mimeMessage);
    }
}
