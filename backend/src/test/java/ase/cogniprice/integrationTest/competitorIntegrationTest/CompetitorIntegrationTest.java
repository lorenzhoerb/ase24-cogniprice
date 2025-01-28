package ase.cogniprice.integrationTest.competitorIntegrationTest;

import ase.cogniprice.controller.dto.competitor.CompetitorCreateDto;
import ase.cogniprice.controller.dto.competitor.CompetitorUpdateDto;
import ase.cogniprice.entity.Competitor;
import ase.cogniprice.repository.CompetitorRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class CompetitorIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CompetitorRepository competitorRepository;

    private Competitor existingCompetitor;

    @BeforeEach
    void setUp() {
        competitorRepository.deleteAll();
        existingCompetitor = new Competitor();
        existingCompetitor.setName("Existing Competitor");
        existingCompetitor.setUrl("existing-competitor.com");
        competitorRepository.save(existingCompetitor);
    }

    @Test
    @WithMockUser(roles = "USER")
    void testCreateCompetitor_expectCreated() throws Exception {
        CompetitorCreateDto createDto = new CompetitorCreateDto();
        createDto.setName("New Competitor");
        createDto.setHostname("new-competitor.com");

        mockMvc.perform(post("/api/competitors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("New Competitor"))
                .andExpect(jsonPath("$.hostname").value("new-competitor.com"));
    }


    @Test
    @WithMockUser(roles = "USER")
    void testCreateCompetitor_WithDuplicateHostname_expectConflict() throws Exception {
        CompetitorCreateDto createDto = new CompetitorCreateDto();
        createDto.setName("Duplicate Hostname Competitor");
        createDto.setHostname(existingCompetitor.getUrl());

        mockMvc.perform(post("/api/competitors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "USER")
    void testGetCompetitorById_expectOk() throws Exception {
        mockMvc.perform(get("/api/competitors/{id}", existingCompetitor.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(existingCompetitor.getName()))
                .andExpect(jsonPath("$.hostname").value(existingCompetitor.getUrl()));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testGetCompetitorById_expectNotFound() throws Exception {
        mockMvc.perform(get("/api/competitors/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER")
    void testUpdateCompetitor_expectOk() throws Exception {
        CompetitorUpdateDto updateDto = new CompetitorUpdateDto();
        updateDto.setName("Updated Competitor Name");

        mockMvc.perform(put("/api/competitors/{id}", existingCompetitor.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Competitor Name"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testUpdateCompetitor_expectNotFound() throws Exception {
        CompetitorUpdateDto updateDto = new CompetitorUpdateDto();
        updateDto.setName("Non-existent Competitor");

        mockMvc.perform(put("/api/competitors/{id}", Long.MAX_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER")
    void testDeleteCompetitor_expectOk() throws Exception {
        mockMvc.perform(delete("/api/competitors/{id}", existingCompetitor.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "USER")
    void testDeleteCompetitorNotFound_expectNotFound() throws Exception {
        mockMvc.perform(delete("/api/competitors/{id}", 99L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER")
    void testCreateCompetitor_InvalidName_expectBadRequest() throws Exception {
        CompetitorCreateDto createDto = new CompetitorCreateDto();
        createDto.setName(""); // Invalid: Name is too short
        createDto.setHostname("valid-hostname.com");

        mockMvc.perform(post("/api/competitors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @WithMockUser(roles = "USER")
    void testCreateCompetitor_InvalidHostname_expectBadRequest() throws Exception {
        CompetitorCreateDto createDto = new CompetitorCreateDto();
        createDto.setName("Valid Name");
        createDto.setHostname("invalid-hostname@"); // Invalid: Doesn't match regex

        mockMvc.perform(post("/api/competitors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @WithMockUser(roles = "USER")
    void testCreateCompetitor_NullFields_expectBadRequest() throws Exception {
        CompetitorCreateDto createDto = new CompetitorCreateDto();
        // Both fields are null

        mockMvc.perform(post("/api/competitors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @WithMockUser(roles = "USER")
    void testUpdateCompetitor_InvalidName_expectBadRequest() throws Exception {
        CompetitorUpdateDto updateDto = new CompetitorUpdateDto();
        updateDto.setName(""); // Invalid: Name is too short

        mockMvc.perform(put("/api/competitors/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isUnprocessableEntity());

    }

    @Test
    @WithMockUser(roles = "USER")
    void testUpdateCompetitor_NullName_expectBadRequest() throws Exception {
        CompetitorUpdateDto updateDto = new CompetitorUpdateDto();
        updateDto.setName(null); // Invalid: Name is null

        mockMvc.perform(put("/api/competitors/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)));
    }

}
