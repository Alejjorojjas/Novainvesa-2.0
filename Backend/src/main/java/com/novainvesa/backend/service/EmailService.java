package com.novainvesa.backend.service;

import com.novainvesa.backend.entity.Order;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Servicio de notificaciones por email.
 *
 * RN-030: Email de confirmación siempre que el pedido tenga email.
 *         3 reintentos con 5 min de delay. El fallo NO cancela el pedido.
 * RN-032: Notificación de envío cuando pasa a SHIPPED.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    // ─── RN-030: Confirmación de pedido ──────────────────────────────────────

    @Async
    public void sendOrderConfirmation(Order order) {
        if (fromEmail == null || fromEmail.isBlank()) {
            log.warn("SMTP no configurado — omitiendo email para pedido {}", order.getOrderCode());
            return;
        }
        int maxAttempts = 3;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                MimeMessage msg = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
                helper.setFrom(fromEmail, "Novainvesa");
                helper.setTo(order.getCustomerEmail());
                helper.setSubject("Tu pedido " + order.getOrderCode() + " fue confirmado");
                helper.setText(buildConfirmationHtml(order), true);
                mailSender.send(msg);
                log.info("Email confirmacion enviado — pedido {} (intento {})", order.getOrderCode(), attempt);
                return;
            } catch (Exception e) {
                log.error("Error enviando email pedido {} (intento {}/{}): {}",
                        order.getOrderCode(), attempt, maxAttempts, e.getMessage());
                if (attempt < maxAttempts) {
                    try {
                        Thread.sleep(5 * 60 * 1000L);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                } else {
                    log.error("Todos los intentos de email fallaron para pedido {}. Pedido sigue valido.",
                            order.getOrderCode());
                }
            }
        }
    }

    // ─── RN-032: Notificación de envío ───────────────────────────────────────

    @Async
    public void sendShippedNotification(Order order, String trackingNumber, String carrier) {
        if (fromEmail == null || fromEmail.isBlank()) return;
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setFrom(fromEmail, "Novainvesa");
            helper.setTo(order.getCustomerEmail());
            helper.setSubject("Tu pedido " + order.getOrderCode() + " esta en camino");
            helper.setText(buildShippedHtml(order, trackingNumber, carrier), true);
            mailSender.send(msg);
            log.info("Email envio enviado — pedido {}", order.getOrderCode());
        } catch (Exception e) {
            log.error("Error enviando email envio pedido {}: {}", order.getOrderCode(), e.getMessage());
        }
    }

    // ─── Builders de HTML ─────────────────────────────────────────────────────

    private String buildConfirmationHtml(Order order) {
        return """
                <!DOCTYPE html><html><head><meta charset="UTF-8"></head>
                <body style="font-family:Arial,sans-serif;background:#0a0a0a;color:#f0f0f0;margin:0;padding:20px;">
                  <div style="max-width:600px;margin:0 auto;background:#171717;border-radius:12px;padding:32px;">
                    <h1 style="color:#2563eb;text-align:center;">Novainvesa</h1>
                    <h2 style="text-align:center;color:#f0f0f0;">Pedido confirmado</h2>
                    <p style="color:#a3a3a3;text-align:center;">Codigo: <strong style="color:#f0f0f0;">%s</strong></p>
                    <div style="background:#0a0a0a;border-radius:8px;padding:16px;margin:16px 0;">
                      <p style="margin:4px 0;color:#a3a3a3;">Total: <strong style="color:#f59e0b;font-size:1.2em;">$%s COP</strong></p>
                      <p style="margin:4px 0;color:#a3a3a3;">Direccion: <strong style="color:#f0f0f0;">%s, %s</strong></p>
                      <p style="margin:4px 0;color:#a3a3a3;">Entrega estimada: <strong style="color:#f0f0f0;">3-5 dias habiles</strong></p>
                    </div>
                    <div style="text-align:center;margin-top:24px;">
                      <a href="%s/rastrear?order=%s" style="background:#2563eb;color:white;padding:12px 24px;border-radius:9999px;text-decoration:none;font-weight:bold;">
                        Rastrear mi pedido
                      </a>
                    </div>
                  </div>
                </body></html>
                """.formatted(
                order.getOrderCode(),
                formatPrice(order.getTotal()),
                order.getShippingAddress(), order.getShippingCity(),
                frontendUrl, order.getOrderCode());
    }

    private String buildShippedHtml(Order order, String trackingNumber, String carrier) {
        return """
                <!DOCTYPE html><html><head><meta charset="UTF-8"></head>
                <body style="font-family:Arial,sans-serif;background:#0a0a0a;color:#f0f0f0;margin:0;padding:20px;">
                  <div style="max-width:600px;margin:0 auto;background:#171717;border-radius:12px;padding:32px;">
                    <h1 style="color:#2563eb;text-align:center;">Novainvesa</h1>
                    <h2 style="text-align:center;">Tu pedido esta en camino</h2>
                    <p style="text-align:center;color:#a3a3a3;">Codigo: <strong>%s</strong></p>
                    <div style="background:#0a0a0a;border-radius:8px;padding:16px;margin:16px 0;">
                      <p>Numero de guia: <strong style="color:#f59e0b;">%s</strong></p>
                      <p>Transportadora: <strong>%s</strong></p>
                      <p style="color:#a3a3a3;">Entrega estimada: 3-5 dias habiles</p>
                    </div>
                    <div style="text-align:center;margin-top:24px;">
                      <a href="%s/rastrear?order=%s" style="background:#2563eb;color:white;padding:12px 24px;border-radius:9999px;text-decoration:none;font-weight:bold;">Ver estado</a>
                    </div>
                  </div>
                </body></html>
                """.formatted(
                order.getOrderCode(), trackingNumber, carrier,
                frontendUrl, order.getOrderCode());
    }

    private String formatPrice(BigDecimal price) {
        if (price == null) return "0";
        return String.format("%,.0f", price).replace(",", ".");
    }
}
