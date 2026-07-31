package com.restaurant.backend.service;

import com.restaurant.backend.dto.request.OrderDto;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;
    private final JavaMailSender mailSender;

    public void notifyKitchen(OrderDto.OrderResponse order) {
        log.info("Sending real-time WebSocket update to /topic/kitchen for Order: {}", order.getOrderNumber());
        messagingTemplate.convertAndSend("/topic/kitchen", order);
    }

    public void notifyWaiter(OrderDto.OrderResponse order) {
        log.info("Sending real-time WebSocket update to /topic/waiter for Order: {}", order.getOrderNumber());
        messagingTemplate.convertAndSend("/topic/waiter", order);
    }

    public void notifyCashier(OrderDto.OrderResponse order) {
        log.info("Sending real-time WebSocket update to /topic/cashier for Order: {}", order.getOrderNumber());
        messagingTemplate.convertAndSend("/topic/cashier", order);
    }

    public void notifyCustomer(Long customerId, OrderDto.OrderResponse order) {
        log.info("Sending real-time WebSocket update to /topic/customer/{} for Order: {}", customerId, order.getOrderNumber());
        messagingTemplate.convertAndSend("/topic/customer/" + customerId, order);
    }

    public void notifyAdmin(Object event) {
        messagingTemplate.convertAndSend("/topic/admin", event);
    }

    @Async
    public void sendInvoiceEmail(String toEmail, String customerName, String invoiceNumber, byte[] pdfBytes) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("Your E-Bill & Invoice #" + invoiceNumber + " - Royal Gourmet Restaurant");
            helper.setText("Dear " + customerName + ",\n\nThank you for dining with Royal Gourmet Restaurant! Please find attached your digital invoice.\n\nWarm regards,\nRoyal Gourmet Management");

            helper.addAttachment("Invoice_" + invoiceNumber + ".pdf", new ByteArrayResource(pdfBytes));

            mailSender.send(message);
            log.info("Successfully sent invoice email for #{} to {}", invoiceNumber, toEmail);
        } catch (Exception e) {
            log.error("Failed to send invoice email to {}: {}", toEmail, e.getMessage());
        }
    }
}
