package com.grootan.policyrouter.channel;

import com.grootan.policyrouter.domain.model.Message;

public interface MessageChannel {
    void send(Message message) throws Exception;
    String getChannelName();
}