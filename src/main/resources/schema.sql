CREATE TABLE IF NOT EXISTS users (
                                     user_id INT AUTO_INCREMENT PRIMARY KEY,
                                     username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    );

-- 학습 자료 테이블
CREATE TABLE IF NOT EXISTS study_materials (
                                               material_id INT AUTO_INCREMENT PRIMARY KEY,
                                               title VARCHAR(200) NOT NULL,
    description TEXT,
    content TEXT,
    category VARCHAR(50),
    file_path VARCHAR(255),
    user_id INT,
    group_id INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
    );

-- 스터디 그룹 테이블
CREATE TABLE IF NOT EXISTS study_groups (
                                            group_id INT AUTO_INCREMENT PRIMARY KEY,
                                            name VARCHAR(100) NOT NULL,
    description TEXT,
    created_by INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES users(user_id)
    );

-- 그룹 멤버십 테이블
CREATE TABLE IF NOT EXISTS group_members (
                                             member_id INT AUTO_INCREMENT PRIMARY KEY,
                                             group_id INT,
                                             user_id INT,
                                             role VARCHAR(20) NOT NULL,
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (group_id) REFERENCES study_groups(group_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id)
    );

-- 채팅 메시지 테이블
CREATE TABLE IF NOT EXISTS chat_messages (
                                             message_id INT AUTO_INCREMENT PRIMARY KEY,
                                             group_id INT,
                                             user_id INT,
                                             message TEXT NOT NULL,
                                             sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                             FOREIGN KEY (group_id) REFERENCES study_groups(group_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id)
    );

-- 퀴즈 테이블
CREATE TABLE IF NOT EXISTS quizzes (
                                       quiz_id INT AUTO_INCREMENT PRIMARY KEY,
                                       title VARCHAR(200) NOT NULL,
    description TEXT,
    material_id INT,
    created_by INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (material_id) REFERENCES study_materials(material_id),
    FOREIGN KEY (created_by) REFERENCES users(user_id)
    );

-- 퀴즈 문제 테이블
CREATE TABLE IF NOT EXISTS quiz_questions (
                                              question_id INT AUTO_INCREMENT PRIMARY KEY,
                                              quiz_id INT,
                                              question TEXT NOT NULL,
                                              answer TEXT NOT NULL,
                                              options TEXT,
                                              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                              FOREIGN KEY (quiz_id) REFERENCES quizzes(quiz_id)
    );