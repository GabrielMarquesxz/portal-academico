-- PROJETO DIDÁTICO: execute no MySQL 8 (Workbench ou cliente MySQL).
-- ATENÇÃO: executá-lo novamente recria as cinco tabelas do exemplo.
CREATE DATABASE IF NOT EXISTS portal_academico
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE portal_academico;

DROP TABLE IF EXISTS alocacoes;
DROP TABLE IF EXISTS usuarios;
DROP TABLE IF EXISTS disciplinas;
DROP TABLE IF EXISTS salas;
DROP TABLE IF EXISTS perfis;

CREATE TABLE perfis (
    id INT PRIMARY KEY,
    nome VARCHAR(20) NOT NULL UNIQUE
);

CREATE TABLE usuarios (
    id INT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    login VARCHAR(40) NOT NULL UNIQUE,
    senha_hash VARCHAR(100) NOT NULL,
    perfil_id INT NOT NULL,
    turma VARCHAR(10),
    FOREIGN KEY (perfil_id) REFERENCES perfis(id)
);

CREATE TABLE salas (
    id INT PRIMARY KEY,
    bloco VARCHAR(10) NOT NULL,
    numero VARCHAR(10) NOT NULL,
    tipo VARCHAR(20) NOT NULL -- TEORICA ou LABORATORIO
);

CREATE TABLE disciplinas (
    id INT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL
);

CREATE TABLE alocacoes (
    id INT PRIMARY KEY AUTO_INCREMENT,
    disciplina_id INT NOT NULL,
    professor_id INT NOT NULL,
    turma VARCHAR(10) NOT NULL,
    sala_id INT NOT NULL,
    dia_semana INT NOT NULL, -- 1=segunda, ... 7=domingo
    hora_inicio TIME NOT NULL,
    hora_fim TIME NOT NULL,
    FOREIGN KEY (disciplina_id) REFERENCES disciplinas(id),
    FOREIGN KEY (professor_id) REFERENCES usuarios(id),
    FOREIGN KEY (sala_id) REFERENCES salas(id),
    CHECK (dia_semana BETWEEN 1 AND 7),
    CHECK (hora_fim > hora_inicio)
);

INSERT INTO perfis (id, nome) VALUES
    (1, 'ALUNO'), (2, 'PROFESSOR');

-- Cinco usuários; senha de TESTE: senha123 (hash BCrypt, não texto puro).
INSERT INTO usuarios (id, nome, login, senha_hash, perfil_id, turma) VALUES
    (1, 'Ana Souza', 'aluno1', '$2y$10$8dnSUK6G9WjL.zhQtoxHM.gRZhvf8eH/S73X54Uofa6nR6Ku4SOwO', 1, 'ADS1'),
    (2, 'Bruno Lima', 'aluno2', '$2y$10$r7W3aMeTTJouI/NNR9Kp2.3p8YuXHDmCPadp.XLYm.NRhJaVjjk92', 1, 'ADS2'),
    (3, 'Carla Mendes', 'prof1', '$2y$10$y6mjSo.wyChNud7YuskZ7uV0T7nEbReFqzUVKIJuPQM95iJ1dn6iO', 2, NULL),
    (4, 'Daniel Costa', 'prof2', '$2y$10$8kQ/VLz0AWs2cI2mKpwChuYufy0xDz82r1KVxSEdyO0Rd9Az9kWGO', 2, NULL);

INSERT INTO usuarios
    (id, nome, login, senha_hash, perfil_id, turma)
SELECT
    5, 'Marcos Oliveira', 'prof3',
    senha_hash, perfil_id, NULL
FROM usuarios
WHERE login = 'prof1';

INSERT INTO salas (id, bloco, numero, tipo) VALUES
    (1, 'A', '101', 'TEORICA'),
    (2, 'B', 'L02', 'LABORATORIO');

INSERT INTO disciplinas (id, nome) VALUES
    (1, 'Banco de Dados'),
    (2, 'Programação Web');

-- Turmas com aulas de segunda a sexta-feira.
-- Cada professor tem aulas teóricas e práticas durante a semana.
INSERT INTO alocacoes
    (disciplina_id, professor_id, turma, sala_id, dia_semana, hora_inicio, hora_fim)
VALUES
  (1, 3, 'ADS1', 1, 1, '08:00:00', '09:40:00'),
  (2, 4, 'ADS2', 2, 1, '14:00:00', '15:40:00'),
  (1, 3, 'ADS1', 2, 2, '08:00:00', '09:40:00'),
  (2, 4, 'ADS2', 1, 2, '14:00:00', '15:40:00'),
  (1, 3, 'ADS1', 1, 3, '08:00:00', '09:40:00'),
  (2, 4, 'ADS2', 2, 3, '14:00:00', '15:40:00'),
  (1, 3, 'ADS1', 2, 4, '08:00:00', '09:40:00'),
  (2, 4, 'ADS2', 1, 4, '14:00:00', '15:40:00'),
  (1, 3, 'ADS1', 1, 5, '08:00:00', '09:40:00'),
  (2, 4, 'ADS2', 2, 5, '14:00:00', '15:40:00');
