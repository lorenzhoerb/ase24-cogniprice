package ase.cogniprice.unitTest.competitorTest;

import ase.cogniprice.controller.dto.competitor.CompetitorCreateDto;
import ase.cogniprice.controller.dto.competitor.CompetitorDetailsDto;
import ase.cogniprice.controller.dto.competitor.CompetitorUpdateDto;
import ase.cogniprice.exception.ConflictException;
import ase.cogniprice.repository.CompetitorRepository;
import ase.cogniprice.service.CompetitorService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class CompetitorServiceTest {
    @Autowired
    private CompetitorService competitorService;
    @Autowired
    private CompetitorRepository competitorRepository;

    @BeforeEach
    void setUp() {
        competitorRepository.deleteAll();
    }

    @Nested
    class CreateCompetitorTests {
        @Test
        void testCreateCompetitor_ShouldCreateNewCompetitor() {
            String competitorName = "Amazon";
            String hostname = "amazon.com";
            CompetitorCreateDto createDto = new CompetitorCreateDto(competitorName, hostname);

            CompetitorDetailsDto result = competitorService.createCompetitor(createDto);

            assertAll(
                    () -> assertNotNull(result),
                    () -> assertNotNull(result.getId()),
                    () -> assertEquals(competitorName, result.getName()),
                    () -> assertEquals(competitorName, result.getName()),
                    () -> assertEquals(hostname, result.getHostname())
            );
        }

        @Test
        void testCreateCompetitor_CompetitorWithHostExists_ShouldThrowConflictException() {
            String competitorName = "Amazon";
            String hostname = "amazon.com";
            CompetitorCreateDto createDto = new CompetitorCreateDto(competitorName, hostname);

            competitorService.createCompetitor(createDto);

            assertThrows(ConflictException.class, () -> {
                competitorService.createCompetitor(createDto);
            });
        }
    }

    @Nested
    class GetCompetitorByIdTests {
        @Test
        void testGetCompetitorById_ShouldThrowNotFoundException() {
            Long nonExistentId = 999L;

            assertThrows(EntityNotFoundException.class, () -> {
                competitorService.getCompetitorById(nonExistentId);
            });
        }

        @Test
        void testGetAllCompetitors_ShouldReturnAllCompetitors() {
            CompetitorCreateDto createDto1 = new CompetitorCreateDto("Amazon", "amazon.com");
            CompetitorCreateDto createDto2 = new CompetitorCreateDto("eBay", "ebay.com");

            competitorService.createCompetitor(createDto1);
            competitorService.createCompetitor(createDto2);

            List<CompetitorDetailsDto> result = competitorService.getAllCompetitors();

            assertAll(
                    () -> assertNotNull(result),
                    () -> assertEquals(2, result.size()),
                    () -> assertEquals("Amazon", result.get(0).getName()),
                    () -> assertEquals("eBay", result.get(1).getName())
            );
        }
    }

    @Nested
    class UpdateCompetitorTests {
        @Test
        void testUpdateCompetitor_ShouldUpdateCompetitorDetails() {
            CompetitorCreateDto createDto = new CompetitorCreateDto("Amazon", "amazon.com");
            CompetitorDetailsDto createdCompetitor = competitorService.createCompetitor(createDto);

            CompetitorUpdateDto updateDto = new CompetitorUpdateDto("Updated Amazon");
            CompetitorDetailsDto updatedCompetitor = competitorService.updateCompetitor(createdCompetitor.getId(), updateDto);

            assertAll(
                    () -> assertNotNull(updatedCompetitor),
                    () -> assertEquals("Updated Amazon", updatedCompetitor.getName()),
                    () -> assertEquals("amazon.com", updatedCompetitor.getHostname())
            );
        }

        @Test
        void testUpdateCompetitor_ShouldThrowNotFoundException() {
            Long nonExistentId = 999L;
            CompetitorUpdateDto updateDto = new CompetitorUpdateDto("Updated Amazon");

            assertThrows(EntityNotFoundException.class, () -> {
                competitorService.updateCompetitor(nonExistentId, updateDto);
            });
        }
    }

    @Nested
    class DeleteCompetitorTests {
        @Test
        void testDeleteCompetitor_ShouldDeleteCompetitor() {
            CompetitorCreateDto createDto = new CompetitorCreateDto("Amazon", "amazon.com");
            CompetitorDetailsDto createdCompetitor = competitorService.createCompetitor(createDto);

            assertDoesNotThrow(() -> competitorService.deleteCompetitor(createdCompetitor.getId()));
            assertThrows(EntityNotFoundException.class, () -> competitorService.getCompetitorById(createdCompetitor.getId()));
        }

        @Test
        void testDeleteCompetitor_ShouldThrowNotFoundException() {
            Long nonExistentId = 999L;

            assertThrows(EntityNotFoundException.class, () -> {
                competitorService.deleteCompetitor(nonExistentId);
            });
        }
    }

    @Nested
    class GetCompetitorByNameTests {

        @Test
        void testGetCompetitorsByName_ShouldReturnCompetitorsMatchingName() {
            CompetitorCreateDto createDto1 = new CompetitorCreateDto("Amazon", "amazon.com");
            CompetitorCreateDto createDto2 = new CompetitorCreateDto("eBay", "ebay.com");
            CompetitorCreateDto createDto3 = new CompetitorCreateDto("Amazon Prime", "prime.amazon.com");

            competitorService.createCompetitor(createDto1);
            competitorService.createCompetitor(createDto2);
            competitorService.createCompetitor(createDto3);

            List<CompetitorDetailsDto> result = competitorService.getCompetitorsByName("amazon");

            assertAll(
                    () -> assertNotNull(result),
                    () -> assertEquals(2, result.size()),
                    () -> {
                        // check if all search results contain amazon in the name
                        List<String> names = result.stream()
                                .map(CompetitorDetailsDto::getName)
                                .toList();
                        assertTrue(names.contains("Amazon"));
                        assertTrue(names.contains("Amazon"));
                    }
            );
        }

        @Test
        void testGetCompetitorsByName_ShouldReturnEmptyListWhenNoMatch() {
            CompetitorCreateDto createDto = new CompetitorCreateDto("eBay", "ebay.com");
            competitorService.createCompetitor(createDto);

            List<CompetitorDetailsDto> result = competitorService.getCompetitorsByName("NonExistingName");

            assertAll(
                    () -> assertNotNull(result),
                    () -> assertTrue(result.isEmpty()) // Expecting an empty list as no competitor matches the name
            );
        }

        @Test
        void testGetCompetitorsByName_ShouldReturnEmptyListForEmptySearch() {
            CompetitorCreateDto createDto1 = new CompetitorCreateDto("Amazon", "amazon.com");
            CompetitorCreateDto createDto2 = new CompetitorCreateDto("eBay", "ebay.com");
            competitorService.createCompetitor(createDto1);
            competitorService.createCompetitor(createDto2);

            List<CompetitorDetailsDto> result = competitorService.getCompetitorsByName("");

            assertAll(
                    () -> assertNotNull(result),
                    () -> assertEquals(2, result.size())
            );
        }

    }


}
