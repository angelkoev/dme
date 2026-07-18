INSERT INTO exercises (name, description, primary_muscle_group, movement_pattern, difficulty_level, exercise_type, instructions) VALUES
    ('Push-up', 'Bodyweight chest press', 'CHEST', 'PUSH', 'BEGINNER', 'COMPOUND', 'Lower chest to the floor, press back up keeping the core tight.'),
    ('Barbell Bench Press', 'Flat barbell press for chest', 'CHEST', 'PUSH', 'INTERMEDIATE', 'COMPOUND', 'Lie on the bench, lower the bar to the chest, press up.'),
    ('Dumbbell Chest Fly', 'Isolation chest exercise', 'CHEST', 'PUSH', 'INTERMEDIATE', 'ISOLATION', 'Lie on the bench, arc the dumbbells out and back with a slight elbow bend.'),
    ('Pull-up', 'Bodyweight back and biceps compound', 'BACK', 'PULL', 'INTERMEDIATE', 'COMPOUND', 'Hang from the bar, pull chin above the bar, lower under control.'),
    ('Bent-over Barbell Row', 'Compound back exercise', 'BACK', 'PULL', 'INTERMEDIATE', 'COMPOUND', 'Hinge at the hips, row the bar to the lower ribs.'),
    ('Dumbbell Row', 'Single-arm back row', 'BACK', 'PULL', 'BEGINNER', 'COMPOUND', 'Support one knee on the bench, row the dumbbell to the hip.'),
    ('Lat Pulldown', 'Machine back pull', 'BACK', 'PULL', 'BEGINNER', 'COMPOUND', 'Pull the bar down to the upper chest, control the return.'),
    ('Resistance Band Row', 'Band back row', 'BACK', 'PULL', 'BEGINNER', 'COMPOUND', 'Anchor the band, row the handles to the ribs squeezing the shoulder blades.'),
    ('Overhead Press', 'Standing barbell shoulder press', 'SHOULDERS', 'PUSH', 'INTERMEDIATE', 'COMPOUND', 'Press the bar overhead from the shoulders, keep the core braced.'),
    ('Dumbbell Shoulder Press', 'Seated or standing shoulder press', 'SHOULDERS', 'PUSH', 'BEGINNER', 'COMPOUND', 'Press the dumbbells overhead from shoulder height.'),
    ('Lateral Raise', 'Isolation for side delts', 'SHOULDERS', 'PUSH', 'BEGINNER', 'ISOLATION', 'Raise the dumbbells out to the sides to shoulder height.'),
    ('Dumbbell Bicep Curl', 'Isolation biceps exercise', 'BICEPS', 'PULL', 'BEGINNER', 'ISOLATION', 'Curl the dumbbells to the shoulders keeping the elbows still.'),
    ('Barbell Curl', 'Isolation biceps exercise', 'BICEPS', 'PULL', 'BEGINNER', 'ISOLATION', 'Curl the bar up keeping the elbows pinned to the sides.'),
    ('Triceps Dip', 'Bodyweight triceps compound', 'TRICEPS', 'PUSH', 'INTERMEDIATE', 'COMPOUND', 'Lower the body between the bench edges, press back up.'),
    ('Triceps Pushdown', 'Cable isolation for triceps', 'TRICEPS', 'PUSH', 'BEGINNER', 'ISOLATION', 'Push the cable attachment down until the arms are straight.'),
    ('Back Squat', 'Barbell squat', 'QUADRICEPS', 'LEGS', 'INTERMEDIATE', 'COMPOUND', 'Bar on the upper back, squat to depth, drive back up.'),
    ('Goblet Squat', 'Kettlebell squat variation', 'QUADRICEPS', 'LEGS', 'BEGINNER', 'COMPOUND', 'Hold the kettlebell at the chest, squat between the knees.'),
    ('Leg Press', 'Machine leg press', 'QUADRICEPS', 'LEGS', 'BEGINNER', 'COMPOUND', 'Press the platform away, control the return without locking the knees.'),
    ('Romanian Deadlift', 'Hip hinge for hamstrings', 'HAMSTRINGS', 'LEGS', 'INTERMEDIATE', 'COMPOUND', 'Hinge at the hips keeping the bar close to the legs, feel the hamstring stretch.'),
    ('Kettlebell Swing', 'Ballistic hip hinge', 'HAMSTRINGS', 'LEGS', 'INTERMEDIATE', 'COMPOUND', 'Hinge and snap the hips to swing the kettlebell to chest height.'),
    ('Barbell Hip Thrust', 'Glute-focused hip extension', 'GLUTES', 'LEGS', 'INTERMEDIATE', 'COMPOUND', 'Upper back on the bench, drive the hips up squeezing the glutes.'),
    ('Bodyweight Lunge', 'Unilateral leg exercise', 'GLUTES', 'LEGS', 'BEGINNER', 'COMPOUND', 'Step forward and lower the back knee toward the floor.'),
    ('Standing Calf Raise', 'Isolation for calves', 'CALVES', 'LEGS', 'BEGINNER', 'ISOLATION', 'Rise onto the toes, pause, lower under control.'),
    ('Plank', 'Isometric core hold', 'CORE', 'CORE', 'BEGINNER', 'ISOLATION', 'Hold a straight line from head to heels, brace the core.'),
    ('Hanging Leg Raise', 'Advanced core exercise', 'CORE', 'CORE', 'ADVANCED', 'ISOLATION', 'Hang from the bar, raise the legs to hip height without swinging.'),
    ('Russian Twist', 'Rotational core exercise', 'CORE', 'CORE', 'BEGINNER', 'ISOLATION', 'Seated, lean back slightly and rotate the torso side to side.'),
    ('Burpee', 'Full body conditioning move', 'FULL_BODY', 'FULL_BODY', 'INTERMEDIATE', 'COMPOUND', 'Squat, kick back to a plank, push up, jump feet in, jump up.'),
    ('Kettlebell Clean and Press', 'Full body power exercise', 'FULL_BODY', 'FULL_BODY', 'ADVANCED', 'COMPOUND', 'Clean the kettlebell to the rack position, press overhead.');

