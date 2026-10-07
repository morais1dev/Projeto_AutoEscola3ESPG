# Auto Escola 3ESPG – API REST

API REST em **Java 25 + Spring Boot 4** para gerenciamento de uma auto-escola: cadastro de instrutores, alunos e usuários, autenticação JWT e agendamento/cancelamento de instruções.

Disciplina: **SOA e WebServices** – Prof. Carlos Eduardo Machado de Oliveira

## Integrantes

| Nome                            | RM         |
|---------------------------------|------------|
| _Gustavo Morais Ildefonso_      | _RM554972_ |
| _Murilo Justi Rodrigues_        | _RM554512_ |
| _Vitor Alves Titus Eskes_       | _RM555137_ |
| _Leonardo Rocha Scarpitta_      | _RM555460_ |

## Sumário

1. [Tecnologias](#tecnologias)
2. [Como executar](#como-executar)
3. [Como testar pelo Swagger](#como-testar-pelo-swagger)
4. [Endpoints](#endpoints)
5. [Regras de negócio](#regras-de-negócio)
6. [Requisitos do Checkpoint 5](#requisitos-do-checkpoint-5)
7. [Testes automatizados](#testes-automatizados)
8. [Estrutura do projeto](#estrutura-do-projeto)

---

## Tecnologias

| Camada | Tecnologia |
|--------|-----------|
| Linguagem / framework | Java 25, Spring Boot 4 (Web MVC, Data JPA, Validation, Security) |
| Banco de dados | MySQL (padrão) ou H2 em memória (perfil `h2` e testes) |
| Migrações | Flyway (`src/main/resources/db/migration`, V1 a V10) |
| Segurança | Spring Security, JWT (`com.auth0:java-jwt`), senhas com BCrypt |
| Documentação | springdoc-openapi (Swagger UI) |
| API externa | ViaCEP, consumida com `RestClient` |
| Testes | JUnit, Mockito, MockMvc, `@DataJpaTest`, `MockRestServiceServer` |

---

## Como executar

Pré-requisito: **JDK 25**. O Maven não precisa estar instalado, pois o projeto já traz o Maven Wrapper (`mvnw`).

### Opção 1 – Sem MySQL (banco H2 em memória)

A forma mais simples. As mesmas migrations do Flyway são aplicadas no H2, e os dados são apagados quando a aplicação é encerrada.

**Pelo IntelliJ**
1. Abra **Run > Edit Configurations...** e selecione `AutoEscola3EspgApplication`.
2. Em **Active profiles**, informe `h2`.
3. Execute e aguarde a mensagem `Started AutoEscola3EspgApplication` no console.

**Pelo terminal**
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=h2       # Linux / macOS
mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=h2     # Windows
```

### Opção 2 – Com MySQL

1. Deixe um MySQL rodando em `localhost:3306` com usuário `root` e senha `fiap`. Se forem outros, ajuste em `src/main/resources/application.properties`. O banco `autoescola3espg` é criado automaticamente.
2. Execute sem perfil: `./mvnw spring-boot:run` (Windows: `mvnw.cmd spring-boot:run`).

A API sobe em **http://localhost:8085**.

### Usuário administrador inicial

Criado automaticamente pela migration `V10`:

| login | senha | perfil |
|-------|-------|--------|
| `admin` | `admin` | ADMIN |

---

## Como testar pelo Swagger

1. Com a API rodando, acesse **http://localhost:8085/swagger-ui.html**.
2. Em **POST /login**, clique em *Try it out* e envie:
   ```json
   { "login": "admin", "senha": "admin" }
   ```
3. Copie o valor de `tokenJWT` da resposta, clique em **Authorize** (cadeado no topo) e cole **somente o token**, sem a palavra "Bearer". O token vale 30 minutos.
4. Siga o fluxo abaixo, na ordem.

**Buscar endereço pelo CEP (API externa ViaCEP)** – `GET /enderecos/01001000`

**Cadastrar instrutor** – `POST /instrutores`
```json
{
  "nome": "Carlos Instrutor",
  "email": "carlos@email.com",
  "telefone": "(11) 91234-5678",
  "cnh": "01234567890",
  "especialidade": "CARROS",
  "endereco": {
    "logradouro": "Praça da Sé", "numero": "10", "complemento": "",
    "bairro": "Sé", "cidade": "São Paulo", "uf": "SP", "cep": "01001-000"
  }
}
```
Especialidades aceitas: `MOTOS`, `CARROS`, `VANS`, `CAMINHOES`.

**Cadastrar aluno** – `POST /alunos`
```json
{
  "nome": "Ana Aluna",
  "email": "ana@email.com",
  "telefone": "(11) 98765-4321",
  "cpf": "12345678901",
  "endereco": {
    "logradouro": "Praça da Sé", "numero": "20", "complemento": "",
    "bairro": "Sé", "cidade": "São Paulo", "uf": "SP", "cep": "01001-000"
  }
}
```

**Agendar instrução** – `POST /instrucoes`

A data usa o formato `dd/MM/yyyy - HH:mm` e precisa ser futura, de segunda a sábado, entre 06:00 e 20:00, em hora cheia.
```json
{ "id_aluno": 1, "id_instrutor": 1, "data_hora": "14/10/2026 - 10:00" }
```
Para o sistema escolher um instrutor disponível automaticamente, troque `id_instrutor` pela especialidade:
```json
{ "id_aluno": 1, "especialidade": "CARROS", "data_hora": "14/10/2026 - 11:00" }
```

**Cancelar instrução** – `DELETE /instrucoes`

Motivos aceitos: `ALUNO_DESISTIU`, `INSTRUTOR_CANCELOU`, `OUTROS`.
```json
{ "id_instrucao": 1, "motivo": "ALUNO_DESISTIU" }
```

**Cadastrar usuário (somente ADMIN)** – `POST /usuarios`
```json
{ "login": "joao", "senha": "senha123", "perfil": "USER" }
```

**Alterar a própria senha (qualquer usuário logado)** – `PUT /usuarios/senha`
```json
{ "senhaAtual": "senha123", "novaSenha": "novaSenha456" }
```

### Validando as regras de negócio

| Cenário | Como provocar | Resultado esperado |
|---------|---------------|--------------------|
| Domingo | agendar em um domingo | 400 – fora do horário de funcionamento |
| Fora do horário | agendar às 21:00 ou antes das 06:00 | 400 |
| Antecedência | agendar para daqui a menos de 30 minutos | 400 |
| Limite diário | agendar o mesmo aluno 3 vezes no mesmo dia | a 3ª retorna 400 |
| Instrutor ocupado | outro aluno, mesmo instrutor e horário | 400 |
| Cancelamento tardio | cancelar instrução que acontece em menos de 24h | 400 |
| Motivo inválido | cancelar com `"motivo": "QUALQUER"` | 400 |
| Campo imutável | `PUT /alunos` com `{"id": 1, "email": "novo@email.com"}` | 400 |
| Exclusão lógica | `DELETE /alunos/1` e depois `GET /alunos` | 204 e o aluno some da listagem |
| Permissão | logado como USER, tentar `POST /instrutores` | 403 |
| Sem token | chamar qualquer rota protegida sem Authorize | 401 |

---

## Endpoints

| Método | Rota | Perfil | Descrição |
|--------|------|--------|-----------|
| POST | `/login` | público | Autenticação; devolve o token JWT |
| GET | `/health-check` | público | Verificação de integridade |
| POST | `/instrutores` | ADMIN | Cadastrar instrutor |
| GET | `/instrutores` | ADMIN, USER | Listar instrutores ativos (nome, e-mail, CNH, especialidade), 10 por página, ordenados por nome |
| GET | `/instrutores/{id}` | ADMIN | Detalhar instrutor |
| PUT | `/instrutores` | ADMIN | Atualizar nome, telefone e endereço |
| DELETE | `/instrutores/{id}` | ADMIN | Excluir (inativar) instrutor |
| POST | `/alunos` | ADMIN | Cadastrar aluno |
| GET | `/alunos` | ADMIN, USER | Listar alunos ativos (nome, e-mail, CPF), 10 por página, ordenados por nome |
| GET | `/alunos/{id}` | ADMIN | Detalhar aluno |
| PUT | `/alunos` | ADMIN | Atualizar nome, telefone e endereço |
| DELETE | `/alunos/{id}` | ADMIN | Excluir (inativar) aluno |
| POST | `/usuarios` | ADMIN | Cadastrar usuário |
| GET | `/usuarios` | ADMIN | Listar usuários (sem a senha) |
| GET | `/usuarios/{id}` | ADMIN | Detalhar usuário |
| PUT | `/usuarios/{id}/perfil` | ADMIN | Atualizar o perfil (USER/ADMIN) de um usuário |
| DELETE | `/usuarios/{id}` | ADMIN | Excluir usuário |
| PUT | `/usuarios/senha` | autenticado | Alterar a própria senha |
| POST | `/instrucoes` | autenticado | Agendar instrução |
| GET | `/instrucoes` | autenticado | Listar instruções |
| GET | `/instrucoes/{id}` | autenticado | Detalhar instrução |
| DELETE | `/instrucoes` | autenticado | Cancelar instrução |
| GET | `/enderecos/{cep}` | autenticado | Buscar endereço pelo CEP (ViaCEP) |

Documentação OpenAPI: `http://localhost:8085/v3/api-docs` (JSON) e `http://localhost:8085/v3/api-docs.yaml` (YAML).

### Códigos de resposta

| Código | Quando |
|--------|--------|
| 200 / 201 / 204 | Sucesso (201 nos cadastros, com header `Location`; 204 nas exclusões e na troca de senha) |
| 400 | Dados inválidos ou regra de negócio violada (a resposta traz a mensagem do erro) |
| 401 | Token ausente, inválido ou expirado; login ou senha incorretos |
| 403 | Usuário sem permissão para a operação |
| 404 | Registro não encontrado |
| 409 | Registro duplicado |
| 503 | ViaCEP indisponível |

---

## Regras de negócio

### Instrutores e alunos
- Todos os campos são obrigatórios, exceto número e complemento do endereço.
- CPF com 11 dígitos, CNH com 9 a 11 dígitos, UF com 2 letras maiúsculas e CEP no formato `00000-000`.
- E-mail, CPF e CNH não podem se repetir.
- Na atualização, só nome, telefone e endereço podem mudar. Tentar alterar **e-mail, CNH ou especialidade** (instrutor) ou **e-mail ou CPF** (aluno) retorna erro 400.
- A exclusão é lógica: o registro fica inativo e deixa de aparecer na listagem.

### Agendamento de instruções
- Funcionamento de segunda a sábado, das 06:00 às 21:00.
- Duração fixa de 1 hora, sempre em hora cheia (último início às 20:00).
- Antecedência mínima de 30 minutos.
- Não é possível agendar com aluno ou instrutor inativo.
- No máximo duas instruções por dia para o mesmo aluno, e nunca duas no mesmo horário.
- O instrutor não pode ter outra instrução na mesma data e hora.
- O instrutor é opcional: se não for informado, o sistema escolhe aleatoriamente um instrutor ativo e livre da especialidade pedida.
- Instruções canceladas liberam o horário e não contam para o limite diário.

### Cancelamento de instruções
- Motivo obrigatório: aluno desistiu, instrutor cancelou ou outros.
- Antecedência mínima de 24 horas.
- Uma instrução já cancelada não pode ser cancelada de novo.

### Usuários
- As senhas são armazenadas apenas criptografadas (BCrypt) e nunca são devolvidas pela API.
- Somente ADMIN cadastra, lista, altera o perfil e exclui usuários.
- Um ADMIN não pode alterar o próprio perfil nem excluir a si mesmo.
- Qualquer usuário autenticado pode alterar a própria senha, informando a senha atual.

---

## Requisitos do Checkpoint 5

| Requisito | Implementação |
|-----------|---------------|
| Consumo de API/WebService externo | `ViaCepService` usa `RestClient` para consultar `https://viacep.com.br/ws/{cep}/json/`, exposto em `GET /enderecos/{cep}`. O retorno já vem no formato do campo `endereco` dos cadastros. |
| Documentação automática com Swagger | springdoc-openapi com esquema de segurança Bearer JWT, tags por recurso e descrição das operações (`SwaggerConfig`). |
| Configuração de CORS | `SecurityConfig`. As origens permitidas ficam em `api.cors.allowed-origins`, no `application.properties`. |
| Testes automatizados por entidade | Veja a seção abaixo. |

---

## Testes automatizados

```bash
./mvnw test      # Linux / macOS
mvnw.cmd test    # Windows
```

No IntelliJ: botão direito em `src/test/java` > **Run 'All Tests'**.

**Não é necessário ter MySQL.** Os testes que usam banco rodam no perfil `test`, com um H2 em memória em modo de compatibilidade MySQL e as mesmas migrations do Flyway. O teste do ViaCEP simula o servidor externo, então também não depende de internet.

| Entidade / recurso | Classes de teste | O que cobrem |
|--------------------|------------------|--------------|
| Instrutor | `InstrutorControllerTest`, `InstrutorServiceTest`, `InstrutorRepositoryTest` | validação (400), cadastro (201), permissão (403), sem token (401), campos imutáveis, exclusão lógica, escolha aleatória de instrutor disponível |
| Aluno | `AlunoControllerTest`, `AlunoServiceTest`, `AlunoRepositoryTest` | CPF inválido, cadastro, permissões, campos imutáveis, exclusão lógica, listagem só de ativos |
| Usuário | `UsuarioControllerTest`, `UsuarioServiceTest`, `UsuarioRepositoryTest` | senha criptografada, login duplicado, acesso só para ADMIN, troca da própria senha |
| Instrução | `InstrucaoControllerTest`, `InstrucaoServiceTest`, `ValidadoresInstrucaoTest` | agendamento, horário de funcionamento, antecedência, limite diário, conflito de horário, cancelamento (24h, motivo, já cancelada) |
| API externa | `ViaCepServiceTest` | resposta do ViaCEP, CEP inexistente, CEP inválido, serviço fora do ar |
| CORS | `CorsConfigTest` | origem permitida e origem bloqueada |

---

## Estrutura do projeto

```
src/main/java/br/com/fiap3espg/autoescola3espg
├── controller/          # Endpoints REST (Aluno, Instrutor, Instrucao, Usuario, Endereco, Login)
├── service/             # Regras de negócio e consumo do ViaCEP
├── domain/
│   ├── aluno/           # Entidade, repository e DTOs de aluno
│   ├── instrutor/       # Entidade, repository e DTOs de instrutor
│   ├── instrucao/       # Entidade, repository, DTOs e validadores (agendamento e cancelamento)
│   ├── usuario/         # Entidade, repository e DTOs de usuário
│   └── endereco/        # Endereço (embeddable) e DTO do ViaCEP
└── infra/
    ├── security/        # JWT, filtro de autenticação, CORS e permissões
    ├── documentation/   # Configuração do Swagger
    └── exception/       # Tratamento global de erros

src/main/resources
├── application.properties        # Configuração padrão (MySQL)
├── application-h2.properties     # Perfil h2: executa sem MySQL
├── application-test.properties   # Perfil dos testes (H2 em memória)
└── db/migration/                 # Migrations do Flyway (V1 a V10)
```

