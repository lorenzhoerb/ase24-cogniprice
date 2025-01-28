package ase.cogniprice.service.implementation;

import ase.cogniprice.controller.dto.competitor.CompetitorCreateDto;
import ase.cogniprice.controller.dto.competitor.CompetitorDetailsDto;
import ase.cogniprice.controller.dto.competitor.CompetitorUpdateDto;
import ase.cogniprice.entity.Competitor;
import ase.cogniprice.exception.ConflictException;
import ase.cogniprice.repository.CompetitorRepository;
import ase.cogniprice.service.CompetitorService;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class CompetitorServiceImpl implements CompetitorService {

    private final CompetitorRepository competitorRepository;

    public CompetitorServiceImpl(CompetitorRepository competitorRepository) {
        this.competitorRepository = competitorRepository;
    }

    @Override
    public CompetitorDetailsDto createCompetitor(CompetitorCreateDto competitor) {
        log.trace("createCompetitor({})", competitor);
        if (competitor == null) {
            throw new IllegalArgumentException("CompetitorCreateDto is null");
        }

        // Check if competitor with the same hostname already exists
        competitorRepository.findCompetitorByUrl(competitor.getHostname())
                .ifPresent(existingCompetitor -> {
                    throw new ConflictException("Competitor with hostname " + competitor.getHostname() + " already exists");
                });

        Competitor createdCompetitor = competitorRepository.save(toCompetitorEntity(competitor));
        return toCompetitorDetailsDto(createdCompetitor);
    }


    @Override
    public CompetitorDetailsDto getCompetitorById(Long id) {
        log.trace("getCompetitorById({})", id);
        if (id == null) {
            throw new IllegalArgumentException("CompetitorId is null");
        }

        Competitor competitor = getCompetitorOrThrow(id);

        return toCompetitorDetailsDto(competitor);
    }

    @Override
    public List<CompetitorDetailsDto> getAllCompetitors() {
        log.trace("getAllCompetitors()");
        return competitorRepository.findAll()
                .stream()
                .map(this::toCompetitorDetailsDto)
                .toList();
    }

    @Override
    public List<CompetitorDetailsDto> getCompetitorsByName(String name) {
        log.trace("getCompetitorsByName({})", name);
        return competitorRepository.findByNameIgnoreCaseContaining(name)
                .stream()
                .map(this::toCompetitorDetailsDto)
                .toList();
    }

    @Override
    public CompetitorDetailsDto updateCompetitor(Long id, CompetitorUpdateDto competitor) {
        log.trace("updateCompetitor({})", id);
        Competitor oldCompetitor = getCompetitorOrThrow(id);

        if (competitor.getName() != null) {
            oldCompetitor.setName(competitor.getName());
        }

        return toCompetitorDetailsDto(competitorRepository.save(oldCompetitor));
    }


    @Override
    public void deleteCompetitor(Long id) {
        log.trace("deleteCompetitor({})", id);
        competitorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Competitor with id " + id + " not found"));
        competitorRepository.deleteById(id);
    }

    private Competitor getCompetitorOrThrow(Long id) {
        return competitorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Competitor with id " + id + " not found"));
    }

    private Competitor toCompetitorEntity(CompetitorCreateDto competitor) {
        return new Competitor(
                competitor.getName(),
                competitor.getHostname()
        );
    }

    private CompetitorDetailsDto toCompetitorDetailsDto(Competitor createdCompetitor) {
        return new CompetitorDetailsDto(
                createdCompetitor.getId(),
                createdCompetitor.getName(),
                createdCompetitor.getUrl()
        );
    }
}
