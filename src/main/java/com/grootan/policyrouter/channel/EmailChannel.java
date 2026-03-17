package com.grootan.policyrouter.channel;

import com.grootan.policyrouter.domain.model.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EmailChannel implements MessageChannel {

    @Override
    public void send(Message message) throws Exception {
        log.info("Sending EMAIL for message id={} type={} priority={}",
                message.getId(), message.getType(), message.getPriority());

        // TODO: Replace with real SendGrid/JavaMail integration
        // Example SendGrid call:
        // sendGridClient.send(buildEmail(message));

        simulateSend(message);

        log.info("EMAIL sent successfully for message id={}", message.getId());
    }

    @Override
    public String getChannelName() {
        return "email";
    }

    private void simulateSend(Message message) throws Exception {
        // Simulate real network call delay
        Thread.sleep(100);
        // Simulate occasional failure for testing retry
        if (Math.random() < 0.1) {
            throw new Exception("Email provider temporarily unavailable");
        }
    }
}