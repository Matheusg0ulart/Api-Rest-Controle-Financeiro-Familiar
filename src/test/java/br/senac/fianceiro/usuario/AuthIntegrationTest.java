package br.senac.fianceiro.usuario;

import br.senac.fianceiro.usuario.dto.LoginRequest;
import br.senac.fianceiro.usuario.dto.RegistroUsuarioRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Deve registrar um novo usuário com sucesso e retornar token JWT")
    void deveRegistrarUsuarioComSucesso() throws Exception {
        RegistroUsuarioRequest request = RegistroUsuarioRequest.builder()
                .nome("Carlos Silva")
                .email("carlos@teste.com")
                .senha("senhaForte123")
                .build();

        mockMvc.perform(post("/api/auth/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sucesso").value(true))
                .andExpect(jsonPath("$.dados.token").isNotEmpty())
                .andExpect(jsonPath("$.dados.usuario.nome").value("Carlos Silva"))
                .andExpect(jsonPath("$.dados.usuario.email").value("carlos@teste.com"));
    }

    @Test
    @DisplayName("Não deve permitir registrar usuário com e-mail duplicado")
    void naoDevePermitirEmailDuplicado() throws Exception {
        RegistroUsuarioRequest request = RegistroUsuarioRequest.builder()
                .nome("Carlos Silva")
                .email("carlos@teste.com")
                .senha("senhaForte123")
                .build();

        mockMvc.perform(post("/api/auth/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.sucesso").value(false))
                .andExpect(jsonPath("$.mensagem").value("Já existe um usuário cadastrado com este e-mail."));
    }

    @Test
    @DisplayName("Deve autenticar com sucesso e retornar token JWT")
    void deveAutenticarComSucesso() throws Exception {
        RegistroUsuarioRequest registro = RegistroUsuarioRequest.builder()
                .nome("Ana Souza")
                .email("ana@teste.com")
                .senha("senhaSegura456")
                .build();

        mockMvc.perform(post("/api/auth/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registro)))
                .andExpect(status().isCreated());

        LoginRequest login = LoginRequest.builder()
                .email("ana@teste.com")
                .senha("senhaSegura456")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true))
                .andExpect(jsonPath("$.dados.token").isNotEmpty())
                .andExpect(jsonPath("$.dados.usuario.email").value("ana@teste.com"));
    }

    @Test
    @DisplayName("Deve rejeitar login com senha incorreta")
    void deveRejeitarLoginComSenhaIncorreta() throws Exception {
        RegistroUsuarioRequest registro = RegistroUsuarioRequest.builder()
                .nome("Ana Souza")
                .email("ana@teste.com")
                .senha("senhaSegura456")
                .build();

        mockMvc.perform(post("/api/auth/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registro)))
                .andExpect(status().isCreated());

        LoginRequest login = LoginRequest.builder()
                .email("ana@teste.com")
                .senha("senhaErrada")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.sucesso").value(false))
                .andExpect(jsonPath("$.mensagem").value("E-mail ou senha inválidos."));
    }
}
