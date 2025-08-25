/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiuser.repository;

import com.soprasteria.g4it.backend.apiuser.modeldb.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {

    /**
     * Find Subscribers with authorizedDomains not null
     *
     * @return the subscribers with authorizedDomains.
     */
    List<Organization> findByAuthorizedDomainsNotNull();


    /**
     * Find Subscriber by name
     *
     * @return the subscriber.
     */
    Optional<Organization> findByName(String organizationName);

    /**
     * Find Subscribers for given domain name
     *
     * @return the subscribers with authorizedDomains.
     */
    List<Organization> findByAuthorizedDomainsContaining(String domainName);

}
