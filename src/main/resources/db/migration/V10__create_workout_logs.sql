CREATE TABLE workout_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    workout_session_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    performed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completion_percentage INT NOT NULL,
    rating INT NULL,
    perceived_intensity INT NULL,
    notes VARCHAR(500) NULL,
    CONSTRAINT fk_workout_logs_session FOREIGN KEY (workout_session_id) REFERENCES workout_sessions (id) ON DELETE CASCADE,
    CONSTRAINT fk_workout_logs_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_workout_logs_user_performed_at ON workout_logs (user_id, performed_at);
