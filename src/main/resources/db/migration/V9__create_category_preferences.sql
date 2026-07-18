CREATE TABLE user_preferred_categories (
    user_id BIGINT NOT NULL,
    muscle_group VARCHAR(30) NOT NULL,
    PRIMARY KEY (user_id, muscle_group),
    CONSTRAINT fk_user_preferred_categories_profile FOREIGN KEY (user_id) REFERENCES user_profiles (user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE user_unwanted_categories (
    user_id BIGINT NOT NULL,
    muscle_group VARCHAR(30) NOT NULL,
    PRIMARY KEY (user_id, muscle_group),
    CONSTRAINT fk_user_unwanted_categories_profile FOREIGN KEY (user_id) REFERENCES user_profiles (user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
