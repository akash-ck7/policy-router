CREATE TABLE user_preferences (
    user_id UUID NOT NULL,
    channel VARCHAR(50) NOT NULL,
    opted_in BOOLEAN DEFAULT TRUE,
    time_window_start TIME,
    time_window_end TIME,
    PRIMARY KEY (user_id, channel)
);