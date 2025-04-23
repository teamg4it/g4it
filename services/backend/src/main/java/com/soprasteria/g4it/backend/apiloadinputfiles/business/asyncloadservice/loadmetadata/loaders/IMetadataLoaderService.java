/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.loadmetadata.loaders;

import com.soprasteria.g4it.backend.common.model.Context;
import com.soprasteria.g4it.backend.common.model.FileToLoad;
import com.soprasteria.g4it.backend.server.gen.api.dto.InVirtualEquipmentRest;

import java.util.List;

public interface IMetadataLoaderService {

    public void execute(Context context, FileToLoad fileToLoad, int pageNumber, List<Object> objects);

}
