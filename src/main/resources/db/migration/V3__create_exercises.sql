CREATE TABLE exercises (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    description VARCHAR(1000) NULL,
    primary_muscle_group VARCHAR(30) NOT NULL,
    movement_pattern VARCHAR(20) NOT NULL,
    difficulty_level VARCHAR(20) NOT NULL,
    exercise_type VARCHAR(20) NOT NULL,
    instructions VARCHAR(2000) NULL,
    video_url VARCHAR(500) NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE exercise_equipment (
    exercise_id BIGINT NOT NULL,
    equipment_id BIGINT NOT NULL,
    PRIMARY KEY (exercise_id, equipment_id),
    CONSTRAINT fk_exercise_equipment_exercise FOREIGN KEY (exercise_id) REFERENCES exercises (id) ON DELETE CASCADE,
    CONSTRAINT fk_exercise_equipment_equipment FOREIGN KEY (equipment_id) REFERENCES equipment (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
