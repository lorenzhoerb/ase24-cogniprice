package ase.cogniprice.unitTest.applicationUserTest.controllerTest;


import ase.cogniprice.controller.dto.application.user.ApplicationUserDetailsDto;
import ase.cogniprice.controller.dto.application.user.ApplicationUserEditDto;
import ase.cogniprice.controller.mapper.ApplicationUserMapper;
import ase.cogniprice.entity.ApplicationUser;
import ase.cogniprice.exception.NotFoundException;
import ase.cogniprice.repository.ApplicationUserRepository;
import ase.cogniprice.security.JwtTokenizer;
import ase.cogniprice.service.ApplicationUserService;
import ase.cogniprice.type.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.server.ResponseStatusException;


import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ApplicationUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ApplicationUserService applicationUserService;

    @MockBean
    private ApplicationUserMapper applicationUserMapper;

    @Autowired
    private JwtTokenizer jwtTokenizer;

    @Autowired
    private ApplicationUserRepository applicationUserRepository;

    private String jwt;

    private final ApplicationUserEditDto validApplicationUserEditDto = new ApplicationUserEditDto(
            "test@gmail.com",
            "testUsername",
            "firstname",
            "lastname"
    );

    private final ApplicationUserEditDto notExistingApplicationUserEditDto = new ApplicationUserEditDto(
            "newTestUsername@gmail.com",
            "newTestUsername",
            "firstname",
            "lastname"
    );

    private final ApplicationUserEditDto invalidApplicationUserEditDto = new ApplicationUserEditDto(
            "not a valid email",
            "n",
            "firstname not valid",
            "lastname"
    );

    @BeforeEach
    void setUp() {
        jwt = jwtTokenizer.getAuthToken(
                "username", 1L, List.of(Role.USER.name())
        );
    }

    @Test
    @WithMockUser(roles = "USER")
    void testGetDetailsSuccess() throws Exception {
        ApplicationUserDetailsDto userDetailsDto = new ApplicationUserDetailsDto(1L, "username", "test", "test", "test@test.com");
        when(applicationUserService.getApplicationUserById(1L)).thenReturn(new ApplicationUser());
        when(applicationUserMapper.applicationUserToApplicationUserDetailsDto(any())).thenReturn(userDetailsDto);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/users/details"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("username"))
                .andExpect(jsonPath("$.email").value("test@test.com"))
                .andExpect(jsonPath("$.firstName").value("test"))
                .andExpect(jsonPath("$.lastName").value("test"));
    }

    @Test
    void testGetDetailsInvalidToken() throws Exception {
        String invalidJwt = "Bearer invalid.token";

        mockMvc.perform(MockMvcRequestBuilders.get("/api/users/details")
                        .header("Authorization", invalidJwt))
                .andExpect(status().isUnauthorized())
                .andExpect(result -> assertTrue(result.getResponse().getContentAsString().contains("Invalid authorization header or token")));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testGetDetailsUserNotFound() throws Exception {
        when(applicationUserService.getApplicationUserByUsername(any()))
                .thenThrow(new NotFoundException("User not found"));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/users/details"))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ResponseStatusException))
                .andExpect(result -> assertTrue(result.getResolvedException().getMessage().contains("404 NOT_FOUND \"User not found\"")));
    }

    @Test
    @WithMockUser(username = "username", roles = "USER")
    void testGetUserByUsernameSuccess() throws Exception {
        ApplicationUserDetailsDto userDetailsDto = new ApplicationUserDetailsDto(1L, "username", "Test", "User", "testuser@test.com");
        when(applicationUserService.getApplicationUserByUsername("username")).thenReturn(new ApplicationUser());
        when(applicationUserMapper.applicationUserToApplicationUserDetailsDto(any())).thenReturn(userDetailsDto);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/users/username"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.username").value("username"))
                .andExpect(jsonPath("$.email").value("testuser@test.com"))
                .andExpect(jsonPath("$.firstName").value("Test"))
                .andExpect(jsonPath("$.lastName").value("User"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testGetUserByUsernameNotFound() throws Exception {
        when(applicationUserService.getApplicationUserByUsername("nonexistentuser"))
                .thenThrow(new NotFoundException("User with username nonexistentuser not found"));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/users/nonexistentuser"))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ResponseStatusException))
                .andExpect(result -> assertTrue(result.getResolvedException().getMessage().contains("404 NOT_FOUND \"User with username nonexistentuser not found\"")));
    }

    @Test
    void testGetUserByUsernameInvalidHeader() throws Exception {
        String invalidJwt = "Bearer invalid.token";

        mockMvc.perform(MockMvcRequestBuilders.get("/api/users/testuser")
                        .header("Authorization", invalidJwt))
                .andExpect(status().isUnauthorized())
                .andExpect(result -> assertTrue(result.getResponse().getContentAsString().contains("Invalid authorization header or token")));
    }


    /* Edit ApplicationUser */

    @Test
    @WithMockUser(roles = "USER")
    void testEditApplicationUserWithValidDataReturns200() throws Exception {
        ApplicationUser user = new ApplicationUser();
        user.setId(1L);
        user.setUsername("username");
        when(applicationUserService.getApplicationUserByUsername(any())).thenReturn(user);

        when(applicationUserService.editApplicationUser(validApplicationUserEditDto, 1L)).thenReturn(null);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/users/edit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(validApplicationUserEditDto)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void testEditApplicationUserNotFound() throws Exception {
        ApplicationUser user = new ApplicationUser();
        user.setId(1L);
        user.setUsername("username");
        when(applicationUserService.getApplicationUserByUsername(any())).thenReturn(user);

        doThrow(new NotFoundException("User not found"))
                .when(applicationUserService).editApplicationUser(notExistingApplicationUserEditDto, 1L);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/users/edit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(notExistingApplicationUserEditDto)))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResponse().getContentAsString().contains("User not found")));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testEditInvalidApplicationUserReturns422() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/api/users/edit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(invalidApplicationUserEditDto)))
                .andExpect(status().isUnprocessableEntity());
    }


    /* Delete ApplicationUser */
    @Test
    @WithMockUser(roles = "USER")
    void testDeleteApplicationUserSuccess() throws Exception {
        ApplicationUser user = new ApplicationUser();
        user.setId(1L);
        user.setUsername("username");
        when(applicationUserService.getApplicationUserByUsername(any())).thenReturn(user);

        doNothing().when(applicationUserService).deleteApplicationUser(1L);

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/users"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "USER")
    void testDeleteApplicationUserNotFound() throws Exception {
        ApplicationUser user = new ApplicationUser();
        user.setId(1L);
        user.setUsername("username");
        when(applicationUserService.getApplicationUserByUsername(any())).thenReturn(user);

        doThrow(new NotFoundException("Could not find user with id: 1"))
                .when(applicationUserService).deleteApplicationUser(1L);

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/users"))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ResponseStatusException))
                .andExpect(result -> assertTrue(result.getResolvedException().getMessage().contains("400 BAD_REQUEST \"Could not find user with id: 1\"")));
    }
}
