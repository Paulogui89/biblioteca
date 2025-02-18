CREATE TABLE IF NOT EXISTS livro (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    titulo VARCHAR(255) NOT NULL,
    autor VARCHAR(255) NOT NULL,
    ano INT NOT NULL,
    disponivel BOOLEAN NOT NULL
);


INSERT INTO livro (titulo, autor, ano, disponivel) VALUES ('Livro A', 'Autor A', 2020, true);
INSERT INTO livro (titulo, autor, ano, disponivel) VALUES ('Livro B', 'Autor B', 2019, true);
INSERT INTO livro (titulo, autor, ano, disponivel) VALUES ('Livro C', 'Autor C', 2021, true);