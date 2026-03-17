package com.grootan.policyrouter.channel;

import com.grootan.policyrouter.domain.model.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class SmsChannel implements MessageChannel {

    @Value("${app.channels.sms.fast2sms-api-key:}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public void send(Message message) throws Exception {
        log.info("Sending SMS for message id={} type={} priority={}",
                message.getId(), message.getType(), message.getPriority());

        if (apiKey == null || apiKey.isEmpty()) {
            log.warn("Fast2SMS API key not configured - simulating SMS");
            simulateSend(message);
            return;
        }

        String phone = extractPhoneNumber(message);
        String body = buildSmsBody(message);

        HttpHeaders headers = new HttpHeaders();
        headers.set("authorization", apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> payload = new HashMap<>();
        payload.put("route", "q");
        payload.put("message", body);
        payload.put("language", "english");
        payload.put("flash", 0);
        payload.put("numbers", phone);

        HttpEntity<Map<String, Object>> request =
                new HttpEntity<>(payload, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "https://www.fast2sms.com/dev/bulkV2",
                request,
                Map.class
        );

        if (response.getStatusCode() != HttpStatus.OK) {
            throw new Exception("Fast2SMS error: "
                    + response.getStatusCode());
        }

        Map<String, Object> responseBody = response.getBody();
        if (responseBody != null
                && Boolean.FALSE.equals(responseBody.get("return"))) {
            throw new Exception("Fast2SMS failed: "
                    + responseBody.get("message"));
        }

        log.info("SMS sent successfully for message id={} response={}",
                message.getId(), responseBody);
    }

    @Override
    public String getChannelName() {
        return "sms";
    }

    private String extractPhoneNumber(Message message) {
        if (message.getPayload() != null
                && message.getPayload().containsKey("phone")) {
            return message.getPayload().get("phone").toString();
        }
        throw new RuntimeException("No phone number in payload");
    }

    private String buildSmsBody(Message message) {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(message.getPriority()).append("] ");
        sb.append(message.getType()).append(": ");
        if (message.getPayload() != null
                && message.getPayload().containsKey("title")) {
            sb.append(message.getPayload().get("title"));
        }
        return sb.toString();
    }

    private void simulateSend(Message message) throws Exception {
        Thread.sleep(150);
        if (Math.random() < 0.1) {
            throw new Exception("SMS provider temporarily unavailable");
        }
    }
}