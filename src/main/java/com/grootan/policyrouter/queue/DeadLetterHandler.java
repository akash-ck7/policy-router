package com.grootan.policyrouter.queue;

import com.grootan.policyrouter.domain.model.DeadLetterEntry;
import com.grootan.policyrouter.domain.repository.DeadLetterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeadLetterHandler {

    private final DeadLetterRepository deadLetterRepository;

    @Scheduled(fixedDelay = 60000)
    public void monitor() {
        List<DeadLetterEntry> entries = deadLetterRepository.findAll();
        if (!entries.isEmpty()) {
            log.warn("DLQ has {} unprocessed message(s):", entries.size());
            entries.forEach(e ->
                    log.warn("  DLQ entry id={} messageId={} channel={} reason={}",
                            e.getId(), e.getMessageId(), e.getChannel(), e.getReason())
            );
        }
    }
}