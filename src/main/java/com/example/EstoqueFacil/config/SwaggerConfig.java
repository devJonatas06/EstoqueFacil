package com.example.EstoqueFacil.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "EstoqueFacil API",
        version = "v1.0.0",
        description = """
            API para gerenciamento de estoque com funcionalidades completas:
            
            - 📦 **Produtos**: CRUD completo, controle de estoque por lote, código de barras
            - 📂 **Categorias**: Organização de produtos por categorias
            - 📊 **Movimentações**: Entrada (compra), saída (venda/perda) com lógica FIFO
            - 📈 **Relatórios**: Lucro por período, produtos mais/menos vendidos, alertas
            - 👥 **Usuários**: Controle de acesso com roles ADMIN e EMPLOYEE
            - 🔍 **Auditoria**: Rastreamento completo de todas as ações
            - 📄 **Exportação**: Relatórios em PDF
            
            ## 🔐 Autenticação
            Para acessar os endpoints protegidos, utilize o token JWT obtido via `/auth/login`.
            Clique no botão "Authorize" abaixo e informe: `Bearer {seu_token}`
            
            ## 👤 Roles
            - **ADMIN**: Acesso total (criar/editar/desativar produtos, categorias, usuários)
            - **EMPLOYEE**: Acesso apenas para consultas e movimentações de estoque
            """,
        contact = @Contact(
            name = "EstoqueFacil",
            email = "suporte@estoquefacil.com",
            url = "https://github.com/seu-usuario/EstoqueFacil"
        ),
        license = @License(
            name = "MIT License",
            url = "https://opensource.org/licenses/MIT"
        ),
        termsOfService = "https://estoquefacil.com/terms"
    ),
    servers = {
        @Server(
            description = "Ambiente Local (Docker)",
            url = "http://localhost:8080"
        ),
        @Server(
            description = "Ambiente de Desenvolvimento",
            url = "http://dev.estoquefacil.com:8080"
        ),
        @Server(
            description = "Ambiente de Produção",
            url = "https://api.estoquefacil.com"
        )
    },
    security = @SecurityRequirement(name = "bearer-auth")
)
@SecurityScheme(
    name = "bearer-auth",
    description = "JWT Token de autenticação. Obtenha o token via POST /auth/login",
    scheme = "bearer",
    type = SecuritySchemeType.HTTP,
    bearerFormat = "JWT",
    in = SecuritySchemeIn.HEADER
)
public class SwaggerConfig {
}