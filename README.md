# Portal Acadêmico — Spring Boot + JSF + JDBC (MVC)

Projeto mínimo para a atividade. **Não usa JPA, Hibernate, APIs REST nem Spring Security para login automático.** A autenticação usa SQL via `JdbcTemplate` e comparação do hash de senha com BCrypt.

## Requisitos para executar

- JDK 17 ou superior (Java 17 configurado no Maven)
- Maven 3.9 ou superior
- MySQL 8.0 funcionando

## Passo 1 — criar o banco

No **MySQL Workbench**, abra `banco.sql`, selecione tudo e execute. O script cria o banco `portal_academico`, cinco tabelas e quatro usuários de demonstração.

> **Atenção:** executar o script novamente apaga e recria essas cinco tabelas com os dados de demonstração.

## Passo 2 — configurar a conexão

Arquivo `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://127.0.0.1:${DB_PORT:3306}/portal_academico
spring.datasource.username=${DB_USER:root}
spring.datasource.password=${DB_PASSWORD:}
server.port=${SERVER_PORT:8080}
```

No PowerShell, antes de iniciar a aplicação (exemplo com seu usuário e senha do MySQL):

```powershell
$env:DB_USER="root"
$env:DB_PASSWORD="SUA_SENHA_MYSQL"
```

Se o MySQL estiver sem senha, não defina `DB_PASSWORD` ou deixe vazio. **Não envie a senha do MySQL ao GitHub.**

## Passo 3 — executar

Abra um terminal na pasta do projeto:

```powershell
mvn spring-boot:run
```

Abra no navegador: **http://localhost:8080/login.xhtml**

## Usuários de demonstração

| Login | Senha de teste | Perfil | Resultado |
| --- | --- | --- | --- |
| `aluno1` | `senha123` | Aluno (Ana) | Aulas de hoje da turma ADS1 |
| `aluno2` | `senha123` | Aluno (Bruno) | Aulas de hoje da turma ADS2 |
| `prof1` | `senha123` | Professor (Carla) | Alocações da semana |
| `prof2` | `senha123` | Professor (Daniel) | Alocações da semana |

Os dados de teste incluem aulas nos **sete dias** para que o painel do aluno possa ser demonstrado em qualquer dia. Em cenário real, ajustaríamos para os dias letivos. Os dois professores possuem alocações teóricas e práticas.

## Arquitetura MVC — como explicar

1. **View (`.xhtml`):** apresenta o formulário e os painéis. Usa `<h:form>`, `<h:inputText>`, `<h:inputSecret>`, `<h:commandButton>` e `<h:dataTable>`.
2. **Controller (`bean`):** `LoginBean` recebe login/senha; `PainelBean` verifica a sessão, escolhe a página e carrega os dados. Ambos usam `@Named` e `@ViewScoped`.
3. **Service:** `PortalService` valida o hash BCrypt e aplica regras de perfil (`ALUNO`/`PROFESSOR`).
4. **DAO:** `UsuarioDAO` executa `SELECT` de login com `JOIN perfis`; `AlocacaoDAO` executa SQL com `JOIN` de alocações, salas, disciplinas e usuários.
5. **Model:** `Usuario` e `Aula` são as entidades/objetos de dados que atravessam as camadas.
6. **Spring Boot:** cria/injeta automaticamente `JdbcTemplate`, `@Repository` e `@Service`, e inicia o servidor embutido. JoinFaces integra JSF ao Spring Boot.

Fluxo: **login.xhtml → LoginBean → PortalService → UsuarioDAO → MySQL → redirecionamento → PainelBean → PortalService → AlocacaoDAO → painel.xhtml**.

## Perguntas para defesa técnica

- **Por que usamos MVC?** Para separar a interface, o controle, a regra de negócio e as operações no banco; cada arquivo possui uma responsabilidade.
- **O que é DAO?** Padrão para isolar comandos SQL e conversão de linhas em objetos Java.
- **O que faz `@Service`?** Marca a classe de regras de negócio como gerenciada pelo Spring.
- **O que faz `@Repository`?** Marca a classe DAO; as falhas JDBC são traduzidas para `DataAccessException`.
- **O que é injeção de dependências?** O Spring fornece os DAOs ao Service, o JdbcTemplate aos DAOs e o Service aos Beans, sem `new` manual desses componentes.
- **Por que `JdbcTemplate`?** Executa SQL diretamente, usa parâmetros `?`, evita concatenação perigosa e fecha recursos JDBC automaticamente.
- **Por que `JOIN`?** As informações estão em tabelas relacionadas; JOIN reúne disciplina, sala, alocação e professor em uma consulta.
- **Por que `@ViewScoped`?** Mantém os dados do Bean durante a interação com a mesma página JSF sem torná-los globais para todos os usuários.
- **Onde o login é validado?** O DAO consulta o login no MySQL e o Service compara a senha fornecida com o hash BCrypt recuperado do banco.
- **Como se impede acesso ao painel errado?** A ação `f:viewAction` chama `PainelBean` ao abrir a página; ele verifica o usuário autenticado na sessão e seu perfil. O Service também verifica o perfil antes de consultar aulas.
- **Como são tratados erros SQL?** `JdbcTemplate` transforma a exceção JDBC em `DataAccessException`, tratada nos Beans para exibir mensagem sem mostrar detalhes internos do banco.
- **Por que a senha não está em texto puro no banco?** O banco armazena hash BCrypt com salt; a comparação é feita sem recuperar a senha original.
- **Qual é a diferença entre a visão de aluno e professor?** O aluno consulta a turma e o dia atual; o professor consulta seu ID em toda a semana, distinguindo salas teóricas e laboratórios.

## Sequência sugerida da demonstração

1. Tente entrar com senha incorreta e mostre a mensagem.
2. Entre como `aluno1` e mostre disciplina, horário, bloco e sala.
3. Saia, entre como `aluno2` e compare o conteúdo.
4. Entre como `prof1` e mostre uma aula teórica e outra prática (laboratório).
5. Saia e entre como `prof2` para provar que as alocações são separadas por professor.
6. Tente abrir `/professor.xhtml` enquanto estiver autenticado como aluno; o sistema redirecionará para o painel de aluno.
7. Mostre `banco.sql`, os `SELECT` com `JOIN` nos DAOs e o `@Service`.

## Publicar o código no GitHub

Crie um repositório vazio no GitHub e, no terminal desta pasta, execute:

```bash
git init
git add .
git commit -m "Portal academico MVC com Spring Boot, JSF e JDBC"
git branch -M main
git remote add origin URL_DO_SEU_REPOSITORIO
git push -u origin main
```

Substitua `URL_DO_SEU_REPOSITORIO` pelo endereço real do repositório criado.

## Observação

Este projeto é acadêmico e simplificado. Um sistema real precisaria de gestão de matrículas, políticas de senha e controles adicionais de segurança. As senhas `senha123` existem apenas para a demonstração. Os arquivos foram preparados para execução local; o banco e o servidor precisam ser testados no computador da equipe.
