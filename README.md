# Auto Escola 3ESPG – API REST

API REST em **Java 25 + Spring Boot 4** para gerenciamento de uma auto-escola: cadastro de instrutores, alunos e usuários, autenticação JWT e agendamento/cancelamento de instruções.

Disciplina: **SOA e WebServices** – Prof. Carlos Eduardo Machado de Oliveira

## Integrantes

| Nome | RM |
|------|----|
| _Nome completo do integrante 1_ | _RM00000_ |
| _Nome completo do integrante 2_ | _RM00000_ |
| _Nome completo do integrante 3_ | _RM00000_ |
| _Nome completo do integrante 4_ | _RM00000_ |
| _Nome completo do integrante 5_ | _RM00000_ |

## Tecnologias

- Java 25, Spring Boot 4 (Web MVC, Data JPA, Validation, Security)
- MySQL + Flyway (migrations versionadas em `src/main/resources/db/migration`)
- H2 (banco em memória) nos testes automatizados e no perfil opcional `h2`
- JWT (`com.auth0:java-jwt`) e senhas criptografadas com BCrypt
- springdoc-openapi (Swagger UI)
- RestClient (consumo do WebService externo **ViaCEP**)
- JUnit 5, Mockito, MockMvc, `@DataJpaTest`, `MockRestServiceServer`

## Como executar

1. Tenha um MySQL rodando em `localhost:3306` com usuário `root` / senha `fiap` (ajuste em `application.properties` se necessário). O banco `autoescola3espg` é criado automaticamente.
2. Execute: `./mvnw spring-boot:run` (Windows: `mvnw.cmd spring-boot:run`)
3. A API sobe em `http://localhost:8085`.

**Sem MySQL instalado?** Rode com o perfil `h2` (banco em memória, as mesmas migrations são aplicadas; os dados somem ao encerrar):

```
./mvnw spring-boot:run -Dspring-boot.run.profiles=h2
```

No IntelliJ: *Run > Edit Configurations > Active profiles* = `h2`.

Usuário administrador criado automaticamente pela migration `V10`:

| login | senha | perfil |
|-------|-------|--------|
| `admin` | `admin` | ADMIN |

> Recomenda-se trocar essa senha após o primeiro acesso (`PUT /usuarios/senha`).

## Documentação (Swagger)

- Swagger UI: `http://localhost:8085/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8085/v3/api-docs` · YAML: `http://localhost:8085/v3/api-docs.yaml`

Para testar pelo Swagger: faça `POST /login`, copie o `tokenJWT` e clique em **Authorize**.

## Endpoints

| Método | Rota | Perfil | Descrição |
|--------|------|--------|-----------|
| POST | `/login` | público | Autenticação; devolve o token JWT |
| GET | `/health-check` | público | Verificação de integridade |
| POST | `/instrutores` | ADMIN | Cadastrar instrutor |
| GET | `/instrutores` | ADMIN, USER | Listar instrutores ativos (nome, e-mail, CNH, especialidade) – 10 por página, ordenado por nome |
| GET | `/instrutores/{id}` | ADMIN | Detalhar instrutor |
| PUT | `/instrutores` | ADMIN | Atualizar nome, telefone e endereço |
| DELETE | `/instrutores/{id}` | ADMIN | Exclusão lógica (inativa o instrutor) |
| POST | `/alunos` | ADMIN | Cadastrar aluno |
| GET | `/alunos` | ADMIN, USER | Listar alunos ativos (nome, e-mail, CPF) – 10 por página, ordenado por nome |
| GET | `/alunos/{id}` | ADMIN | Detalhar aluno |
| PUT | `/alunos` | ADMIN | Atualizar nome, telefone e endereço |
| DELETE | `/alunos/{id}` | ADMIN | Exclusão lógica (inativa o aluno) |
| POST | `/usuarios` | ADMIN | Cadastrar usuário (senha criptografada com BCrypt) |
| GET | `/usuarios` | ADMIN | Listar usuários (a senha nunca é devolvida) |
| GET | `/usuarios/{id}` | ADMIN | Detalhar usuário |
| PUT | `/usuarios/{id}/perfil` | ADMIN | Atualizar o perfil (USER/ADMIN) de um usuário |
| DELETE | `/usuarios/{id}` | ADMIN | Excluir usuário |
| PUT | `/usuarios/senha` | autenticado | Alterar a própria senha |
| POST | `/instrucoes` | autenticado | Agendar instrução |
| GET | `/instrucoes` | autenticado | Listar instruções |
| GET | `/instrucoes/{id}` | autenticado | Detalhar instrução |
| DELETE | `/instrucoes` | autenticado | Cancelar instrução (com motivo) |
| GET | `/enderecos/{cep}` | autenticado | Buscar endereço pelo CEP (WebService externo ViaCEP) |

