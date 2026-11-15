# Portal Acadêmico — versão 4 (Gestão Acadêmica)

Aplicação Java para consulta de aulas e **gestão acadêmica**, com Spring Boot, JSF (JoinFaces), JDBC/JdbcTemplate e MySQL. A interface em verde, branco e laranja é inspirada na identidade visual da UNICEPLAC, mas **não é um sistema oficial da instituição**.

## Funcionalidades

- **Aluno:** vê aulas de hoje e a grade semanal de sua turma; filtra por dia.
- **Professor:** vê suas alocações da semana, a agenda do dia, o tipo de sala e a turma.
- **Coordenação (`ADMIN`):** cadastra turmas, alunos, professores, salas/laboratórios e disciplinas; cadastra e exclui alocações; consulta todos os registros administrativos.
- **Cadastros integrados:** qualquer professor, turma, disciplina ou ambiente criado no painel fica imediatamente disponível no formulário de alocações.
- **Senhas:** novos usuários recebem perfil automaticamente e a senha inicial é armazenada somente como hash BCrypt.
- **Integridade:** login e código de turma únicos, validação de turma existente, bloqueio de salas duplicadas e disciplinas repetidas sem diferenciar maiúsculas e minúsculas.
- **Validações do cadastro:** campos obrigatórios, referências válidas, horário de início menor que término, apenas segunda a sexta e ausência de sobreposição de horário para **sala, professor e turma**.
- Login validado no MySQL, senha demonstrativa protegida por hash BCrypt, sessão e autorização por perfil.

Os cadastros administrativos são persistidos diretamente no MySQL. O painel do aluno consulta aulas pelo código da turma e o painel do professor pelo seu ID; por isso uma nova alocação aparece nos dois painéis ao abrir ou recarregar a página.

## Tecnologias e arquitetura

- **Java 17**, Spring Boot **3.5.15**, JoinFaces **5.5.15**, MySQL 8, Maven 3.9+.
- **View:** `login.xhtml`, `aluno.xhtml`, `professor.xhtml` e `admin.xhtml` (JSF/Facelets).
- **Controller:** `LoginBean`, `PainelBean`, `AdminBean`.
- **Service:** `PortalService` (login e consultas por perfil) e `AdminService` (autorização, validações, BCrypt e transações administrativas).
- **DAO:** `UsuarioDAO`, `AlocacaoDAO` e `AdminDAO` (SQL parametrizado com JdbcTemplate).
- **Model:** `Usuario`, `UsuarioAdministrado`, `Aula`, `Turma`, `Sala`, `Disciplina` e `Opcao`.

O `AdminService` valida o perfil `ADMIN` no backend em todas as operações. Os métodos de escrita executam transações (`@Transactional`) e bloqueiam uma linha do perfil `ADMIN`, serializando validações e inserções feitas pelo portal. Para alocações, o serviço verifica sobreposição de sala, professor e turma antes do `INSERT`. Outras aplicações que escrevam diretamente no banco precisam respeitar as mesmas regras.

### Estrutura do banco

- `perfis`: perfis ALUNO, PROFESSOR e ADMIN.
- `turmas`: código único, descrição e curso opcional.
- `usuarios`: alunos, professores e administradores; alunos mantêm o código textual da turma por compatibilidade.
- `salas`: bloco, número e tipo `TEORICA` ou `LABORATORIO`.
- `disciplinas`: catálogo de componentes curriculares.
- `alocacoes`: relaciona disciplina, professor, turma, sala, dia e horário.

As colunas `usuarios.turma` e `alocacoes.turma` continuam textuais, mas agora referenciam `turmas.codigo`. Isso preserva as consultas e os dados anteriores.

## Instalação

**Pré-requisitos:** JDK 17+, Maven 3.9+ e MySQL 8.

### 1. Preparar o banco de dados

Escolha **apenas uma** das opções:

- **Instalação nova:** execute `banco.sql` no MySQL Workbench. Ele cria o banco e todos os registros demonstrativos, incluindo o administrador. **Atenção: esse script recria as tabelas e apaga os dados anteriores.**
- **Banco que já possui o painel ADMIN:** faça backup e execute somente `atualizacao-gestao-academica.sql`.
- **Versão antiga sem ADMIN:** execute primeiro `atualizacao-admin.sql` e depois `atualizacao-gestao-academica.sql`.

O script de gestão é não destrutivo: cria `turmas`, migra os códigos já usados, mantém os IDs legados, adiciona chaves estrangeiras e cria restrições únicas somente quando os dados existentes são compatíveis. Ele pode ser reexecutado sem duplicar registros. Não reexecute `banco.sql` em um banco em uso.

No cliente de linha de comando do MySQL:

```powershell
mysql -u root -p -P 3307 -e "source C:/caminho/portal-academico/atualizacao-gestao-academica.sql"
```

### 2. Configurar a conexão

Defina a conexão no PowerShell antes de iniciar. Esses valores ficam somente no terminal e não são enviados ao Git:

```powershell
$env:DB_HOST = "127.0.0.1"
$env:DB_PORT = "3306"
$env:DB_NAME = "portal_academico"
$env:DB_USER = "root"
$env:DB_PASSWORD = Read-Host "Senha do MySQL"
$env:SERVER_PORT = "8080"
```

Adapte as portas ao seu ambiente. Não grave credenciais reais em `application.properties`.

### 3. Executar

```powershell
mvn spring-boot:run
```

Abra `http://localhost:8080/login.xhtml` (ou a porta definida em `SERVER_PORT`).

## Contas de demonstração

| Login | Senha | Perfil | Uso |
| --- | --- | --- | --- |
| `aluno1` | `senha123` | Aluno | Ana Souza (ADS1) |
| `aluno2` | `senha123` | Aluno | Bruno Lima (ADS2) |
| `prof1` | `senha123` | Professor | Carla Mendes |
| `prof2` | `senha123` | Professor | Daniel Costa |
| `prof3` | `senha123` | Professor | Marcos Oliveira (sem aulas inicialmente) |
| `admin1` | `senha123` | ADMIN | Coordenação Acadêmica |

As credenciais são exclusivamente demonstrativas; não utilize esse banco com dados reais ou em um servidor público.

## Testes sugeridos

1. Entre com `admin1` e cadastre a turma `ADS3`.
2. Cadastre um professor, um aluno vinculado à ADS3, a disciplina `Algoritmos` e o laboratório `L03` no bloco C.
3. Crie uma alocação de Algoritmos para ADS3 usando o novo professor e laboratório.
4. Saia e entre com o novo professor; confirme a aula em sua grade.
5. Entre com o novo aluno, escolha o dia da alocação e confirme a mesma aula.
6. Tente repetir login, turma, sala e disciplina; os cadastros devem ser recusados.
7. Tente cadastrar outra aula no mesmo período para a mesma sala, professor ou turma; deve aparecer uma mensagem de conflito.
8. Entre como aluno e professor e tente acessar `/admin.xhtml`; cada perfil deve voltar ao próprio painel.
9. Entre com `admin1`, exclua uma alocação e confirme que ela desaparece dos painéis relacionados.

Todos os seletores do formulário de alocação consultam o banco; não há listas fixas ou dados simulados no código.

**Observação:** os dados de aula são recorrentes por **dia da semana**, não por uma data de calendário. A tela administrativa permite cadastrar e excluir alocações; ainda não há edição nesta versão.

## Limites do projeto

É uma demonstração acadêmica, não um sistema de produção. Para uso real ainda seriam necessários edição/exclusão controlada dos cadastros mestres, trilha de auditoria, recuperação de senha, regras de matrícula e controles adicionais contra alterações diretas no banco.
