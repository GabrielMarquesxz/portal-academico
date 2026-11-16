# Publicacao do Portal Academico no Render

Este conjunto acrescenta suporte ao deploy, preservando o projeto Spring Boot + JSF + JDBC e a execucao local no Windows. **Nao e um deploy realizado**. Os arquivos foram preparados para ser copiados para a raiz do projeto, antes de commit/push.

## 1. Conferencia local (PowerShell)

Na pasta `portal-academico`, rode `git status` antes de copiar os arquivos. Copie o `Dockerfile`, o `.dockerignore`, a pasta `scripts` e este documento, substituindo apenas `src/main/resources/application.properties` com o arquivo fornecido neste pacote.

Execute `mvn -version` para verificar que o Maven usa Java 17+ (idealmente 21). Opcionalmente valide com `mvn clean package` e, se houver Docker Desktop, `docker build -t portal-academico-render .`.

O Dockerfile compila com Maven e executa com Java 17, sem depender das instalacoes locais de Java/Maven no Render.

## 2. Escolha o banco remoto

**Opcao de menor custo: TiDB Cloud Starter compativel com MySQL (faixa gratuita).**

- Site: https://tidbcloud.com/
- Documentacao: https://docs.pingcap.com/tidbcloud/tidb-cloud-quickstart/
- Crie instancia Starter em modo **MySQL-compatible**; mantenha limite de gastos em zero.
- Obtenha host, porta, usuario e senha no menu Connect.
- Crie um banco novo `portal_academico` e importe o `banco.sql` **somente nesse banco remoto novo**. O script faz DROP TABLE e apaga registros existentes; **nao o execute novamente em banco em uso**.
- TiDB nao e identico ao MySQL. FKs estao disponiveis, mas CHECK pode depender de configuracao do provedor. Execute e confirme todas as tabelas e o fluxo administrativo antes de considerar o banco aprovado. Se houver erro SQL, registre a mensagem: nao remova restricoes de integridade sem avaliacao.
- TLS e obrigatorio para conexoes publicas; use o endereco oficial e sslMode=VERIFY_IDENTITY quando compativel com o certificado do provedor.

**Alternativa MySQL real:** Aiven for MySQL Developer (pago, a partir de US$5/mes). O plano gratis de MySQL da Aiven deixou de aceitar novas criacoes em 06/11/2026. O Render nao fornece MySQL gerenciado nativo; e necessario um provedor externo.

## 3. Proteja as credenciais ANTES de tornar o site publico

O `banco.sql` cria logins demonstrativos com senha `senha123`, inclusive o administrador `admin1`. Nunca publique o sistema com essa senha administrativa.

No Windows, para gerar localmente um novo hash BCrypt sem enviar sua senha para a internet:

```powershell
py -m pip install bcrypt
py scripts/gerar_hash_bcrypt.py
```

Copie o hash (nao a senha) e execute no **banco remoto**, usando o editor SQL ou Workbench:

```sql
USE portal_academico;
UPDATE usuarios
SET senha_hash = '<HASH_BCRYPT_GERADO_LOCALMENTE>'
WHERE login = 'admin1';
```

Recomenda-se tambem substituir as senhas das outras contas demonstrativas antes de publicar, pois o README contem suas credenciais. Use somente dados ficticios. Nao coloque segredos no Git ou no README.

## 4. Render: Web Service Docker

1. Acesse https://dashboard.render.com/ e entre com GitHub.
2. Escolha **New > Web Service**, conecte `GabrielMarquesxz/portal-academico` (apos incluir os arquivos de deploy no GitHub).
3. Selecione `main` ou branch de teste; escolha **Docker** como runtime.
4. Root Directory: raiz do repositorio, onde esta o `Dockerfile`.
5. Instance Type: **Free** para apresentacao (512 MB de RAM). Se ocorrer falha por falta de memoria, aumente o plano ou reduza o consumo da aplicacao.
6. Em **Environment**, adicione as variaveis abaixo. **Nunca inclua usuario/senha reais em arquivos versionados.**

| Chave | Valor |
|---|---|
| `DB_JDBC_URL` | `jdbc:mysql://HOST_DO_PROVEDOR:PORTA/portal_academico?sslMode=VERIFY_IDENTITY&useUnicode=true&characterEncoding=UTF-8` (substituir HOST e PORTA) |
| `DB_USER` | Usuario fornecido pelo banco remoto |
| `DB_PASSWORD` | Senha do banco remoto (campo secreto) |
| `DB_POOL_MAX_SIZE` | `4` |

`PORT` e definido pelo Render (padrao 10000) e a aplicacao usa automaticamente. Evite definir `DB_HOST=127.0.0.1` no Render. O endereco exato e os parametros TLS podem variar por provedor; siga a orientacao oficial da conexao JDBC.

7. Crie o Web Service; verifique o build Maven e os logs da inicializacao.
8. Abra `https://<nome-do-seu-servico>.onrender.com/login.xhtml` (endereco so existira apos deploy).
9. Teste logins, cadastros, consultas e exclusoes com dados ficticios.

## 5. Verificacao

- Login JSF abre pela URL publica.
- Aluno consulta sua turma; professor consulta aulas; ADMIN cria/exclui alocacoes.
- O cadastro continua presente apos o Render reiniciar, porque os dados estao no banco remoto, nao no disco efemero.
- A resposta inicial no plano Free pode atrasar depois de 15 minutos sem trafego.
- Nao use as credenciais de demo originais e nao deixe acesso administrativo publico com senha conhecida.

## 6. Publicacao no GitHub (apos validacao)

```powershell
git status
git add Dockerfile .dockerignore src/main/resources/application.properties scripts/gerar_hash_bcrypt.py DEPLOY_RENDER.md
git commit -m "Prepara deploy Docker no Render"
git push origin main
```

Nao comite arquivos `.env`, senhas ou hashes de producao no repositorio.

## Observacoes

- Render Free e indicado para demonstracao, nao producao, e pode suspender o servico apos inatividade.
- TiDB MySQL-compativel utiliza driver MySQL mas pode exigir pequenos ajustes de SQL. O desempenho das transacoes administrativas e a integridade das constraints precisam de teste integrado real.
- O build Docker e o deploy ainda precisam ser executados e verificados por voce. Este pacote nao altera automaticamente seu repositorio remoto nem o banco.
