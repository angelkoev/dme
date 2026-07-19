-- Dev-only seed data. This file lives outside src/main/resources/db/migration
-- specifically so it is NOT part of the default Flyway migration path: only
-- application-dev.yml adds "classpath:db/dev-migration" to spring.flyway.locations,
-- so this never runs against a real/shared/production database, no matter
-- which schema Flyway is pointed at, unless someone explicitly opts in the
-- same way the dev profile does.
--
-- Password for both accounts: Test1234!  (bcrypt-hashed below; change/remove
-- this file entirely before using this project as a base for anything with
-- real users.)

INSERT INTO users (username, email, password_hash, enabled, created_at) VALUES
    ('testuser', 'testuser@example.com', '$2a$10$wzR2abEGJ8LMni8EipqFJ.63XZ8cY1oagzXODOBtqY42r7EM2P3eK', TRUE, CURRENT_TIMESTAMP),
    ('testadmin', 'testadmin@example.com', '$2a$10$.NYyHNDQR5YssPnThoZV4OcC7rNpSw1PMMwqwECGbGh0.0VoRL9hu', TRUE, CURRENT_TIMESTAMP);

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r WHERE u.username = 'testuser' AND r.name = 'ROLE_USER';

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r WHERE u.username = 'testadmin' AND r.name IN ('ROLE_USER', 'ROLE_ADMIN');

-- testuser gets a ready-to-go profile so a fresh checkout can generate a
-- plan immediately without filling in /profile first. testadmin is left
-- profile-less on purpose, so the "no profile yet" state is still there to
-- click through if you want to see it.
INSERT INTO user_profiles (user_id, birth_date, sex, height_cm, weight_kg, experience_level, primary_goal,
                            days_per_week, session_duration_minutes, notes, location)
SELECT u.id, '1996-04-12', 'MALE', 178, 80.00, 'INTERMEDIATE', 'HYPERTROPHY', 4, 60,
       'Seeded test account for manual/demo testing.', 'GYM'
FROM users u WHERE u.username = 'testuser';

INSERT INTO user_equipment (user_id, equipment_id)
SELECT u.id, eq.id FROM users u, equipment eq
WHERE u.username = 'testuser' AND eq.name IN ('Bodyweight', 'Dumbbell', 'Barbell', 'Bench', 'Pull-up Bar');

INSERT INTO user_preferred_categories (user_id, muscle_group)
SELECT u.id, 'CHEST' FROM users u WHERE u.username = 'testuser';

INSERT INTO workout_streaks (user_id, current_streak, longest_streak, last_workout_date)
SELECT u.id, 3, 5, CURDATE() FROM users u WHERE u.username = 'testuser';
