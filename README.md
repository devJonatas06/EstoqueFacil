# EstoqueFacil API

Sistema de gestão de estoque para controle de produtos, movimentações de entrada e saída, gerenciamento de lotes com data de validade, relatórios gerenciais e auditoria completa de operações.

---

## Visão Geral

API REST para controle de estoque que permite gerenciar produtos, categorias, movimentações de entrada e saída (vendas e perdas), controle de lotes com validade, relatórios inteligentes e auditoria de todas as ações. Desenvolvida com foco em segurança, boas práticas de arquitetura e preparada para containerização.

Problemas resolvidos:
- Controle preciso de estoque com lógica FIFO (First-In-First-Out)
- Alertas automáticos para produtos com estoque baixo ou crítico
- Rastreabilidade completa através de logs de auditoria
- Relatórios financeiros e de performance para tomada de decisão
- Gestão de usuários com diferentes níveis de acesso

Público-alvo: Pequenas e médias empresas que necessitam de um sistema de controle de estoque eficiente, com relatórios gerenciais e histórico de operações.

---

## Tecnologias Utilizadas

- Java 21
- Spring Boot 3.5.11
- Spring Security (JWT)
- Spring Data JPA / Hibernate
- PostgreSQL 15
- Flyway (migrations)
- Lombok
- Maven
- Swagger / OpenAPI 3.0
- iText7 (geração de PDF)
- Docker / Docker Compose

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

Crie um arquivo .env na raiz do projeto com as seguintes variáveis:
```
#Banco de Dados
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

```
docker-compose up -d

```
 A aplicação estará disponível em http://localhost:8080
 
### Executar localmente
```
./mvnw clean package -DskipTests
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

## Autenticação e Segurança

O sistema utiliza autenticação via JWT (JSON Web Token) com expiração de 2 horas.
Registrar novo usuário

```
POST /auth/register
Content-Type: application/json

{
  "name": "João Silva",
  "email": "joao@empresa.com",
  "password": "SenhaForte@123"
}
```
### Login
```
POST /auth/login
Content-Type: application/json

{
  "email": "admin@estoque.com",
  "password": "Admin@123"
}
```
Resposta:
```
{
  "name": "Administrador",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```
### Utilizar o token

**Inclua o token no header de todas as requisições autenticadas:**

```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```
### Roles e Permissões

**ADMIN**:	Acesso total: criar/editar/desativar produtos, categorias e usuários

**EMPLOYEE**:	Acesso para consultas e movimentações de estoque

## Documentação da API

A documentação interativa está disponível via Swagger UI:
text

http://localhost:8080/swagger-ui.html

## Endpoints Principais

| Método | Endpoint | Descrição | Permissão |
|--------|----------|-----------|-----------|
| POST | /auth/login | Autenticação | Público |
| POST | /auth/register | Registrar usuário | Público |
| POST | /api/v1/categories | Criar categoria | ADMIN |
| GET | /api/v1/categories | Listar categorias | ADMIN / EMPLOYEE |
| POST | /api/v1/products | Criar produto | ADMIN |
| GET | /api/v1/products | Listar produtos | ADMIN / EMPLOYEE |
| POST | /api/v1/stock/entry | Entrada de estoque | ADMIN / EMPLOYEE |
| POST | /api/v1/stock/exit | Saída de estoque | ADMIN / EMPLOYEE |
| GET | /api/v1/reports/profit | Relatório de lucro | ADMIN / EMPLOYEE |
| GET | /api/v1/reports/export/pdf | Exportar relatórios PDF | ADMIN / EMPLOYEE |
| POST | /api/v1/users | Criar usuário | ADMIN |
| GET | /api/v1/audit | Consultar auditoria | ADMIN |
---
## Estrutura do Projeto

