package com.grootan.policyrouter;

import com.grootan.policyrouter.channel.ChannelRegistry;
import com.grootan.policyrouter.channel.EmailChannel;
import com.grootan.policyrouter.channel.MessageChannel;
import com.grootan.policyrouter.channel.SmsChannel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ChannelRegistryTest {

    private ChannelRegistry channelRegistry;

    @BeforeEach
    void setup() {
        List<MessageChannel> channels = List.of(
                new EmailChannel(),
                new SmsChannel()
        );
        channelRegistry = new ChannelRegistry(channels);
    }

    @Test
    void shouldResolveEmailChannel() {
        MessageChannel channel = channelRegistry.resolve("email");
        assertNotNull(channel);
        assertEquals("email", channel.getChannelName());
    }

    @Test
    void shouldResolveSmsChannel() {
        MessageChannel channel = channelRegistry.resolve("sms");
        assertNotNull(channel);
        assertEquals("sms", channel.getChannelName());
    }

    @Test
    void shouldResolveChannelCaseInsensitive() {
        MessageChannel channel = channelRegistry.resolve("EMAIL");
        assertNotNull(channel);
        assertEquals("email", channel.getChannelName());
    }

    @Test
    void shouldThrowForUnknownChannel() {
        assertThrows(IllegalArgumentException.class, () ->
                channelRegistry.resolve("whatsapp")
        );
    }

    @Test
    void shouldReturnTrueForExistingChannel() {
        assertTrue(channelRegistry.exists("email"));
        assertTrue(channelRegistry.exists("sms"));
    }

    @Test
    void shouldReturnFalseForMissingChannel() {
        assertFalse(channelRegistry.exists("push"));
    }
}