# EstoqueFacil API

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.11-brightgreen)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue)
![Docker](https://img.shields.io/badge/Docker-ready-blue)
![License](https://img.shields.io/badge/License-MIT-green)

Sistema de gestão de estoque para controle de produtos, movimentações de entrada e saída, gerenciamento de lotes com data de validade, relatórios gerenciais e auditoria completa de operações.

---

## Visão Geral

API REST para controle de estoque que permite gerenciar produtos, categorias, movimentações de entrada e saída (vendas e perdas), controle de lotes com validade, relatórios inteligentes e auditoria de todas as ações. Desenvolvida com foco em segurança, boas práticas de arquitetura e preparada para containerização.

**Problemas resolvidos:**
- Controle preciso de estoque com lógica FIFO (First-In-First-Out)
- Alertas automáticos para produtos com estoque baixo ou crítico
- Rastreabilidade completa através de logs de auditoria
- Relatórios financeiros e de performance para tomada de decisão
- Gestão de usuários com diferentes níveis de acesso

**Público-alvo:** Pequenas e médias empresas que necessitam de um sistema de controle de estoque eficiente, com relatórios gerenciais e histórico de operações.

---

## Tecnologias Utilizadas

| Tecnologia | Versão | Finalidade |
|------------|--------|------------|
| Java | 21 | Linguagem principal |
| Spring Boot | 3.5.11 | Framework principal |
| Spring Security | - | Autenticação e autorização |
| Spring Data JPA | - | Acesso a dados |
| PostgreSQL | 15 | Banco de dados |
| JWT | - | Autenticação stateless |
| iText7 | 7.2.5 | Geração de PDF |
| Swagger/OpenAPI | 2.8.9 | Documentação |
| Docker | - | Containerização |
| Maven | - | Gerenciador de dependências |
| Lombok | - | Redução de boilerplate |

---

## Como Rodar o Projeto

### Pré-requisitos

- Java 21
- Docker e Docker Compose (recomendado)
- Maven (opcional, se não usar Docker)
- PostgreSQL 15 (se for rodar local)

### Clonar o repositório

```bash
git clone https://github.com/seu-usuario/EstoqueFacil.git
cd EstoqueFacil
```

### Configurar variáveis de ambiente

Crie um arquivo `.env` na raiz do projeto com as seguintes variáveis:

```env
# Banco de Dados
SPRING_DATASOURCE_PASSWORD=sua_senha_aqui
POSTGRES_PASSWORD=sua_senha_aqui

# JWT Security (gere uma chave com: openssl rand -hex 32)
JWT_SECRET=SUA_CHAVE_JWT_AQUI

# Admin inicial
ADMIN_EMAIL=admin@estoque.com
ADMIN_PASSWORD=Admin@123
ADMIN_NAME=Administrador

# CORS
CORS_ALLOWED_ORIGINS=http://localhost:4200,http://localhost:3000
```

### Executar com Docker (recomendado)

```bash
docker-compose up -d
```

A aplicação estará disponível em `http://localhost:8080`

### Executar localmente

```bash
./mvnw clean package -DskipTests
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

---

## Autenticação e Segurança

O sistema utiliza autenticação via JWT (JSON Web Token) com expiração de 2 horas.

### Registrar novo usuário

```http
POST /auth/register
Content-Type: application/json

{
  "name": "João Silva",
  "email": "joao@empresa.com",
  "password": "SenhaForte@123"
}
```

### Login

```http
POST /auth/login
Content-Type: application/json

{
  "email": "admin@estoque.com",
  "password": "Admin@123"
}
```

**Resposta:**

```json
{
  "name": "Administrador",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

### Utilizar o token

Inclua o token no header de todas as requisições autenticadas:

```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### Roles e Permissões

| Role | Permissões |
|------|------------|
| **ADMIN** | Acesso total: criar/editar/desativar produtos, categorias e usuários |
| **EMPLOYEE** | Acesso para consultas e movimentações de estoque |

### Medidas de Segurança Implementadas

- [x] JWT com expiração (2 horas)
- [x] BCrypt para hashing de senhas
- [x] Rate limiting (5 tentativas de login)
- [x] Blacklist de senhas comuns (10.000+ senhas)
- [x] SQL Injection prevention (JPA parametrizado)
- [x] CORS configurado
- [x] Soft delete para dados sensíveis
- [x] Logs de auditoria para ações críticas

---

## Documentação da API

A documentação interativa está disponível via Swagger UI:

```
http://localhost:8080/swagger-ui.html
```

---

## Endpoints Principais

| Método | Endpoint | Descrição | Permissão |
|--------|----------|-----------|-----------|
| POST | /auth/login | Autenticação | Público |
| POST | /auth/register | Registrar usuário | Público |
| POST | /api/v1/categories | Criar categoria | ADMIN |
| GET | /api/v1/categories | Listar categorias | ADMIN / EMPLOYEE |
| PUT | /api/v1/categories/{id} | Atualizar categoria | ADMIN |
| DELETE | /api/v1/categories/{id} | Desativar categoria | ADMIN |
| POST | /api/v1/products | Criar produto | ADMIN |
| GET | /api/v1/products | Listar produtos | ADMIN / EMPLOYEE |
| GET | /api/v1/products/{id} | Buscar produto por ID | ADMIN / EMPLOYEE |
| PUT | /api/v1/products/{id} | Atualizar produto | ADMIN |
| DELETE | /api/v1/products/{id} | Desativar produto | ADMIN |
| GET | /api/v1/products/search | Buscar produtos por nome | ADMIN / EMPLOYEE |
| GET | /api/v1/products/barcode/{barcode} | Buscar por código de barras | ADMIN / EMPLOYEE |
| GET | /api/v1/products/filter | Filtros avançados | ADMIN / EMPLOYEE |
| POST | /api/v1/stock/entry | Registrar entrada de estoque | ADMIN / EMPLOYEE |
| POST | /api/v1/stock/exit | Registrar saída de estoque | ADMIN / EMPLOYEE |
| GET | /api/v1/stock/movements | Histórico de movimentações | ADMIN / EMPLOYEE |
| GET | /api/v1/stock/current/{productId} | Consultar estoque atual | ADMIN / EMPLOYEE |
| GET | /api/v1/reports/alerts/summary | Resumo de alertas | ADMIN / EMPLOYEE |
| GET | /api/v1/reports/alerts/details | Detalhes dos alertas | ADMIN / EMPLOYEE |
| GET | /api/v1/reports/best-sellers | Produtos mais vendidos | ADMIN / EMPLOYEE |
| GET | /api/v1/reports/worst-sellers | Produtos menos vendidos | ADMIN / EMPLOYEE |
| GET | /api/v1/reports/profit | Relatório de lucro | ADMIN / EMPLOYEE |
| GET | /api/v1/reports/inactive | Produtos parados | ADMIN / EMPLOYEE |
| GET | /api/v1/reports/low-stock | Estoque baixo | ADMIN / EMPLOYEE |
| GET | /api/v1/reports/expiring | Lotes próximos ao vencimento | ADMIN / EMPLOYEE |
| GET | /api/v1/reports/movements | Histórico por período | ADMIN / EMPLOYEE |
| GET | /api/v1/reports/export/pdf | Exportar relatórios PDF | ADMIN / EMPLOYEE |
| POST | /api/v1/users | Criar usuário | ADMIN |
| GET | /api/v1/users | Listar usuários | ADMIN |
| GET | /api/v1/users/{id} | Buscar usuário por ID | ADMIN |
| GET | /api/v1/users/email/{email} | Buscar usuário por email | ADMIN / EMPLOYEE |
| DELETE | /api/v1/users/{id} | Desativar usuário | ADMIN |
| PUT | /api/v1/users/{id}/role | Alterar role do usuário | ADMIN |
| GET | /api/v1/audit | Consultar auditoria | ADMIN |
| GET | /api/v1/audit/entity/{entityType}/{entityId} | Logs por entidade | ADMIN |
| GET | /api/v1/audit/user/{userId} | Logs por usuário | ADMIN |
| GET | /api/v1/audit/action/{action} | Logs por ação | ADMIN |

---

## Estrutura do Projeto

```
src/main/java/com/example/EstoqueFacil/
├── config/           # Configurações (Security, Swagger, CORS, Pageable)
├── controller/       # Endpoints REST (7 controllers)
├── service/          # Regras de negócio (14 services)
├── repository/       # Acesso a dados (8 repositories)
├── entity/           # Entidades JPA (8 entities)
├── dto/              # Objetos de transferência (35+ DTOs)
├── mapper/           # Conversores entre Entity e DTO (4 mappers)
├── exception/        # Exceções e handler global
├── security/         # JWT, filtros, validação de senha
└── specification/    # Consultas dinâmicas
```

### Descrição das Camadas

| Camada | Responsabilidade |
|--------|------------------|
| controller | Recebe requisições HTTP, valida dados, retorna respostas |
| service | Implementa regras de negócio e orquestra operações |
| repository | Acesso ao banco de dados, queries JPA e JPQL |
| entity | Mapeamento objeto-relacional com JPA |
| dto | Transferência de dados entre camadas |
| mapper | Conversão entre Entity e DTO |
| exception | Exceções customizadas e tratamento global de erros |
| security | Autenticação JWT, filtros, validação de senha |
| config | Configurações gerais (Swagger, CORS, paginação) |
| specification | Consultas dinâmicas e filtros avançados |

---

## Funcionalidades Principais

### Produtos
- Cadastro, edição, consulta e desativação de produtos
- Código de barras único
- Preço de custo e preço de venda
- Estoque mínimo configurável

### Categorias
- Organização de produtos por categoria
- Ativação e desativação

### Movimentações de Estoque
- Entrada de produtos (compra) com controle de lote e validade
- Saída de produtos (venda ou perda)
- Lógica FIFO (First-In-First-Out) para baixa de estoque
- Histórico completo de movimentações

### Relatórios
- Produtos mais e menos vendidos
- Lucro estimado por período (resumido e detalhado por produto)
- Produtos parados há X dias
- Produtos com estoque abaixo do mínimo
- Lotes próximos ao vencimento
- Histórico de movimentações por período
- Exportação de todos os relatórios em PDF

### Alertas Inteligentes
- Produtos com estoque baixo
- Produtos em situação crítica
- Produtos sem movimentação (parados)
- Lotes vencidos e próximos ao vencimento

### Auditoria
- Registro de todas as ações (CREATE, UPDATE, DELETE, SALE, ENTRY, LOSS)
- Armazenamento de valores antes/depois da alteração
- Consulta de logs por entidade, usuário e ação

### Usuários
- Cadastro de funcionários (role EMPLOYEE)
- Promoção para ADMIN
- Desativação de usuários

---

## Regras de Negócio Importantes

- Código de barras do produto deve ser único no sistema
- Preço de venda deve ser maior que preço de custo
- Quantidade em estoque nunca pode ser negativa
- Saída de estoque verifica disponibilidade antes de processar
- Utiliza lógica FIFO para baixar produtos dos lotes mais antigos
- Produtos com estoque abaixo do mínimo geram alerta automático
- Lotes vencidos são identificados e geram relatório de perdas
- Usuários registrados recebem role EMPLOYEE por padrão
- Apenas ADMIN pode criar/editar/desativar produtos e categorias
- Apenas ADMIN pode criar/desativar usuários e alterar roles
- Token JWT expira após 2 horas
- Após 5 tentativas de login falhas, a conta é temporariamente bloqueada
- Senha deve ter no mínimo 8 caracteres e não pode estar na blacklist de senhas comuns

---
## Testando a API com Insomnia/Postman

### Importar a Coleção

Você pode importar a coleção completa do Insomnia/Postman para testar todos os endpoints.

**Insomnia:**
1. Abra o Insomnia
2. Clique em `Preferences` > `Data` > `Import Data`
3. Selecione o arquivo `insomnia_collection.json`

**Postman:**
1. Abra o Postman
2. Clique em `Import`
3. Selecione o arquivo `postman_collection.json`

### Fluxo Básico de Testes

1. **Registrar um usuário**
   ```
   POST /auth/register
   Body: {"name": "Teste", "email": "teste@email.com", "password": "Teste@123"}
   ```

2. **Fazer login**
   ```
   POST /auth/login
   Body: {"email": "admin@estoque.com", "password": "Admin@123"}
   ```

3. **Copiar o token recebido**

4. **Configurar o header Authorization**
   ```
   Authorization: Bearer {seu_token}
   ```

5. **Testar os endpoints protegidos**
    - Criar categoria
    - Criar produto
    - Registrar entrada de estoque
    - Consultar relatórios
    - Exportar PDF

---

## Padrões Adotados

- Controller -> Service -> Repository: separação clara de responsabilidades
- DTOs para comunicação entre camadas
- Mappers para conversão Entity/DTO
- GlobalExceptionHandler para tratamento centralizado de erros
- ErrorResponseDTO padronizado com timestamp, status, message e traceId
- Logs estruturados com níveis apropriados (info, warn, error)
- Paginação em listagens (Pageable)
- Validação com Bean Validation (@Valid)
- @PreAuthorize para controle de acesso baseado em roles
- JWT stateless para autenticação
- Soft delete (desativação lógica) para produtos, categorias e usuários
---
## Roadmap

- [ ] Dashboard com gráficos e métricas em tempo real
- [ ] Exportação de relatórios em Excel
- [ ] Envio de alertas por email (estoque baixo, lotes vencendo)
- [ ] Suporte a múltiplas empresas (multitenancy)
- [ ] Integração com leitores de código de barras
- [ ] API de notificações via WebSocket
- [ ] Cache com Redis para consultas frequentes
- [ ] Deploy na AWS (ECS / RDS)

---

## Monitoramento e Health Checks

```bash
# Health Check da aplicação
GET /actuator/health

# Health Check do banco (via Docker)
docker-compose ps
```

---

## Autor

**Jonata Freitas**

- GitHub: [github.com/devJonatas06](https://github.com/devJonatas06)
- LinkedIn: [linkedin.com/in/jonatadev](https://linkedin.com/in/jonatadev)

---

