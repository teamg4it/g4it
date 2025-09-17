/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiuser.mapper;

import com.soprasteria.g4it.backend.apiuser.model.UserBO;
import com.soprasteria.g4it.backend.apiuser.model.UserInfoBO;
import com.soprasteria.g4it.backend.apiuser.model.UserSearchBO;
import com.soprasteria.g4it.backend.apiuser.modeldb.User;
import com.soprasteria.g4it.backend.server.gen.api.dto.UserInfoRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.UserRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.UserSearchRest;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * UserRest Mapper.
 */
@Mapper(componentModel = "spring", uses = OrganizationRestMapper.class)
public interface UserRestMapper {

    /**
     * Map a business object to dto object.
     *
     * @param businessObject the source.
     * @return the UserRest.
     */
    UserRest toDto(final UserBO businessObject);

    /**
     * Map a dto object to business object.
     *
     * @param source the User entity.
     * @return the UserRest.
     */
    UserBO toBusinessObject(final User source);

    List<UserInfoRest> toListRest(final List<UserInfoBO> businessObj);

    List<UserSearchRest> toRestObj(final List<UserSearchBO> businessObj);

}

