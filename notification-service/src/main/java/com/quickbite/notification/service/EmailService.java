package com.quickbite.notification.service;

import com.quickbite.notification.messaging.OrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import java.io.UnsupportedEncodingException;

// Handles all email sending for notification-service
// Sending email is done ASYNCHRONOUSLY (@Async)
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.email.from}")
    private String fromEmail;

    @Value("${app.email.from-name}")
    private String fromName;

    @Async
    public void sendOrderPlacedEmail(OrderEvent event,
                                     String customerEmail,
                                     String customerName) {
        try {
            String subject = "Order Confirmed — QuickBite #" + event.getOrderId();
            String body = buildOrderPlacedEmailBody(event, customerName);
            sendEmail(customerEmail, subject, body);
            log.info("Order placed email sent to: {}", customerEmail);

        } catch (Exception e) {
            log.error("Failed to send order placed email to: {}", customerEmail, e);
        }
    }

    // Builds a clean HTML email body with order details
    private String buildOrderPlacedEmailBody(OrderEvent event, String customerName) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body { font-family: Arial, sans-serif;
                               background: #f5f5f5; margin: 0;
                               padding: 20px; }
                        .container { max-width: 600px; margin: auto;
                                     background: white;
                                     border-radius: 8px;
                                     overflow: hidden;
                                     box-shadow: 0 2px 8px rgba(0,0,0,0.1); }
                        .header { background: linear-gradient(135deg,#EAD8C5,#D6B89C); padding: 30px;
                                  text-align: center; }
                        .header h1 { color: #2B1D17; margin: 0;
                                     font-size: 28px; }
                        .header p { color: #786B5E; margin: 5px 0 0; }
                        .body { padding: 30px; }
                        .greeting { font-size: 18px; color: #212121;
                                    margin-bottom: 20px; }
                        .order-box { background: #fafafa;
                                     border: 1px solid #e0e0e0;
                                     border-radius: 6px;
                                     padding: 20px;
                                     margin: 20px 0; }
                        .order-box h3 { margin: 0 0 15px;
                                        color: #e53935; font-size: 16px; }
                        .detail-row { display: flex;
                                      justify-content: space-between;
                                      padding: 8px 0;
                                      border-bottom: 1px solid #f0f0f0; }
                        .detail-row:last-child { border-bottom: none;
                                                  font-weight: bold;
                                                  color: #e53935; }
                        .label { color: #757575; font-size: 14px; }
                        .value { color: #212121; font-size: 14px;
                                 font-weight: 500; }
                        .status-badge { display: inline-block;
                                        background: #e8f5e9;
                                        color: #2e7d32;
                                        padding: 6px 16px;
                                        border-radius: 20px;
                                        font-size: 13px;
                                        font-weight: bold;
                                        margin: 10px 0; }
                        .message { color: #616161; font-size: 14px;
                                   line-height: 1.6; margin: 20px 0; }
                        .footer { background: #f5f5f5; padding: 20px;
                                  text-align: center;
                                  color: #9e9e9e; font-size: 12px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>QuickBite 🍟</h1>
                            <p>Order Smarter. Eat Better. Delivered Faster.</p>
                        </div>
                        <div class="body">
                            <p class="greeting">
                                Hi <strong>%s</strong>! 👋
                            </p>
                            <p class="message">
                                Your order has been placed successfully!
                                We have notified the restaurant and they
                                will confirm it shortly.
                            </p>

                            <span class="status-badge">
                                ✅ Order Placed Successfully
                            </span>

                            <div class="order-box">
                                <h3>📋 Order Details</h3>
                                <div class="detail-row">
                                    <span class="label">Order ID&nbsp;</span>
                                    <span class="value">#%d</span>
                                </div>
                                <div class="detail-row">
                                    <span class="label">Payment Mode&nbsp;</span>
                                    <span class="value">%s</span>
                                </div>
                                <div class="detail-row">
                                    <span class="label">Delivery Address&nbsp;</span>
                                    <span class="value">%s</span>
                                </div>
                                <div class="detail-row">
                                    <span class="label">Estimated Delivery&nbsp;</span>
                                    <span class="value">~30 minutes</span>
                                </div>
                                <div class="detail-row">
                                    <span class="label">Total Amount&nbsp;</span>
                                    <span class="value">Rs. %.0f</span>
                                </div>
                            </div>

                            <p class="message">
                                You will receive updates as your order
                                progresses. You can also track your order
                                in real time on the QuickBite app.
                            </p>
                        </div>
                        <div class="footer">
                            <p>© 2026 QuickBite. All rights reserved.</p>
                            <p>This email was sent to confirm your order.</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(
                customerName,
                event.getOrderId(),
                event.getPaymentMode(),
                event.getDeliveryAddress() != null ? event.getDeliveryAddress() : "On file",
                event.getFinalAmount()
        );
    }

    private void sendEmail(String toEmail, String subject, String htmlBody)
            throws MessagingException, UnsupportedEncodingException {

        // MimeMessage supports HTML content
        MimeMessage message = mailSender.createMimeMessage();

        // MimeMessageHelper makes it easy to set HTML, from name, subject, etc.
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        // Set from as "QuickBite <yourapp@gmail.com>"
        helper.setFrom(fromEmail, fromName);
        helper.setTo(toEmail);
        helper.setSubject(subject);

        // true = HTML content (false = plain text)
        helper.setText(htmlBody, true);

        mailSender.send(message);
    }
}