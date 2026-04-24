package com.ecommerce.helper;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;

    @Value("${ADMIN_EMAIL}")
    private String adminEmail;

    @Async
    public void sendOtp(String name, String email, Integer otp) {
        try {
            String subject = "one time password for account creation";

            String text = "<html><body>"
                    + "<h3>hello <b>" + name + "</b>,</h3>"
                    + "<h3>your one time password is <b>" + otp + "</b></h3>"
                    + "<h3>otp will be valid for only <b>5 minutes</b>.</h3>"
                    + "</body></html>";

            sendEmail(email, subject, text);

        } catch (Exception e) {
            log.error("failed to send OTP email to {}", email, e);
        }
    }

    @Async
    public void sendConfirmation(String name, String email, String password) {
        try {
            String subject = "registration successful";

            String text = "<html><body>"
                    + "<h3>hello <b>" + name + "</b>,</h3>"
                    + "<h3>your account has been successfully registered.</h3>"
                    + "<h3>email: <b>" + email + "</b></h3>"
                    + "<h3>password: <b>" + password + "</b></h3>"
                    + "</body></html>";

            sendEmail(email, subject, text);

        } catch (Exception e) {
            log.error("failed to send confirmation email to {}", email, e);
        }
    }

    @Async
    public void sendPaymentConfirmation(String email, Long orderId, Double amount) {
        try {
            String subject = "payment successful";

            String text = "<html><body>"
                    + "<h3>payment successful</h3>"
                    + "<h3>order id: <b>" + orderId + "</b></h3>"
                    + "<h3>amount: <b>" + amount + "</b></h3>"
                    + "</body></html>";

            sendEmail(email, subject, text);

        } catch (Exception e) {
            log.error("failed to send payment email to {}", email, e);
        }
    }

    @Async
    public void notifyAdminNewProduct(String title, Long productId) {
        try {
            String subject = "new product pending approval";

            String text = "<html><body>"
                    + "<h3>New product added</h3>"
                    + "<p>Product: <b>" + title + "</b></p>"
                    + "<p>ID: <b>" + productId + "</b></p>"
                    + "</body></html>";

            sendEmail(adminEmail, subject, text);

        } catch (Exception e) {
            log.error("failed to notify admin", e);
        }
    }

    @Async
    public void sendProductApprovedEmail(String email, String title) {
        try {
            String subject = "product Approved";

            String text = "<html><body>"
                    + "<h3>your product has been approved</h3>"
                    + "<p>product: <b>" + title + "</b></p>"
                    + "</body></html>";

            sendEmail(email, subject, text);

        } catch (Exception e) {
            log.error("failed to send product approval email", e);
        }
    }

    private void sendEmail(String to, String subject, String htmlContent) throws Exception {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);

        mimeMessageHelper.setFrom(adminEmail, "ecommerce app");
        mimeMessageHelper.setTo(to);
        mimeMessageHelper.setSubject(subject);
        mimeMessageHelper.setText(htmlContent, true);

        javaMailSender.send(mimeMessage);
    }
}