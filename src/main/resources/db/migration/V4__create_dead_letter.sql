CREATE TABLE dead_letter_queue (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    message_id UUID REFERENCES messages(id),
    channel VARCHAR(50),
    reason TEXT,
    payload JSONB,
    created_at TIMESTAMP DEFAULT NOW()
);