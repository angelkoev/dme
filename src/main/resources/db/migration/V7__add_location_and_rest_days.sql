ALTER TABLE user_profiles
    ADD COLUMN location VARCHAR(20) NOT NULL DEFAULT 'ANYWHERE';

CREATE TABLE user_rest_days (
    user_id BIGINT NOT NULL,
    day_of_week VARCHAR(10) NOT NULL,
    PRIMARY KEY (user_id, day_of_week),
    CONSTRAINT fk_user_rest_days_profile FOREIGN KEY (user_id) REFERENCES user_profiles (user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
