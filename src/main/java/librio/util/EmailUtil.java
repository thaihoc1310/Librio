package librio.util;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailUtil {

    public static void sendResetCode(String toEmail, String resetCode) {
        final String fromEmail = "thaihoc131005@gmail.com";
        final String password = "yagz qsyk ugfm wdlh";

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Authenticator auth = new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, password);
            }
        };

        Session session = Session.getInstance(props, auth);

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
            message.setSubject("Password Reset Code");
            String htmlContent = "<!DOCTYPE html>" +
                    "<html lang='en'>" +
                    "<head>" +
                    "<meta charset='UTF-8'>" +
                    "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                    "<title>Password Reset</title>" +
                    "</head>" +
                    "<body style='font-family: Arial, sans-serif; background-color: #f4f4f9; margin: 0; padding: 0;'>" +
                    "<div class='email-container' style='background-color: #ffffff; border-radius: 8px; max-width: 600px; margin: 50px auto; padding: 20px; box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);'>" +
                    "<div class='header' style='text-align: center;'>" +
                    "<img src='https://i.imghippo.com/files/xnxU6342c.png' alt='Librio' style='width: 200px; height: auto;'>" +
                    "</div>" +
                    "<div class='email-content' style='text-align: center; color: #333; margin-top: 20px;'>" +
                    "<h2 style='color: #B38B60;'>Password Reset Request</h2>" +
                    "<p>We received a request to reset your password. Please use the reset code below to proceed.</p>" +
                    "<div class='reset-code' style='font-size: 20px; font-weight: bold; color: #4c2113; margin: 10px 0;'>" +
                    "<p>Your reset code: <strong>" + resetCode + "</strong></p>" +
                    "</div>" +
                    "<p>If you didn't request a password reset, please ignore this email.</p>" +
                    "</div>" +
                    "<div class='footer' style='text-align: center; font-size: 12px; color: #888; margin-top: 30px;'>" +
                    "<p>Thank you for using our service!</p>" +
                    "</div>" +
                    "</div>" +
                    "</body>" +
                    "</html>";
            message.setContent(htmlContent, "text/html");
            Transport.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