### Exemplos de payload

Agendar instrução (`id_instrutor` é opcional; se omitido, informe `especialidade` e o sistema escolhe um instrutor livre aleatoriamente):

```json
{ "id_aluno": 1, "id_instrutor": 1, "data_hora": "12/10/2026 - 10:00" }
```

Cancelar instrução (`motivo`: `ALUNO_DESISTIU`, `INSTRUTOR_CANCELOU` ou `OUTROS`):

```json
{ "id_instrucao": 1, "motivo": "ALUNO_DESISTIU" }
```

Alterar a própria senha:

```json
{ "senhaAtual": "admin", "novaSenha": "novaSenha123" }
```

## Regras de negócio implementadas

**Instrutores / Alunos**
- Campos obrigatórios validados (exceto número e complemento do endereço); CPF com 11 dígitos, CNH com 9 a 11 dígitos, UF e CEP com formato validado.
- E-mail, CPF e CNH não podem se repetir.
- Atualização permite apenas nome, telefone e endereço; tentativas de alterar **e-mail, CNH ou especialidade** (instrutor) e **e-mail ou CPF** (aluno) são rejeitadas com erro 400.
- Exclusão é lógica: o registro fica como inativo e deixa de aparecer na listagem.

**Agendamento de instruções**
- Funcionamento de segunda a sábado, das 06:00 às 21:00; duração fixa de 1 hora (último início às 20:00), em horas inteiras.
- Antecedência mínima de 30 minutos.
- Não permite aluno ou instrutor inativo.
- No máximo duas instruções por dia para o mesmo aluno (e não no mesmo horário).
- Não permite instrutor com outra instrução na mesma data/hora.
- Instrutor opcional: escolha aleatória de um instrutor ativo e disponível da especialidade informada.
- Instruções canceladas não ocupam horário nem contam para o limite diário.

**Cancelamento de instruções**
- Motivo obrigatório: aluno desistiu, instrutor cancelou ou outros.
- Antecedência mínima de 24 horas; uma instrução já cancelada não pode ser cancelada de novo.

**Usuários**
- Senhas armazenadas apenas criptografadas (BCrypt).
- Somente ADMIN cadastra, lista, atualiza perfil e exclui usuários; um ADMIN não pode alterar o próprio perfil nem excluir a si mesmo.
- Qualquer usuário autenticado pode alterar a própria senha, informando a senha atual.

## Checkpoint 5

- **Consumo de WebService externo:** `ViaCepService` usa o `RestClient` para consultar `https://viacep.com.br/ws/{cep}/json/` (endpoint `GET /enderecos/{cep}`). O retorno já está no formato do campo `endereco` dos cadastros.
- **Swagger:** springdoc-openapi com esquema de segurança Bearer JWT, tags e descrição das operações.
- **CORS:** configurado em `SecurityConfig` (origens permitidas em `api.cors.allowed-origins` no `application.properties`).
- **Testes automatizados:** veja abaixo.

## Testes

Execute `./mvnw test` (Windows: `mvnw.cmd test`). **Não é necessário ter MySQL**: os testes que usam banco (`@SpringBootTest` e `@DataJpaTest`) rodam no perfil `test`, que usa um banco **H2 em memória** em modo de compatibilidade MySQL, com as mesmas migrations do Flyway.

| Entidade / recurso | Testes |
|--------------------|--------|
| Instrutor | `InstrutorControllerTest`, `InstrutorServiceTest`, `InstrutorRepositoryTest` |
| Aluno | `AlunoControllerTest`, `AlunoServiceTest`, `AlunoRepositoryTest` |
| Usuário | `UsuarioControllerTest`, `UsuarioServiceTest`, `UsuarioRepositoryTest` |
| Instrução | `InstrucaoControllerTest`, `InstrucaoServiceTest`, `ValidadoresInstrucaoTest` |
| API externa (ViaCEP) | `ViaCepServiceTest` (servidor simulado com `MockRestServiceServer`, não depende de internet) |
| CORS | `CorsConfigTest` |
