package com.redcode.mcms.service;

import jakarta.ejb.Asynchronous;
import jakarta.ejb.Stateless;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/** Sends transactional mail through EmailJS without exposing credentials to the browser. */
@Stateless
public class EmailJsService {

    private static final String ENDPOINT = "https://api.emailjs.com/api/v1.0/email/send";
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10)).build();

    @Asynchronous
    public void sendReceipt(String recipient, String saleNumber, String customerName,
                            String items, String total, String paymentMethod, String saleDate,
                            String cashierName) {
        if (recipient == null || recipient.isBlank()) return;

        String serviceId = setting("MCMS_EMAILJS_SERVICE_ID", "service_7t7hs2g");
        String templateId = setting("MCMS_EMAILJS_TEMPLATE_ID", "template_j5sulv5");
        // EmailJS public keys are safe to expose; the private key is never stored in code.
        String publicKey = setting("MCMS_EMAILJS_PUBLIC_KEY", "tOOf1QsFtNO-JiHZZ");
        String privateKey = setting("MCMS_EMAILJS_PRIVATE_KEY", null);
        if (publicKey == null || publicKey.isBlank()) return;

        String message = "Customer: " + customerName + "\n"
                + "Sale number: " + saleNumber + "\n"
                + "Cashier: " + cashierName + "\n"
                + "Date: " + saleDate + "\n"
                + "Items:\n" + items + "\n"
                + "Total: R" + total + "\n"
                + "Payment method: " + paymentMethod;

        Map<String, String> params = new LinkedHashMap<>();
        params.put("email", recipient);
        params.put("subject", "MegaMart receipt " + saleNumber);
        params.put("reason", "receipt " + saleNumber);
        params.put("message", message);
        params.put("name", customerName);
        params.put("regards", "MegaMart Central Management System");
        params.put("sale_number", saleNumber);
        params.put("customer_name", customerName);
        params.put("items", items);
        params.put("total", total);
        params.put("payment_method", paymentMethod);
        params.put("sale_date", saleDate);
        params.put("cashier_name", cashierName);

        StringBuilder body = new StringBuilder("{\"service_id\":\"")
                .append(escape(serviceId)).append("\",\"template_id\":\"")
                .append(escape(templateId)).append("\",\"user_id\":\"")
                .append(escape(publicKey)).append("\",\"template_params\":{");
        body.append(params.entrySet().stream()
                .map(e -> "\"" + escape(e.getKey()) + "\":\"" + escape(e.getValue()) + "\"")
                .collect(Collectors.joining(",")));
        body.append("}");
        if (privateKey != null && !privateKey.isBlank()) {
            body.append(",\"accessToken\":\"").append(escape(privateKey)).append("\"");
        }
        body.append("}");

        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(ENDPOINT))
                    .timeout(Duration.ofSeconds(20))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString(), StandardCharsets.UTF_8))
                    .build();
            HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 300) {
                System.err.println("EmailJS receipt failed with HTTP " + response.statusCode());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            System.err.println("EmailJS receipt failed: " + e.getMessage());
        }
    }

    private String setting(String name, String fallback) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) value = System.getProperty(name);
        return value == null || value.isBlank() ? fallback : value;
    }

    private String escape(String value) {
        return value == null ? "" : value.replace("\\", "\\\\")
                .replace("\"", "\\\"").replace("\r", "\\r").replace("\n", "\\n");
    }
}
