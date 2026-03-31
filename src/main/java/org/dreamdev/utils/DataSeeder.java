package org.dreamdev.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dreamdev.models.Electorate;
import org.dreamdev.models.Permission;
import org.dreamdev.models.CitizenshipType;
import org.dreamdev.repositories.ElectorateRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;


import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final ElectorateRepository electorateRepository;

    @Override
    public void run(String... args) throws Exception {
        if (electorateRepository.count() == 0) {

            Electorate electorate = getElectorate();

            electorateRepository.save(electorate);

            log.info("Seeded default electorate with upload & vote permissions.");
        } else {
            log.warn(" Electorates already exist, skipping seeding.");
        }
    }

    private static Electorate getElectorate() {
        return Electorate.builder()
                .electorateId("ELECTORATE-001")
                .firstName("Samuel")
                .lastName("Joseph")
                .dateOfBirth("1990-05-15")
                .citizenship(CitizenshipType.NATURALIZATION)
                .permissions(List.of(
                        Permission.CAN_UPLOAD_FILE,
                        Permission.CAN_APPROVE_VOTER,
                        Permission.CAN_VIEW_ELECTORATE

                ))
                .build();
    }
}