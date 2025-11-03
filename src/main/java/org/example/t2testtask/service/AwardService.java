package org.example.t2testtask.service;

import lombok.RequiredArgsConstructor;
import org.example.t2testtask.entity.Award;
import org.example.t2testtask.repository.AwardRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class AwardService {

    private final AwardRepository awardRepository;

    public Award save(Long id, String name, LocalDate receivedDate){
        Award award = new Award();
        award.setId(id);
        award.setName(name);
        award.setReceivedDate(receivedDate);
        return awardRepository.save(award);
    }
}
