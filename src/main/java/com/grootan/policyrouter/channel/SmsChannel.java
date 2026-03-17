package com.grootan.policyrouter.channel;

import com.grootan.policyrouter.domain.model.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SmsChannel implements MessageChannel {

    @Override
    public void send(Message message) throws Exception {
        log.info("Sending SMS for message id={} type={} priority={}",
                message.getId(), message.getType(), message.getPriority());

        // TODO: Replace with real Twilio integration
        // Example Twilio call:
        // twilioClient.messages.create(buildSmsMessage(message));

        simulateSend(message);

        log.info("SMS sent successfully for message id={}", message.getId());
    }

    @Override
    public String getChannelName() {
        return "sms";
    }

    private void simulateSend(Message message) throws Exception {
        Thread.sleep(150);
        if (Math.random() < 0.1) {
            throw new Exception("SMS provider temporarily unavailable");
        }
    }
}