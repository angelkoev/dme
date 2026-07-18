CREATE TABLE user_favorite_exercises (
    user_id BIGINT NOT NULL,
    exercise_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, exercise_id),
    CONSTRAINT fk_user_favorite_exercises_profile FOREIGN KEY (user_id) REFERENCES user_profiles (user_id) ON DELETE CASCADE,
    CONSTRAINT fk_user_favorite_exercises_exercise FOREIGN KEY (exercise_id) REFERENCES exercises (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE user_disliked_exercises (
    user_id BIGINT NOT NULL,
    exercise_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, exercise_id),
    CONSTRAINT fk_user_disliked_exercises_profile FOREIGN KEY (user_id) REFERENCES user_profiles (user_id) ON DELETE CASCADE,
    CONSTRAINT fk_user_disliked_exercises_exercise FOREIGN KEY (exercise_id) REFERENCES exercises (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
