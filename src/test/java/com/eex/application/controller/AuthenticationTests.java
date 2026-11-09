package com.eex.application.controller;

import com.eex.domain.entity.Account;
import com.eex.domain.entity.Client;
import com.eex.domain.entity.User;
import com.eex.domain.service.AccountService;
import com.eex.domain.service.ClientService;
import com.eex.domain.service.UserService;
import com.eex.domain.util.JwtUtil;
import com.eex.domain.util.PasswordUtil;
import com.eex.infrastructure.config.SecurityConfig;
import com.eex.infrastructure.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({AuthController.class, UserController.class, AccountController.class, ClientController.class})
@Import(SecurityConfig.class)
class AuthenticationTests {
    private static final String VALID_TOKEN = "valid-" + "test-token";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private AccountService accountService;

    @MockBean
    private ClientService clientService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private UserRepository userRepository;

    @Test
    void loginWithValidCredentialsReturnsToken() throws Exception {
        User user = new User(1, "Alice", PasswordUtil.hashPassword("aliceSecret"), 1);
        when(userService.getUsersByUsername("Alice")).thenReturn(List.of(user));
        when(jwtUtil.generateToken(1)).thenReturn("test-token");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"Alice","password":"aliceSecret"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("test-token"));
    }

    @Test
    void loginWithInvalidCredentialsReturnsUnauthorized() throws Exception {
        User user = new User(1, "Alice", PasswordUtil.hashPassword("aliceSecret"), 1);
        when(userService.getUsersByUsername("Alice")).thenReturn(List.of(user));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"Alice","password":"wrong-password"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Identifiants invalides"))
                .andExpect(jsonPath("$.token").doesNotExist());
    }

    @Test
    void loginWithUnknownUsernameReturnsSameGenericError() throws Exception {
        when(userService.getUsersByUsername("Unknown")).thenReturn(List.of());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"Unknown","password":"wrong-password"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Identifiants invalides"))
                .andExpect(jsonPath("$.token").doesNotExist());
    }

    @Test
    void loginWithBlankCredentialsIsRejected() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":" ","password":""}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void loginWithPasswordExceedingBcryptByteLimitIsRejected() throws Exception {
        String multibytePassword = "é".repeat(37);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"Alice\",\"password\":\"" + multibytePassword + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Requête invalide"));
    }

    @Test
    void protectedRouteWithoutTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void validateWithoutTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/auth/validate"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Jeton manquant ou invalide"));
    }

    @Test
    void validateInvalidOrExpiredTokenReturnsUnauthorized() throws Exception {
        when(jwtUtil.validateAndGetUserId(VALID_TOKEN)).thenReturn(null);

        mockMvc.perform(post("/api/auth/validate")
                        .header(AUTHORIZATION_HEADER, BEARER_PREFIX + VALID_TOKEN))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Jeton invalide ou expiré"));
    }

    @Test
    void validateTokenForDeletedUserReturnsUnauthorized() throws Exception {
        when(jwtUtil.validateAndGetUserId(VALID_TOKEN)).thenReturn(42);
        when(userService.getUserById(42)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/auth/validate")
                        .header(AUTHORIZATION_HEADER, BEARER_PREFIX + VALID_TOKEN))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Jeton invalide ou expiré"));
    }

    @Test
    void usersRouteReturnsOnlyUsersFromAuthenticatedClientWithoutPasswords() throws Exception {
        User authenticatedUser = new User(1, "Alice", "hashed-password", 1);
        when(jwtUtil.validateAndGetUserId(VALID_TOKEN)).thenReturn(1);
        when(userRepository.findById(1)).thenReturn(Optional.of(authenticatedUser));
        when(userService.getUsersByClientId(1)).thenReturn(List.of(authenticatedUser));

        mockMvc.perform(get("/api/users")
                        .header(AUTHORIZATION_HEADER, BEARER_PREFIX + VALID_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("Alice"))
                .andExpect(jsonPath("$[0].password").doesNotExist());

        verify(userService).getUsersByClientId(1);
    }

    @Test
    void usersClientRouteAllowsAuthenticatedClient() throws Exception {
        User authenticatedUser = new User(1, "Alice", "hashed-password", 1);
        when(jwtUtil.validateAndGetUserId(VALID_TOKEN)).thenReturn(1);
        when(userRepository.findById(1)).thenReturn(Optional.of(authenticatedUser));
        when(userService.getUsersByClientId(1)).thenReturn(List.of(authenticatedUser));

        mockMvc.perform(get("/api/users/client/1")
                        .header(AUTHORIZATION_HEADER, BEARER_PREFIX + VALID_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("Alice"))
                .andExpect(jsonPath("$[0].password").doesNotExist());

        verify(userService).getUsersByClientId(1);
    }

    @Test
    void usersClientRouteRejectsAnotherClientWithoutQueryingItsUsers() throws Exception {
        User authenticatedUser = new User(1, "Alice", "hashed-password", 1);
        when(jwtUtil.validateAndGetUserId(VALID_TOKEN)).thenReturn(1);
        when(userRepository.findById(1)).thenReturn(Optional.of(authenticatedUser));

        mockMvc.perform(get("/api/users/client/2")
                        .header(AUTHORIZATION_HEADER, BEARER_PREFIX + VALID_TOKEN))
                .andExpect(status().isNotFound());

        verify(userService, never()).getUsersByClientId(2);
    }

    @Test
    void userFromAnotherClientIsNotFound() throws Exception {
        User authenticatedUser = new User(1, "Alice", "hashed-password", 1);
        User otherClientUser = new User(2, "Bob", "another-hash", 2);
        when(jwtUtil.validateAndGetUserId(VALID_TOKEN)).thenReturn(1);
        when(userRepository.findById(1)).thenReturn(Optional.of(authenticatedUser));
        when(userService.getUserById(2)).thenReturn(Optional.of(otherClientUser));

        mockMvc.perform(get("/api/users/2")
                        .header(AUTHORIZATION_HEADER, BEARER_PREFIX + VALID_TOKEN))
                .andExpect(status().isNotFound());
    }

    @Test
    void clientRouteReturnsAuthenticatedUsersClient() throws Exception {
        User authenticatedUser = new User(1, "Alice", "hashed-password", 1);
        when(jwtUtil.validateAndGetUserId(VALID_TOKEN)).thenReturn(1);
        when(userRepository.findById(1)).thenReturn(Optional.of(authenticatedUser));
        when(clientService.getClientById(1)).thenReturn(Optional.of(new Client(1, true)));

        mockMvc.perform(get("/api/clients/me")
                        .header(AUTHORIZATION_HEADER, BEARER_PREFIX + VALID_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void clientFromAnotherTenantIsNotFound() throws Exception {
        User authenticatedUser = new User(1, "Alice", "hashed-password", 1);
        when(jwtUtil.validateAndGetUserId(VALID_TOKEN)).thenReturn(1);
        when(userRepository.findById(1)).thenReturn(Optional.of(authenticatedUser));

        mockMvc.perform(get("/api/clients/2")
                        .header(AUTHORIZATION_HEADER, BEARER_PREFIX + VALID_TOKEN))
                .andExpect(status().isNotFound());
    }

    @Test
    void accountRouteReturnsOnlyAuthenticatedUsersAccounts() throws Exception {
        User authenticatedUser = new User(1, "Alice", "hashed-password", 1);
        when(jwtUtil.validateAndGetUserId(VALID_TOKEN)).thenReturn(1);
        when(userRepository.findById(1)).thenReturn(Optional.of(authenticatedUser));
        when(accountService.getAccountsByClientId(1))
                .thenReturn(List.of(new Account(10, "Savings", 100.0, 1, null)));

        mockMvc.perform(get("/api/accounts")
                        .header(AUTHORIZATION_HEADER, BEARER_PREFIX + VALID_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].clientId").value(1));

        verify(accountService).getAccountsByClientId(1);
    }

    @Test
    void accountsClientRouteAllowsAuthenticatedClient() throws Exception {
        User authenticatedUser = new User(1, "Alice", "hashed-password", 1);
        when(jwtUtil.validateAndGetUserId(VALID_TOKEN)).thenReturn(1);
        when(userRepository.findById(1)).thenReturn(Optional.of(authenticatedUser));
        when(accountService.getAccountsByClientId(1))
                .thenReturn(List.of(new Account(10, "Savings", 100.0, 1, null)));

        mockMvc.perform(get("/api/accounts/client/1")
                        .header(AUTHORIZATION_HEADER, BEARER_PREFIX + VALID_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].clientId").value(1));

        verify(accountService).getAccountsByClientId(1);
    }

    @Test
    void accountsClientRouteRejectsAnotherClientWithoutQueryingItsAccounts() throws Exception {
        User authenticatedUser = new User(1, "Alice", "hashed-password", 1);
        when(jwtUtil.validateAndGetUserId(VALID_TOKEN)).thenReturn(1);
        when(userRepository.findById(1)).thenReturn(Optional.of(authenticatedUser));

        mockMvc.perform(get("/api/accounts/client/2")
                        .header(AUTHORIZATION_HEADER, BEARER_PREFIX + VALID_TOKEN))
                .andExpect(status().isNotFound());

        verify(accountService, never()).getAccountsByClientId(2);
    }

    @Test
    void accountUpdateChangesOnlyTheNameOnAnAccountBelongingToAuthenticatedClient() throws Exception {
        User authenticatedUser = new User(1, "Alice", "hashed-password", 1);
        Account renamedAccount = new Account(10, "Renamed", 100.0, 1, null);
        when(jwtUtil.validateAndGetUserId(VALID_TOKEN)).thenReturn(1);
        when(userRepository.findById(1)).thenReturn(Optional.of(authenticatedUser));
        when(accountService.updateNameForClient(10, 1, "Renamed"))
                .thenReturn(Optional.of(renamedAccount));

        mockMvc.perform(put("/api/accounts/10")
                        .header(AUTHORIZATION_HEADER, BEARER_PREFIX + VALID_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Renamed","amount":999999,"clientId":2}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Renamed"))
                .andExpect(jsonPath("$.amount").value(100.0))
                .andExpect(jsonPath("$.clientId").value(1));

        verify(accountService).updateNameForClient(10, 1, "Renamed");
    }
}
