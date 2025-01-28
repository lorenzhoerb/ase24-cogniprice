package ase.cogniprice.service;

import ase.cogniprice.controller.dto.competitor.CompetitorCreateDto;
import ase.cogniprice.controller.dto.competitor.CompetitorDetailsDto;
import ase.cogniprice.controller.dto.competitor.CompetitorUpdateDto;
import ase.cogniprice.exception.ConflictException;
import jakarta.persistence.EntityNotFoundException;

import java.util.List;

/**
 * Service interface for managing competitors.
 * Provides CRUD operations and additional methods for retrieving competitors by name.
 *
 * @author Lorenz
 */
public interface CompetitorService {

    /**
     * Creates a new competitor.
     *
     * @param competitor the {@link CompetitorCreateDto} containing details of the competitor to create.
     * @return the created competitor as a {@link CompetitorDetailsDto}.
     * @throws ConflictException if a competitor with the same hostname already exists.
     */
    CompetitorDetailsDto createCompetitor(CompetitorCreateDto competitor);

    /**
     * Retrieves a competitor by its ID.
     *
     * @param id the unique identifier of the competitor.
     * @return the {@link CompetitorDetailsDto} containing the details of the competitor.
     * @throws EntityNotFoundException if the competitor with the given ID is not found.
     */
    CompetitorDetailsDto getCompetitorById(Long id);

    /**
     * Retrieves all competitors.
     *
     * @return a list of {@link CompetitorDetailsDto} representing all competitors.
     */
    List<CompetitorDetailsDto> getAllCompetitors();

    /**
     * Retrieves competitors filtered by name.
     *
     * @param name the name of the competitors to search for.
     * @return a list of {@link CompetitorDetailsDto} for competitors matching the specified name.
     */
    List<CompetitorDetailsDto> getCompetitorsByName(String name);

    /**
     * Updates the details of an existing competitor. Only the name is updatable.
     *
     * @param id         the unique identifier of the competitor to update.
     * @param competitor the {@link CompetitorCreateDto} containing the updated details.
     * @return the updated competitor as a {@link CompetitorDetailsDto}.
     * @throws EntityNotFoundException if the competitor with the given ID is not found.
     */
    CompetitorDetailsDto updateCompetitor(Long id, CompetitorUpdateDto competitor);

    /**
     * Deletes a competitor by its ID.
     *
     * @param id the unique identifier of the competitor to delete.
     * @throws EntityNotFoundException if the competitor with the given ID is not found.
     */
    void deleteCompetitor(Long id);
}
