CREATE TABLE routing_rules (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    priority INT NOT NULL DEFAULT 0,
    conditions JSONB NOT NULL,
    channels TEXT[],
    fallback TEXT[],
    retry_count INT DEFAULT 3,
    retry_delay_ms INT DEFAULT 5000,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_rules_priority ON routing_rules(priority DESC) WHERE is_active = TRUE;

INSERT INTO routing_rules (name, priority, conditions, channels, fallback, retry_count, retry_delay_ms) VALUES
('Critical alerts', 100, '{"type":"ALERT","priority":"CRITICAL"}', ARRAY['sms','email'], ARRAY['email'], 5, 3000),
('Promotions email only', 50, '{"type":"PROMOTION"}', ARRAY['email'], NULL, 2, 10000),
('OTP sms with fallback', 90, '{"type":"OTP"}', ARRAY['sms'], ARRAY['email'], 3, 2000),
('General notifications', 10, '{"type":"NOTIFICATION"}', ARRAY['email'], NULL, 2, 5000);