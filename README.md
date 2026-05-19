# EstoqueFacil API

Java • Spring Boot • PostgreSQL • Docker • Prometheus • Grafana • CI/CD • Observabilidade

Sistema de gestão de estoque para controle de produtos, movimentações de entrada e saída, gerenciamento de lotes com data de validade, relatórios gerenciais, auditoria completa de operações e monitoramento da aplicação em tempo real.

---

# Visão Geral

API REST para controle de estoque que permite gerenciar produtos, categorias, movimentações de entrada e saída (vendas e perdas), controle de lotes com validade, relatórios inteligentes e auditoria de todas as ações.

O projeto foi desenvolvido com foco em:

* arquitetura em camadas
* segurança
* observabilidade
* logs estruturados
* monitoramento de métricas
* containerização
* CI/CD
* boas práticas backend

---

# Problemas Resolvidos

* Controle preciso de estoque com lógica FIFO (First-In-First-Out)
* Alertas automáticos para produtos com estoque baixo ou crítico
* Rastreabilidade completa através de logs de auditoria
* Relatórios financeiros e de performance para tomada de decisão
* Gestão de usuários com diferentes níveis de acesso
* Monitoramento da saúde da aplicação e banco de dados
* Observabilidade com métricas JVM, HTTP e conexões de banco

---

# Público-Alvo

Pequenas e médias empresas que necessitam de um sistema de controle de estoque eficiente, com relatórios gerenciais, auditoria e monitoramento operacional da aplicação.

---

# Tecnologias Utilizadas

| Tecnologia           | Versão | Finalidade                  |
| -------------------- | ------ | --------------------------- |
| Java                 | 21     | Linguagem principal         |
| Spring Boot          | 3.5.11 | Framework principal         |
| Spring Security      | -      | Autenticação e autorização  |
| Spring Data JPA      | -      | Acesso a dados              |
| Spring Boot Actuator | -      | Health checks e métricas    |
| Micrometer           | -      | Coleta de métricas          |
| Prometheus           | -      | Monitoramento de métricas   |
| Grafana              | -      | Visualização e dashboards   |
| PostgreSQL           | 15     | Banco de dados              |
| JWT                  | -      | Autenticação stateless      |
| Logback              | -      | Logs estruturados           |
| iText7               | 7.2.5  | Geração de PDF              |
| Swagger/OpenAPI      | 2.8.9  | Documentação                |
| Docker               | -      | Containerização             |
| GitHub Actions       | -      | CI/CD Pipeline              |
| Maven                | -      | Gerenciador de dependências |
| Lombok               | -      | Redução de boilerplate      |

---

# Observabilidade e Monitoramento

O projeto possui monitoramento e observabilidade utilizando:

* Spring Boot Actuator
* Micrometer
* Prometheus
* Grafana
* Logs estruturados com Logback

Métricas monitoradas:

* uso de memória JVM
* uso de CPU
* threads ativas
* métricas HTTP
* conexões HikariCP
* health check da aplicação
* health check do banco de dados
* tempo de resposta da aplicação

---

# Endpoints de Monitoramento

## Health Check

```http
GET /actuator/health
```

## Métricas

```http
GET /actuator/metrics
```

## Prometheus

```http
GET /actuator/prometheus
```

---

# CI/CD Pipeline

Este projeto utiliza GitHub Actions para Integração Contínua e Entrega Contínua.

## O que acontece automaticamente ao fazer push na branch `main`

| Etapa | Ação                                |
| ----- | ----------------------------------- |
| 1     | Compilação do código Java com Maven |
| 2     | Execução dos testes automatizados   |
| 3     | Geração do arquivo `.jar`           |
| 4     | Build da imagem Docker              |
| 5     | Push da imagem para o Docker Hub    |

---

# Imagem Docker

```bash
docker pull jonatadevsuario/estoquefacil-api:latest
docker run -p 8080:8080 jonatadevsuario/estoquefacil-api:latest
```

---

# Como Rodar o Projeto

## Pré-requisitos

* Java 21
* Docker e Docker Compose
* Maven (opcional)
* PostgreSQL 15

---

## Clonar o Repositório

```bash
git clone https://github.com/seu-usuario/EstoqueFacil.git
cd EstoqueFacil
```

---

## Configurar Variáveis de Ambiente

Crie um arquivo `.env` na raiz do projeto:

```env
# Banco de Dados
SPRING_DATASOURCE_PASSWORD=sua_senha_aqui
POSTGRES_PASSWORD=sua_senha_aqui

# JWT Security
JWT_SECRET=SUA_CHAVE_JWT_AQUI

# Admin Inicial
ADMIN_EMAIL=admin@estoque.com
ADMIN_PASSWORD=Admin@123
ADMIN_NAME=Administrador

# CORS
CORS_ALLOWED_ORIGINS=http://localhost:4200,http://localhost:3000
```

---

## Executar com Docker

```bash
docker compose up --build
```

A aplicação estará disponível em:

```text
http://localhost:8080
```

Grafana:

```text
http://localhost:3000
```

Prometheus:

```text
http://localhost:9090
```

---

## Executar Localmente

```bash
./mvnw clean package -DskipTests
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

---

# Autenticação e Segurança

O sistema utiliza autenticação JWT (JSON Web Token) com expiração de 2 horas.

## Medidas de Segurança Implementadas

* JWT com expiração
* BCrypt para hashing de senhas
* Rate limiting (5 tentativas de login)
* Blacklist de senhas comuns
* SQL Injection Prevention
* Soft delete
* CORS configurado
* Auditoria de ações críticas
* Logs estruturados
* Controle de acesso baseado em roles

---

# Estrutura do Projeto

```text
src/main/java/com/example/EstoqueFacil/
├── config/           # Configurações gerais
├── controller/       # Endpoints REST
├── service/          # Regras de negócio
├── repository/       # Acesso a dados
├── entity/           # Entidades JPA
├── dto/              # Objetos de transferência
├── mapper/           # Conversores Entity/DTO
├── exception/        # Exceptions e handlers globais
├── security/         # JWT, filtros e autenticação
└── specification/    # Filtros e consultas dinâmicas
```

---

# Padrões Adotados

* Arquitetura em camadas
* DTO Pattern
* Repository Pattern
* Global Exception Handler
* Logs estruturados com Logback
* Observabilidade com Actuator + Prometheus + Grafana
* Paginação com Pageable
* Bean Validation
* JWT Stateless Authentication
* Soft Delete
* Role-Based Access Control
* Health Checks
* Métricas de aplicação

---

# Funcionalidades Principais

## Produtos

* Cadastro e atualização
* Código de barras único
* Controle de estoque mínimo
* Preço de custo e venda

## Movimentações

* Entrada e saída de estoque
* FIFO (First-In-First-Out)
* Histórico completo

## Relatórios

* Produtos mais vendidos
* Produtos parados
* Lucro estimado
* Estoque baixo
* Lotes vencendo
* Exportação PDF

## Auditoria

* Logs de CREATE, UPDATE, DELETE, SALE, ENTRY e LOSS
* Histórico por entidade
* Histórico por usuário
* Histórico por ação

---

# Roadmap Futuro

* Dashboard operacional avançado
* Exportação Excel
* Notificações por email
* Redis cache
* Deploy AWS (ECS/RDS)
* OpenTelemetry
* Logs centralizados
* Tracing distribuído
* Multitenancy
* WebSocket para notificações em tempo real

---

# Autor

## Jonata Freitas

* GitHub: `github.com/devJonatas06`
* LinkedIn: `linkedin.com/in/jonatadev`
