package com.grootan.policyrouter.channel;

import com.grootan.policyrouter.domain.model.Message;
import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EmailChannel implements MessageChannel {

    @Value("${app.channels.email.sendgrid-api-key:}")
    private String sendGridApiKey;

    @Value("${app.channels.email.from}")
    private String fromEmail;

    @Override
    public void send(Message message) throws Exception {
        log.info("Sending EMAIL for message id={} type={} priority={}",
                message.getId(), message.getType(), message.getPriority());

        if (sendGridApiKey == null || sendGridApiKey.isEmpty()) {
            log.warn("SendGrid API key not configured - simulating email");
            simulateSend(message);
            return;
        }

        String toEmail = extractEmail(message);
        String subject = buildSubject(message);
        String body = buildBody(message);

        Email from = new Email(fromEmail);
        Email to = new Email(toEmail);
        Content content = new Content("text/plain", body);
        Mail mail = new Mail(from, subject, to, content);

        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());

        Response response = sg.api(request);

        if (response.getStatusCode() >= 400) {
            throw new Exception("SendGrid error: "
                    + response.getStatusCode()
                    + " " + response.getBody());
        }

        log.info("EMAIL sent successfully for message id={} status={}",
                message.getId(), response.getStatusCode());
    }

    @Override
    public String getChannelName() {
        return "email";
    }

    private String extractEmail(Message message) {
        if (message.getPayload() != null
                && message.getPayload().containsKey("email")) {
            return message.getPayload().get("email").toString();
        }
        return fromEmail;
    }

    private String buildSubject(Message message) {
        return "[" + message.getPriority() + "] "
                + message.getType() + " Notification";
    }

    private String buildBody(Message message) {
        StringBuilder sb = new StringBuilder();
        sb.append("Message Type: ").append(message.getType()).append("\n");
        sb.append("Priority: ").append(message.getPriority()).append("\n");
        sb.append("Message ID: ").append(message.getId()).append("\n\n");
        if (message.getPayload() != null) {
            message.getPayload().forEach((k, v) ->
                    sb.append(k).append(": ").append(v).append("\n"));
        }
        return sb.toString();
    }

    private void simulateSend(Message message) throws Exception {
        Thread.sleep(100);
        if (Math.random() < 0.1) {
            throw new Exception("Email provider temporarily unavailable");
        }
    }
}