INSERT INTO exercise_equipment (exercise_id, equipment_id)
SELECT e.id, eq.id FROM exercises e, equipment eq WHERE e.name = 'Push-up' AND eq.name = 'Bodyweight';
INSERT INTO exercise_equipment (exercise_id, equipment_id)
SELECT e.id, eq.id FROM exercises e, equipment eq WHERE e.name = 'Barbell Bench Press' AND eq.name = 'Barbell';
INSERT INTO exercise_equipment (exercise_id, equipment_id)
SELECT e.id, eq.id FROM exercises e, equipment eq WHERE e.name = 'Barbell Bench Press' AND eq.name = 'Bench';
INSERT INTO exercise_equipment (exercise_id, equipment_id)
SELECT e.id, eq.id FROM exercises e, equipment eq WHERE e.name = 'Dumbbell Chest Fly' AND eq.name = 'Dumbbell';
INSERT INTO exercise_equipment (exercise_id, equipment_id)
SELECT e.id, eq.id FROM exercises e, equipment eq WHERE e.name = 'Dumbbell Chest Fly' AND eq.name = 'Bench';
INSERT INTO exercise_equipment (exercise_id, equipment_id)
SELECT e.id, eq.id FROM exercises e, equipment eq WHERE e.name = 'Pull-up' AND eq.name = 'Pull-up Bar';
INSERT INTO exercise_equipment (exercise_id, equipment_id)
SELECT e.id, eq.id FROM exercises e, equipment eq WHERE e.name = 'Bent-over Barbell Row' AND eq.name = 'Barbell';
INSERT INTO exercise_equipment (exercise_id, equipment_id)
SELECT e.id, eq.id FROM exercises e, equipment eq WHERE e.name = 'Dumbbell Row' AND eq.name = 'Dumbbell';
INSERT INTO exercise_equipment (exercise_id, equipment_id)
SELECT e.id, eq.id FROM exercises e, equipment eq WHERE e.name = 'Dumbbell Row' AND eq.name = 'Bench';
INSERT INTO exercise_equipment (exercise_id, equipment_id)
SELECT e.id, eq.id FROM exercises e, equipment eq WHERE e.name = 'Lat Pulldown' AND eq.name = 'Cable Machine';
INSERT INTO exercise_equipment (exercise_id, equipment_id)
SELECT e.id, eq.id FROM exercises e, equipment eq WHERE e.name = 'Resistance Band Row' AND eq.name = 'Resistance Band';
INSERT INTO exercise_equipment (exercise_id, equipment_id)
SELECT e.id, eq.id FROM exercises e, equipment eq WHERE e.name = 'Overhead Press' AND eq.name = 'Barbell';
INSERT INTO exercise_equipment (exercise_id, equipment_id)
SELECT e.id, eq.id FROM exercises e, equipment eq WHERE e.name = 'Dumbbell Shoulder Press' AND eq.name = 'Dumbbell';
INSERT INTO exercise_equipment (exercise_id, equipment_id)
SELECT e.id, eq.id FROM exercises e, equipment eq WHERE e.name = 'Lateral Raise' AND eq.name = 'Dumbbell';
INSERT INTO exercise_equipment (exercise_id, equipment_id)
SELECT e.id, eq.id FROM exercises e, equipment eq WHERE e.name = 'Dumbbell Bicep Curl' AND eq.name = 'Dumbbell';
INSERT INTO exercise_equipment (exercise_id, equipment_id)
SELECT e.id, eq.id FROM exercises e, equipment eq WHERE e.name = 'Barbell Curl' AND eq.name = 'Barbell';
INSERT INTO exercise_equipment (exercise_id, equipment_id)
SELECT e.id, eq.id FROM exercises e, equipment eq WHERE e.name = 'Triceps Dip' AND eq.name = 'Bodyweight';
INSERT INTO exercise_equipment (exercise_id, equipment_id)
SELECT e.id, eq.id FROM exercises e, equipment eq WHERE e.name = 'Triceps Dip' AND eq.name = 'Bench';
INSERT INTO exercise_equipment (exercise_id, equipment_id)
SELECT e.id, eq.id FROM exercises e, equipment eq WHERE e.name = 'Triceps Pushdown' AND eq.name = 'Cable Machine';
INSERT INTO exercise_equipment (exercise_id, equipment_id)
SELECT e.id, eq.id FROM exercises e, equipment eq WHERE e.name = 'Back Squat' AND eq.name = 'Barbell';
INSERT INTO exercise_equipment (exercise_id, equipment_id)
SELECT e.id, eq.id FROM exercises e, equipment eq WHERE e.name = 'Goblet Squat' AND eq.name = 'Kettlebell';
INSERT INTO exercise_equipment (exercise_id, equipment_id)
SELECT e.id, eq.id FROM exercises e, equipment eq WHERE e.name = 'Leg Press' AND eq.name = 'Machine';
INSERT INTO exercise_equipment (exercise_id, equipment_id)
SELECT e.id, eq.id FROM exercises e, equipment eq WHERE e.name = 'Romanian Deadlift' AND eq.name = 'Barbell';
INSERT INTO exercise_equipment (exercise_id, equipment_id)
SELECT e.id, eq.id FROM exercises e, equipment eq WHERE e.name = 'Kettlebell Swing' AND eq.name = 'Kettlebell';
INSERT INTO exercise_equipment (exercise_id, equipment_id)
SELECT e.id, eq.id FROM exercises e, equipment eq WHERE e.name = 'Barbell Hip Thrust' AND eq.name = 'Barbell';
INSERT INTO exercise_equipment (exercise_id, equipment_id)
SELECT e.id, eq.id FROM exercises e, equipment eq WHERE e.name = 'Barbell Hip Thrust' AND eq.name = 'Bench';
INSERT INTO exercise_equipment (exercise_id, equipment_id)
SELECT e.id, eq.id FROM exercises e, equipment eq WHERE e.name = 'Bodyweight Lunge' AND eq.name = 'Bodyweight';
INSERT INTO exercise_equipment (exercise_id, equipment_id)
SELECT e.id, eq.id FROM exercises e, equipment eq WHERE e.name = 'Standing Calf Raise' AND eq.name = 'Bodyweight';
INSERT INTO exercise_equipment (exercise_id, equipment_id)
SELECT e.id, eq.id FROM exercises e, equipment eq WHERE e.name = 'Plank' AND eq.name = 'Bodyweight';
INSERT INTO exercise_equipment (exercise_id, equipment_id)
SELECT e.id, eq.id FROM exercises e, equipment eq WHERE e.name = 'Hanging Leg Raise' AND eq.name = 'Pull-up Bar';
INSERT INTO exercise_equipment (exercise_id, equipment_id)
SELECT e.id, eq.id FROM exercises e, equipment eq WHERE e.name = 'Russian Twist' AND eq.name = 'Bodyweight';
INSERT INTO exercise_equipment (exercise_id, equipment_id)
SELECT e.id, eq.id FROM exercises e, equipment eq WHERE e.name = 'Burpee' AND eq.name = 'Bodyweight';
INSERT INTO exercise_equipment (exercise_id, equipment_id)
SELECT e.id, eq.id FROM exercises e, equipment eq WHERE e.name = 'Kettlebell Clean and Press' AND eq.name = 'Kettlebell';
