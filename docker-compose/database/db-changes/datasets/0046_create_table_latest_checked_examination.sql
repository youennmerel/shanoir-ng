CREATE TABLE latest_checked_examination (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    examination_id BIGINT NOT NULL,
    FOREIGN KEY (examination_id) REFERENCES examination (id)
);
