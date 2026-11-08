# Portal Acadêmico

Aplicação web desenvolvida em Java para gerenciamento e consulta de informações acadêmicas. O sistema permite a autenticação de usuários e disponibiliza painéis específicos para alunos e professores, apresentando disciplinas, horários e locais das aulas.

## Funcionalidades

- **Autenticação:** validação de credenciais armazenadas no MySQL, com verificação de senhas utilizando BCrypt.
- **Painel do aluno:** consulta das disciplinas do dia, horários, professores e localização das aulas.
- **Painel do professor:** visualização das alocações semanais, diferenciando salas de aulas teóricas e laboratórios para aulas práticas.
- **Controle de acesso:** direcionamento automático conforme o perfil autenticado e verificação da sessão do usuário.

## Tecnologias utilizadas

| Tecnologia | Finalidade |
| --- | --- |
| Java 17 | Linguagem principal |
| Spring Boot 3.5 | Configuração, inicialização e injeção de dependências |
| JSF (Jakarta Faces) | Desenvolvimento das interfaces web |
| JoinFaces | Integração entre Spring Boot e JSF |
| Spring JDBC | Persistência utilizando JdbcTemplate |
| MySQL 8.0 | Banco de dados relacional |
| BCrypt | Verificação de senhas |
| Maven | Gerenciamento de dependências |

## Arquitetura

O sistema foi desenvolvido seguindo o padrão arquitetural **MVC (Model-View-Controller)**, complementado por camadas de serviço e persistência.

A organização do código separa a interface, o controle de navegação, as regras de negócio e as operações de banco de dados.

| Camada | Componentes | Responsabilidade |
| --- | --- | --- |
| Model | `Usuario`, `Aula` | Representação dos dados |
| View | `login.xhtml`, `aluno.xhtml`, `professor.xhtml` | Interface com o usuário |
| Controller | `LoginBean`, `PainelBean` | Controle de interação, sessão e navegação |
| Service | `PortalService` | Autenticação e regras de negócio |
| DAO | `UsuarioDAO`, `AlocacaoDAO` | Consultas SQL e acesso aos dados |

### Fluxo de autenticação

`login.xhtml → LoginBean → PortalService → UsuarioDAO → MySQL`

Após a autenticação, o sistema identifica o perfil do usuário e realiza o direcionamento para `aluno.xhtml` ou `professor.xhtml`.

Os dados dos painéis são recuperados por consultas SQL realizadas através do `JdbcTemplate`, utilizando relacionamentos entre tabelas com `JOIN`.

A persistência é implementada diretamente com JDBC, sem utilização de ORM ou JPA.

## Banco de dados

O projeto utiliza MySQL e possui cinco tabelas principais:

| Tabela | Descrição |
| --- | --- |
| `perfis` | Identificação dos perfis de acesso |
| `usuarios` | Dados dos usuários e credenciais |
| `disciplinas` | Informações das disciplinas |
| `salas` | Identificação, localização e tipo de sala |
| `alocacoes` | Associação de disciplinas, professores, turmas, salas e horários |

O arquivo `banco.sql`, localizado na raiz do projeto, contém os comandos para criação da estrutura e inserção dos dados demonstrativos.

**Atenção:** a execução do script recria as tabelas e restaura os dados iniciais.

## Instalação e execução

### Pré-requisitos

- JDK 17 ou superior
- Maven 3.9 ou superior
- MySQL 8.0

### 1. Clonar o repositório

```powershell
git clone https://github.com/GabrielMarquesxz/portal-academico.git
cd portal-academico
```

### 2. Configurar o banco de dados

Abra o arquivo `banco.sql` no MySQL Workbench e execute o script para criar o banco `portal_academico` e seus dados iniciais.

### 3. Configurar a conexão

O arquivo `src/main/resources/application.properties` utiliza variáveis de ambiente para definir a conexão:

```properties
spring.datasource.url=jdbc:mysql://127.0.0.1:${DB_PORT:3306}/portal_academico?useUnicode=true&characterEncoding=UTF-8
spring.datasource.username=${DB_USER:root}
spring.datasource.password=${DB_PASSWORD:}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

joinfaces.faces-servlet.url-mappings=*.xhtml
server.port=${SERVER_PORT:8080}
```

Exemplo de configuração no PowerShell:

```powershell
$env:DB_USER="root"
$env:DB_PORT="3306"
$env:SERVER_PORT="8080"
$env:DB_PASSWORD = Read-Host "Senha do MySQL"
```

As variáveis devem ser ajustadas de acordo com o ambiente de execução.

### 4. Executar a aplicação

No terminal, a partir da raiz do projeto:

```powershell
mvn spring-boot:run
```

Com a configuração padrão, a aplicação estará disponível em:

**http://localhost:8080/login.xhtml**

## Usuários de demonstração

O banco de dados inclui quatro contas para testes funcionais.

| Usuário | Senha | Perfil |
| --- | --- | --- |
| `aluno1` | `senha123` | Aluno — Ana Souza |
| `aluno2` | `senha123` | Aluno — Bruno Lima |
| `prof1` | `senha123` | Professor — Carla Mendes |
| `prof2` | `senha123` | Professor — Daniel Costa |

As senhas são exclusivas do ambiente demonstrativo e são armazenadas no banco utilizando hashes BCrypt.

## Escopo do projeto

O Portal Acadêmico implementa as funcionalidades essenciais de autenticação, consulta de aulas e direcionamento por perfil.

A aplicação apresenta uma arquitetura modular, com separação das responsabilidades entre interface, regras de negócio e persistência.

O sistema utiliza uma implementação simplificada de autenticação e não contempla funcionalidades administrativas, gerenciamento de matrículas ou edição de horários pela interface.

Para utilização em produção, seriam necessários controles adicionais de segurança, gerenciamento de usuários e procedimentos de manutenção dos dados.