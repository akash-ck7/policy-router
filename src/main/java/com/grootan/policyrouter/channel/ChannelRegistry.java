package com.grootan.policyrouter.channel;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class ChannelRegistry {

    private final Map<String, MessageChannel> channels = new HashMap<>();

    public ChannelRegistry(List<MessageChannel> channelList) {
        for (MessageChannel channel : channelList) {
            channels.put(channel.getChannelName().toLowerCase(), channel);
            log.info("Registered channel: {}", channel.getChannelName());
        }
    }

    public MessageChannel resolve(String channelName) {
        MessageChannel channel = channels.get(channelName.toLowerCase());
        if (channel == null) {
            throw new IllegalArgumentException("No channel registered with name: " + channelName);
        }
        return channel;
    }

    public boolean exists(String channelName) {
        return channels.containsKey(channelName.toLowerCase());
    }
}