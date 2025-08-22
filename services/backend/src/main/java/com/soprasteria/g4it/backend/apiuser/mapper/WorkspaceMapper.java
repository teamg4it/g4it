/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiuser.mapper;

import com.soprasteria.g4it.backend.apidigitalservice.mapper.DateMapper;
import com.soprasteria.g4it.backend.apiuser.model.OrganizationBO;
import com.soprasteria.g4it.backend.apiuser.modeldb.Workspace;
import com.soprasteria.g4it.backend.apiuser.modeldb.Subscriber;
import com.soprasteria.g4it.backend.apiuser.modeldb.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Organization Mapper.
 */
@Mapper(componentModel = "spring", uses = {DateMapper.class})
public interface WorkspaceMapper {

    /**
     * Map an entity to business object.
     *
     * @param workspace the Organization
     * @return the Organization.
     */
    OrganizationBO toBusinessObject(final Workspace workspace);


    /**
     * Map object to entity.
     *
     * @param name       the organization name.
     * @param subscriber the subscriber.
     * @param user       the user.
     * @param status     the status.
     * @return the organization entity.
     */
    @Mapping(target = "subscriber", source = "subscriber")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "lastUpdateDate", ignore = true)
    @Mapping(target = "lastUpdatedBy", source = "user")
    @Mapping(target = "createdBy", source = "user")
    Workspace toEntity(final String name, final Subscriber subscriber, final User user, final String status);

}
