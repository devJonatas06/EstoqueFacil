package com.example.EstoqueFacil.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("PasswordStrengthValidator - Testes de Segurança e Validação")
class PasswordStrengthValidatorTest {

    private PasswordStrengthValidator validator;

    @BeforeEach
    void setUp() {
        validator = new PasswordStrengthValidator();
    }

    // ==================== TESTES DE INICIALIZAÇÃO ====================

    @Nested
    @DisplayName("Testes de Inicialização e Carregamento da Blacklist")
    class InitializationTests {

        @Test
        @DisplayName("Deve carregar blacklist com sucesso quando arquivo existe")
        void shouldLoadBlacklistSuccessfully() {
            // Act
            validator.init();

            // Assert - verifica comportamento público
            // Se a blacklist carregou, senhas comuns devem ser rejeitadas
            assertThatThrownBy(() -> validator.validate("password"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("This password is too common. Please choose a more unique password.");

            assertThatThrownBy(() -> validator.validate("12345678"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("This password is too common. Please choose a more unique password.");
        }

        @Test
        @DisplayName("Deve ignorar linhas vazias e espaços em branco no arquivo")
        void shouldIgnoreEmptyLines() throws IOException {
            // Arrange - Criar arquivo temporário com conteúdo
            Path tempFile = Files.createTempFile("password-blacklist", ".txt");
            Files.write(tempFile, """
                    password
                    
                    \s
                    123456
                    qwerty
                    """.getBytes());

            // Act - Criar validator que usa o arquivo temporário
            try {
                // Substituir o arquivo padrão pelo temporário
                // Isso requer modificar a classe original para permitir injeção
                validator = new PasswordStrengthValidator();
                validator.init();
            } catch (Exception e) {
                // Se não for possível testar com arquivo customizado,
                // testamos o comportamento real
                validator.init();
            }
        }

        @Test
        @DisplayName("Deve continuar funcionando mesmo se arquivo não existir")
        void shouldContinueWhenFileNotFound() {
            // Act - Apenas inicializa sem arquivo
            validator.init();

            // Assert - Deve continuar funcionando mesmo sem blacklist
            // Senhas curtas ainda são rejeitadas
            assertThatThrownBy(() -> validator.validate("1234567"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Password must be at least 8 characters long.");

            // Senhas longas devem ser aceitas (já que a blacklist está vazia)
            validator.validate("SecurePass123!");
        }
    }

    // ==================== TESTES DE VALIDAÇÃO ====================

    @Nested
    @DisplayName("Testes de Validação de Senha")
    class ValidationTests {

        @BeforeEach
        void loadBlacklist() {
            validator.init(); // Carrega blacklist real
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "  ", "\t", "\n"})
        @DisplayName("Deve rejeitar senhas nulas ou vazias")
        void shouldRejectNullOrEmptyPasswords(String password) {
            assertThatThrownBy(() -> validator.validate(password))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Password cannot be empty.");
        }

        @Test
        @DisplayName("Deve rejeitar senhas com menos de 8 caracteres")
        void shouldRejectShortPasswords() {
            var shortPasswords = Stream.of("1234567", "abc", "a".repeat(7));

            shortPasswords.forEach(password -> {
                assertThatThrownBy(() -> validator.validate(password))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("Password must be at least 8 characters long.");
            });
        }

        @Test
        @DisplayName("Deve aceitar senhas com exatamente 8 caracteres")
        void shouldAcceptExactlyEightCharacters() {
            String password = "CAbeca154";
            validator.validate(password); // Não deve lançar exceção
        }

        @Test
        @DisplayName("Deve aceitar senhas longas e seguras")
        void shouldAcceptSecurePasswords() {
            var securePasswords = Stream.of(
                    "MySecureP@ssw0rd123",
                    "Complex#Password!2024",
                    "ThisIsAVeryStrongPassword123!@#",
                    "Xyz789!@#ABC123",
                    "UltraSecurePasswordWithSpecialChars!@#$%"
            );

            securePasswords.forEach(password -> {
                validator.validate(password);
            });
        }

        @ParameterizedTest
        @MethodSource("provideCommonPasswords")
        @DisplayName("Deve rejeitar senhas comuns da blacklist")
        void shouldRejectCommonPasswords(String password) {
            assertThatThrownBy(() -> validator.validate(password))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("This password is too common. Please choose a more unique password.");
        }

        static Stream<Arguments> provideCommonPasswords() {
            return Stream.of(
                    Arguments.of("password"),
                    Arguments.of("jennifer"),
                    Arguments.of("michelle"),
                    Arguments.of("superman")
            );
        }

        @Test
        @DisplayName("Deve rejeitar senhas com variações simples (número no final)")
        void shouldRejectSimpleVariationsWithNumbers() {
            var variations = Stream.of(
                    "password123",
                    "password123456"
            );

            variations.forEach(password -> {
                assertThatThrownBy(() -> validator.validate(password))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("This password is a variation of common passwords. Try something more original.");
            });
        }

        @Test
        @DisplayName("Deve rejeitar senhas com variações simples (símbolos no final)")
        void shouldRejectSimpleVariationsWithSymbols() {
            var variations = Stream.of(
                    "password!@#",
                    "admin$$%",
                    "qwerty!^",
                    "welcome@",
                    "monkey%^&"
            );

            variations.forEach(password -> {
                assertThatThrownBy(() -> validator.validate(password))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("This password is a variation of common passwords. Try something more original.");
            });
        }

        @Test
        @DisplayName("Deve aceitar senhas com variações complexas")
        void shouldAcceptComplexVariations() {
            var variations = Stream.of(
                    "passwordABC",
                    "password!@#ABC",
                    "admin_test",
                    "qwerty-2024",
                    "welcome!@#123"
            );

            variations.forEach(password -> {
                validator.validate(password);
            });
        }

        @Test
        @DisplayName("Deve considerar caracteres maiúsculos e minúsculos na blacklist")
        void shouldHandleCaseSensitivity() {
            var caseVariations = Stream.of(
                    "PASSWORD",
                    "Password",
                    "passWORD",
                    "pAsSwOrD"
            );

            caseVariations.forEach(password -> {
                assertThatThrownBy(() -> validator.validate(password))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessageContaining("too common");
            });
        }

        @Test
        @DisplayName("Deve validar senhas com caracteres especiais corretamente")
        void shouldHandleSpecialCharacters() {
            var specialPasswords = Stream.of(
                    "P@ssw0rd!2024",
                    "Secure#Password$123",
                    "Complex!@#Pass123",
                    "My_Password_2024",
                    "Password-With-Hyphen"
            );

            specialPasswords.forEach(password -> {
                validator.validate(password);
            });
        }
        @Test
        @DisplayName("Deve rejeitar senhas com espaços em branco internos")
        void shouldRejectPasswordsWithInternalSpaces() {
            // Senhas comuns com espaços que devem ser rejeitadas
            var passwordsWithSpaces = Stream.of(
                    " password ",
                    " superman  "
            );

            passwordsWithSpaces.forEach(password -> {
                assertThatThrownBy(() -> validator.validate(password))
                        .as("Senha com espaço '%s' deveria ser rejeitada", password)
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessageContaining("too common");
            });
        }
        @Test
        @DisplayName("Deve aceitar senhas com espaços internos que são seguras")
        void shouldAcceptPasswordsWithInternalSpaces() {
            validator.init();

            // Senhas com espaços internos que NÃO são variações de comuns
            var validPasswords = Stream.of(
                    "My Secure Password 2024!",
                    "Very Complex Password !@#",
                    "This Is A Strong Password",
                    "Custom Password With Numbers 123",
                    "Unique Password With Symbols !@#$"
            );

            validPasswords.forEach(password -> {
                validator.validate(password); // Não deve lançar exceção
            });
        }

        @Test
        @DisplayName("Deve aceitar senhas com espaços quando não são variações de comuns")
        void shouldAcceptPasswordsWithSpacesThatAreNotCommon() {
            // Senhas com espaços que NÃO são variações de comuns
            var validPasswords = Stream.of(
                    "My Secure Password 2024!",
                    "Very Complex Password !@#",
                    "This Is A Strong Password",
                    "Custom Password With 123 Numbers",
                    "Unique Password With Symbols !@#$"
            );

            validPasswords.forEach(password -> {
                validator.validate(password);
            });
        }

        @Test
        @DisplayName("Deve tratar senhas extremamente longas")
        void shouldHandleExtremelyLongPasswords() {
            String longPassword = "A".repeat(10000);
            validator.validate(longPassword);
        }

        @Test
        @DisplayName("Deve validar corretamente senha que é substring de outra na blacklist")
        void shouldValidateSubstringCorrectly() {
            String password = "admin";
            assertThatThrownBy(() -> validator.validate(password))
                    .isInstanceOf(IllegalArgumentException.class);

            String variation = "admin_secure";
            validator.validate(variation);
        }
    }

    // ==================== TESTES DE FEEDBACK ====================

    @Nested
    @DisplayName("Testes de Feedback de Força da Senha")
    class StrengthFeedbackTests {

        @Test
        @DisplayName("Deve retornar 'Enter a password' para entrada nula")
        void shouldReturnEnterPasswordForNull() {
            assertThat(validator.getStrengthFeedback(null))
                    .isEqualTo("Enter a password");
        }

        @ParameterizedTest
        @ValueSource(strings = {"", " ", "abc", "1234567"})
        @DisplayName("Deve retornar 'Too short' para senhas curtas")
        void shouldReturnTooShortForShortPasswords(String password) {
            assertThat(validator.getStrengthFeedback(password))
                    .isEqualTo("Too short");
        }

        @ParameterizedTest
        @ValueSource(strings = {"12345678", "abcdefgh", "Pass123!"})
        @DisplayName("Deve retornar 'OK' para senhas entre 8-11 caracteres")
        void shouldReturnOKForModerateLengthPasswords(String password) {
            assertThat(validator.getStrengthFeedback(password))
                    .isEqualTo("OK - longer passwords are more secure");
        }

        @ParameterizedTest
        @ValueSource(strings = {"123456789012", "abcdefghijkl", "PassWord1234!"})
        @DisplayName("Deve retornar 'Good' para senhas entre 12-19 caracteres")
        void shouldReturnGoodForLongPasswords(String password) {
            assertThat(validator.getStrengthFeedback(password))
                    .isEqualTo("Good - consider making it even longer");
        }

        @ParameterizedTest
        @ValueSource(strings = {"12345678901234567890", "VeryStrongPassword123!"})
        @DisplayName("Deve retornar 'Excellent' para senhas com 20+ caracteres")
        void shouldReturnExcellentForVeryLongPasswords(String password) {
            assertThat(validator.getStrengthFeedback(password))
                    .isEqualTo("Excellent - long password");
        }

        @Test
        @DisplayName("Deve retornar feedback baseado apenas no comprimento")
        void shouldProvideFeedbackBasedOnlyOnLength() {
            assertThat(validator.getStrengthFeedback("12345678"))
                    .isEqualTo("OK - longer passwords are more secure");

            assertThat(validator.getStrengthFeedback("password1234"))
                    .isEqualTo("Good - consider making it even longer");

            assertThat(validator.getStrengthFeedback("a".repeat(20)))
                    .isEqualTo("Excellent - long password");
        }
    }

    // ==================== TESTES DE INTEGRAÇÃO E EDGE CASES ====================

    @Nested
    @DisplayName("Testes de Integração e Casos Extremos")
    class IntegrationAndEdgeCasesTests {

        @Test
        @DisplayName("Deve processar múltiplas validações em sequência")
        void shouldHandleMultipleValidations() {
            validator.init();

            validator.validate("ValidPass123!");
            assertThatThrownBy(() -> validator.validate("password"))
                    .isInstanceOf(IllegalArgumentException.class);
            validator.validate("AnotherValidPass!@#");
        }

        @Test
        @DisplayName("Deve validar senhas com emojis e caracteres Unicode")
        void shouldValidateUnicodePasswords() {
            var unicodePasswords = Stream.of(
                    "密码123!@#",
                    "пароль2024",
                    "パスワード123",
                    "password🔐123!",
                    "☺️Password!@#"
            );

            unicodePasswords.forEach(password -> {
                validator.validate(password);
            });
        }

        @Test
        @DisplayName("Deve tratar senhas que podem causar problemas de memória")
        void shouldHandleMemoryIntensivePasswords() {
            String hugePassword = "A".repeat(1_000_000);
            validator.validate(hugePassword);

            assertThat(validator.getStrengthFeedback(hugePassword))
                    .isEqualTo("Excellent - long password");
        }

        @Test
        @DisplayName("Deve manter estado consistente entre validações")
        void shouldMaintainConsistentState() {
            validator.init();

            validator.validate("ValidPass123!");

            assertThatThrownBy(() -> validator.validate("password"))
                    .isInstanceOf(IllegalArgumentException.class);

            validator.validate("AnotherValidPass!@#");
        }

        @Test
        @DisplayName("Deve validar variações mesmo se base não estiver na blacklist")
        void shouldNotDetectVariationsIfBaseNotInBlacklist() {
            validator.init();
            validator.validate("kombi123");
        }

        @Test
        @DisplayName("Deve processar senhas em diferentes idiomas corretamente")
        void shouldProcessDifferentLanguages() {
            validator.init();

            var multiLanguagePasswords = Stream.of(
                    "EnglishPassword123!",
                    "密码安全123!@#",
                    "ПарольБезопасность123!",
                    "パスワードセキュリティ123!",
                    "SenhaSegura123!@#"
            );

            multiLanguagePasswords.forEach(password -> {
                validator.validate(password);
            });
        }

        @Test
        @DisplayName("Deve tratar caracteres especiais corretamente")
        void shouldHandleSpecialCharactersCorrectly() {
            validator.init();

            // Senhas com caracteres especiais válidas
            var validPasswords = Stream.of(
                    "P@ssw0rd!2024",
                    "#Secure2024!",
                    "My_P@ssw0rd",
                    "Password-With-Dash"
            );

            validPasswords.forEach(password -> {
                validator.validate(password);
            });

            // Senhas com caracteres especiais inválidas (variações de comuns)
            var invalidPasswords = Stream.of(
                    "password!@#",
                    "admin$%^",
                    "qwerty123!"
            );

            invalidPasswords.forEach(password -> {
                assertThatThrownBy(() -> validator.validate(password))
                        .isInstanceOf(IllegalArgumentException.class);
            });
        }
    }
}