/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apidigitalservice.mapper;

import com.soprasteria.g4it.backend.apidigitalservice.business.DigitalServiceReferentialService;
import com.soprasteria.g4it.backend.apidigitalservice.model.DigitalServiceBO;
import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalService;
import com.soprasteria.g4it.backend.apiuser.modeldb.User;
import com.soprasteria.g4it.backend.common.dbmodel.Note;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * DigitalService Mapper.
 */
@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true),
        collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED
)
@Slf4j
public abstract class DigitalServiceMapper {

    @Autowired
    private NoteMapper noteMapper;

    public abstract DigitalServiceBO toBusinessObject(final DigitalService entity);

    /**
     * Map to Business Object list.
     *
     * @param source the source list.
     * @return the business object list.
     */
    public abstract List<DigitalServiceBO> toBusinessObject(final List<DigitalService> source);

    /**
     * Map to Business Object.
     *
     * @param entity the source.
     * @return the DigitalServiceBO.
     */
    @Named("fullMapping")
    @Mapping(target = "isAi", source = "ai")
    public abstract DigitalServiceBO toFullBusinessObject(final DigitalService entity);

    /**
     * Map to Business Object list.
     *
     * @param source the source list.
     * @return the business object list.
     */
    @IterableMapping(qualifiedByName = "fullMapping")
    public abstract List<DigitalServiceBO> toFullBusinessObject(final List<DigitalService> source);

    /**
     * Map the business object in entity object.
     *
     * @param source                           the business object.
     * @param digitalServiceReferentialService service to retrieve referential data.
     * @return the digital service entity.
     */
    public abstract DigitalService toEntity(final DigitalServiceBO source, @Context final DigitalServiceReferentialService digitalServiceReferentialService);

    /**
     * Merge two entities to update.
     *
     * @param target                           the digital service to update.
     * @param source                           the digital service from frontend.
     * @param digitalServiceReferentialService the service to retrieve referential data.
     */
    public void mergeEntity(@MappingTarget final DigitalService target, final DigitalServiceBO source,
                            @Context final DigitalServiceReferentialService digitalServiceReferentialService, @Context final User user) {
        if (source == null) {
            return;
        }

        target.setName(source.getName());
        target.setLastUpdateDate(LocalDateTime.now());
        target.setEnableDataInconsistency(source.getEnableDataInconsistency());

        List<String> sourceCriteria = source.getCriteria();
        List<String> targetCriteria = target.getCriteria();
        if (!Objects.equals(sourceCriteria, targetCriteria)) {
            //set criteria
            target.setCriteria(sourceCriteria);
        }

        // Merge note
        Note note = noteMapper.toEntity(source.getNote());
        if (note != null) {
            if (target.getNote() == null) {
                //create note
                note.setCreatedBy(user);
            } else if (note.getContent().equals(target.getNote().getContent())) {
                //nothing to update in the note
                return;
            }
            note.setLastUpdatedBy(user);
        }
        // update/delete note
        target.setNote(note);
    }

}
