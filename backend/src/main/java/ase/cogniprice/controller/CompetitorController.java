package ase.cogniprice.controller;

import ase.cogniprice.controller.dto.competitor.CompetitorCreateDto;
import ase.cogniprice.controller.dto.competitor.CompetitorDetailsDto;
import ase.cogniprice.controller.dto.competitor.CompetitorUpdateDto;
import ase.cogniprice.service.CompetitorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/competitors")
public class CompetitorController {

    private final CompetitorService competitorService;

    public CompetitorController(CompetitorService competitorService) {
        this.competitorService = competitorService;
    }


    @Operation(summary = "Create a new competitor", description = "Create a competitor by providing necessary details. The hostname must be unique.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Competitor created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "409", description = "Competitor with hostname already existing")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompetitorDetailsDto createCompetitor(@RequestBody @Valid CompetitorCreateDto competitorCreateDto) {
        log.info("createCompetitor({})", competitorCreateDto);
        return competitorService.createCompetitor(competitorCreateDto);
    }


    @Operation(summary = "Get competitor details by ID", description = "Retrieve competitor details using the unique ID.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Competitor details retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Competitor not found")
    })
    @GetMapping("/{id}")
    public CompetitorDetailsDto getCompetitorById(@PathVariable Long id) {
        log.info("getCompetitorById({})", id);
        return competitorService.getCompetitorById(id);
    }

    @Operation(summary = "Get all competitors", description = "Retrieve a list of all competitors, optionally filtered by name.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List of competitors retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid search input")
    })
    @GetMapping
    public List<CompetitorDetailsDto> getAllCompetitors(@RequestParam(required = false) String name) {
        log.info("getAllCompetitors({})", name);
        return name == null || name.isBlank()
                ? competitorService.getAllCompetitors()
                : competitorService.getCompetitorsByName(name);
    }

    @Operation(summary = "Update an existing competitor", description = "Update the details of an existing competitor.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Competitor updated successfully"),
        @ApiResponse(responseCode = "404", description = "Competitor not found"),
        @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PutMapping("/{id}")
    public CompetitorDetailsDto updateCompetitor(@PathVariable Long id, @RequestBody @Valid CompetitorUpdateDto competitorUpdateDto) {
        log.info("updateCompetitor({})", id);
        return competitorService.updateCompetitor(id, competitorUpdateDto);
    }

    @Operation(summary = "Delete a competitor", description = "Delete a competitor by its ID.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Competitor deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Competitor not found")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCompetitor(@PathVariable Long id) {
        log.info("deleteCompetitor({})", id);
        competitorService.deleteCompetitor(id);
    }
}
