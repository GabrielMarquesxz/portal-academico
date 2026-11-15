-- MIGRAÇÃO NÃO DESTRUTIVA: adiciona a gestão acadêmica ao banco já existente.
-- Faça backup e execute este arquivo no MySQL 8 antes de iniciar a nova versão.
-- O script preserva usuários, salas, disciplinas e alocações e pode ser reexecutado.
USE portal_academico;

CREATE TABLE IF NOT EXISTS turmas (
    codigo VARCHAR(10) PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    curso VARCHAR(100)
);

-- Migra todos os códigos já usados, inclusive turmas que só apareçam em alocações antigas.
INSERT IGNORE INTO turmas (codigo, nome, curso)
SELECT existentes.codigo, CONCAT('Turma ', existentes.codigo), NULL
FROM (
    SELECT turma AS codigo FROM usuarios WHERE turma IS NOT NULL AND turma <> ''
    UNION
    SELECT turma AS codigo FROM alocacoes WHERE turma IS NOT NULL AND turma <> ''
) existentes;

-- Os IDs legados permanecem inalterados. O portal calcula o próximo ID dentro da
-- transação administrativa serializada, evitando remover chaves estrangeiras existentes.

DROP PROCEDURE IF EXISTS aplicar_integridade_gestao_academica;
DELIMITER $$
CREATE PROCEDURE aplicar_integridade_gestao_academica()
BEGIN
    -- Só cria as restrições únicas quando os dados legados são compatíveis.
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.statistics
        WHERE table_schema = DATABASE() AND table_name = 'salas'
          AND index_name = 'uk_salas_bloco_numero'
    ) AND NOT EXISTS (
        SELECT 1 FROM (
            SELECT bloco, numero FROM salas GROUP BY bloco, numero HAVING COUNT(*) > 1
        ) salas_duplicadas
    ) THEN
        ALTER TABLE salas
            ADD CONSTRAINT uk_salas_bloco_numero UNIQUE (bloco, numero);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.statistics
        WHERE table_schema = DATABASE() AND table_name = 'disciplinas'
          AND index_name = 'uk_disciplinas_nome'
    ) AND NOT EXISTS (
        SELECT 1 FROM (
            SELECT LOWER(nome) FROM disciplinas GROUP BY LOWER(nome) HAVING COUNT(*) > 1
        ) disciplinas_duplicadas
    ) THEN
        ALTER TABLE disciplinas
            ADD CONSTRAINT uk_disciplinas_nome UNIQUE (nome);
    END IF;

    -- Os códigos foram migrados acima; as FKs mantêm a coluna textual compatível com o sistema.
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.key_column_usage
        WHERE table_schema = DATABASE() AND table_name = 'usuarios'
          AND column_name = 'turma' AND referenced_table_name = 'turmas'
    ) AND NOT EXISTS (
        SELECT 1 FROM usuarios u LEFT JOIN turmas t ON t.codigo = u.turma
        WHERE u.turma IS NOT NULL AND t.codigo IS NULL
    ) THEN
        ALTER TABLE usuarios
            ADD CONSTRAINT fk_usuarios_turma FOREIGN KEY (turma) REFERENCES turmas(codigo);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.key_column_usage
        WHERE table_schema = DATABASE() AND table_name = 'alocacoes'
          AND column_name = 'turma' AND referenced_table_name = 'turmas'
    ) AND NOT EXISTS (
        SELECT 1 FROM alocacoes a LEFT JOIN turmas t ON t.codigo = a.turma
        WHERE t.codigo IS NULL
    ) THEN
        ALTER TABLE alocacoes
            ADD CONSTRAINT fk_alocacoes_turma FOREIGN KEY (turma) REFERENCES turmas(codigo);
    END IF;
END$$
DELIMITER ;

CALL aplicar_integridade_gestao_academica();
DROP PROCEDURE aplicar_integridade_gestao_academica;

-- Conferência final. Se "restricao_unica" for NÃO, há duplicatas legadas a revisar.
SELECT 'turmas migradas' AS item, COUNT(*) AS total FROM turmas
UNION ALL
SELECT 'salas', COUNT(*) FROM salas
UNION ALL
SELECT 'disciplinas', COUNT(*) FROM disciplinas;

SELECT
    CASE WHEN EXISTS (
        SELECT 1 FROM information_schema.statistics
        WHERE table_schema = DATABASE() AND table_name = 'salas'
          AND index_name = 'uk_salas_bloco_numero'
    ) THEN 'SIM' ELSE 'NÃO' END AS salas_com_restricao_unica,
    CASE WHEN EXISTS (
        SELECT 1 FROM information_schema.statistics
        WHERE table_schema = DATABASE() AND table_name = 'disciplinas'
          AND index_name = 'uk_disciplinas_nome'
    ) THEN 'SIM' ELSE 'NÃO' END AS disciplinas_com_restricao_unica;
