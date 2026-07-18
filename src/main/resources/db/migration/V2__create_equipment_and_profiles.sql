CREATE TABLE equipment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE user_profiles (
    user_id BIGINT PRIMARY KEY,
    birth_date DATE NULL,
    sex VARCHAR(10) NULL,
    height_cm INT NULL,
    weight_kg DECIMAL(5,2) NULL,
    experience_level VARCHAR(20) NOT NULL,
    primary_goal VARCHAR(30) NOT NULL,
    days_per_week INT NOT NULL,
    session_duration_minutes INT NOT NULL,
    notes VARCHAR(1000) NULL,
    CONSTRAINT fk_user_profiles_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE user_equipment (
    user_id BIGINT NOT NULL,
    equipment_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, equipment_id),
    CONSTRAINT fk_user_equipment_profile FOREIGN KEY (user_id) REFERENCES user_profiles (user_id) ON DELETE CASCADE,
    CONSTRAINT fk_user_equipment_equipment FOREIGN KEY (equipment_id) REFERENCES equipment (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE user_limitations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    muscle_group VARCHAR(30) NULL,
    note VARCHAR(500) NOT NULL,
    CONSTRAINT fk_user_limitations_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
