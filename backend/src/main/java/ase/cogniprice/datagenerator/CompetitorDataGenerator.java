package ase.cogniprice.datagenerator;

import ase.cogniprice.entity.Competitor;
import ase.cogniprice.repository.CompetitorRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.List;

@Profile({"generateData", "generateDemoData"})
@Component
@DependsOn({"applicationUserDataGenerator"})
public class CompetitorDataGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final CompetitorRepository competitorRepository;
    private final Environment environment;

    @Autowired
    public CompetitorDataGenerator(CompetitorRepository competitorRepository, Environment environment) {
        this.competitorRepository = competitorRepository;
        this.environment = environment;
    }

    @PostConstruct
    private void generateCompetitors() {
        List<String> activeProfiles = Arrays.stream(environment.getActiveProfiles()).toList();
        List<Competitor> competitors;
        if (activeProfiles.contains("generateDemoData")) {
            competitors = Arrays.asList(
                createCompetitor("https://www.demo1.com", "DemoCompetitor1"),
                createCompetitor("https://www.demo2.com", "DemoCompetitor2"),
                createCompetitor("https://www.demo3.com", "DemoCompetitor3"),
                createCompetitor("https://www.demo4.com", "DemoCompetitor4"),
                createCompetitor("https://www.demo5.com", "DemoCompetitor5")
                );
        } else {
            competitors = Arrays.asList(
                createCompetitor("https://www.amazon.com", "Amazon"),
                createCompetitor("https://www.ebay.com", "eBay"),
                createCompetitor("https://www.walmart.com", "Walmart"),
                createCompetitor("https://www.target.com", "Target"),
                createCompetitor("https://www.bestbuy.com", "Best Buy"),
                createCompetitor("https://www.newegg.com", "Newegg"),
                createCompetitor("https://www.homedepot.com", "Home Depot"),
                createCompetitor("https://www.etsy.com", "Etsy"),
                createCompetitor("https://www.alibaba.com", "Alibaba"),
                createCompetitor("https://www.costco.com", "Costco"),
                createCompetitor("https://www.example.com", "Example")
            );
        }
        for (Competitor competitor : competitors) {
            if (competitorRepository.findCompetitorByUrl(competitor.getUrl()).isEmpty()) {
                competitorRepository.save(competitor);
                LOG.info("Created competitor: {} with URL: {}", competitor.getName(), competitor.getUrl());
            }
        }
    }

    private Competitor createCompetitor(String url, String name) {
        Competitor competitor = new Competitor();
        competitor.setUrl(url);
        competitor.setName(name);
        return competitor;
    }
}
