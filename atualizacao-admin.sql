-- MIGRAÇÃO NÃO DESTRUTIVA para quem já executou banco.sql na versão anterior.
-- Execute SOMENTE UMA VEZ (pode ser repetido se o admin já tiver sido criado).
-- Não use banco.sql novamente para essa atualização: ele recria as cinco tabelas.
USE portal_academico;

INSERT INTO perfis (id, nome)
SELECT 3, 'ADMIN'
WHERE NOT EXISTS (SELECT 1 FROM perfis WHERE id = 3 OR nome = 'ADMIN');

-- Conta demonstrativa: admin1 / senha123, com o hash de teste já existente em prof1.
INSERT INTO usuarios (id, nome, login, senha_hash, perfil_id, turma)
SELECT 6, 'Coordenação Acadêmica', 'admin1', u.senha_hash, p.id, NULL
FROM usuarios u
JOIN perfis p ON p.nome = 'ADMIN'
WHERE u.login = 'prof1'
  AND NOT EXISTS (SELECT 1 FROM usuarios x WHERE x.id = 6 OR x.login = 'admin1');

SELECT u.login, u.nome, p.nome AS perfil
FROM usuarios u JOIN perfis p ON p.id = u.perfil_id
WHERE u.login = 'admin1';
