package com.soprasteria.g4it.backend.apirecomandation.repository;
import com.soprasteria.g4it.backend.apiinout.modeldb.InDatacenter;
import com.soprasteria.g4it.backend.apirecomandation.modeldb.OutAiReco;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OutAiRecoRepository extends JpaRepository<OutAiReco, Long> {

    List<OutAiReco> findByDigitalServiceUid(String digitalServiceUid);

}