src/main/java/com/example/EstoqueFacil/
- config/           (Configuracoes: Security, Swagger, CORS)
- controller/       (Endpoints REST)
- service/          (Regras de negocio)
- repository/       (Acesso a dados)
- entity/           (Entidades JPA)
- dto/              (Objetos de transferencia)
- mapper/           (Conversores Entity/DTO)
- exception/        (Excecoes e handler global)
- security/         (JWT, filtros, validacao de senha)
- specification/    (Consultas dinâmicas)

Funcionalidades Principais
Produtos

    Cadastro, edição, consulta e desativação de produtos

    Código de barras único

    Preço de custo e preço de venda

    Estoque mínimo configurável

Categorias

    Organização de produtos por categoria

    Ativação e desativação

Movimentações de Estoque

    Entrada de produtos (compra) com controle de lote e validade

    Saída de produtos (venda ou perda)

    Lógica FIFO (First-In-First-Out) para baixa de estoque

    Histórico completo de movimentações

Relatórios

    Produtos mais e menos vendidos

    Lucro estimado por período (resumido e detalhado por produto)

    Produtos parados há X dias

    Produtos com estoque abaixo do mínimo

    Lotes próximos ao vencimento

    Histórico de movimentações por período

    Exportação de todos os relatórios em PDF

Alertas Inteligentes

    Produtos com estoque baixo

    Produtos em situação crítica

    Produtos sem movimentação (parados)

    Lotes vencidos e próximos ao vencimento

Auditoria

    Registro de todas as ações (CREATE, UPDATE, DELETE, SALE, ENTRY, LOSS)

    Armazenamento de valores antes/depois da alteração

    Consulta de logs por entidade, usuário e ação

Usuários

    Cadastro de funcionários (role EMPLOYEE)

    Promoção para ADMIN

    Desativação de usuários

Regras de Negócio Importantes

    Código de barras do produto deve ser único no sistema

    Preço de venda deve ser maior que preço de custo

    Quantidade em estoque nunca pode ser negativa

    Saída de estoque verifica disponibilidade antes de processar

    Utiliza lógica FIFO para baixar produtos dos lotes mais antigos

    Produtos com estoque abaixo do mínimo geram alerta automático

    Lotes vencidos são identificados e geram relatório de perdas

    Usuários registrados recebem role EMPLOYEE por padrão

    Apenas ADMIN pode criar/editar/desativar produtos e categorias

    Apenas ADMIN pode criar/desativar usuários e alterar roles

    Token JWT expira após 2 horas

    Após 5 tentativas de login falhas, a conta é temporariamente bloqueada

    Senha deve ter no mínimo 8 caracteres e não pode estar na blacklist de senhas comuns

## Testes

```
# Executar todos os testes
./mvnw test

# Executar apenas testes de unidade
./mvnw test -Dtest=*Test

# Executar testes de integração
./mvnw test -Dtest=*IT
```

## Padrões Adotados

    Controller -> Service -> Repository: separação clara de responsabilidades

    DTOs para comunicação entre camadas

    Mappers para conversão Entity/DTO

    GlobalExceptionHandler para tratamento centralizado de erros

    ErrorResponseDTO padronizado com timestamp, status, message e traceId

    Logs estruturados com níveis apropriados (info, warn, error)

    Paginação em listagens (Pageable)

    Validação com Bean Validation (@Valid)

    @PreAuthorize para controle de acesso baseado em roles

    JWT stateless para autenticação

    Soft delete (desativação lógica) para produtos, categorias e usuários

## Roadmap

    Dashboard com gráficos e métricas em tempo real

    Exportação de relatórios em Excel

    Envio de alertas por email (estoque baixo, lotes vencendo)

    Suporte a múltiplas empresas (multitenancy)

    Integração com leitores de código de barras

    API de notificações via WebSocket

    Cache com Redis para consultas frequentes

    Deploy na AWS (ECS / RDS)

# Autor:
## Jonata Freitas

### GitHub: https://github.com/devJonatas06

### LinkedIn: www.linkedin.com/in/jonatadev


