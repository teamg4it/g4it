/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apidigitalservice.mapper;

import com.soprasteria.g4it.backend.apidigitalservice.business.DigitalServiceReferentialService;
import com.soprasteria.g4it.backend.apidigitalservice.model.DigitalServiceVersionBO;
import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalService;
import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalServiceVersion;
import com.soprasteria.g4it.backend.apiuser.modeldb.User;
import com.soprasteria.g4it.backend.common.dbmodel.Note;
import com.soprasteria.g4it.backend.common.task.mapper.TaskBOMapper;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * DigitalServiceVersion Mapper.
 */
@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true),
        collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED
)
@Slf4j
public abstract class DigitalServiceVersionMapper {

    @Autowired
    private NoteMapper noteMapper;

    @Autowired
    private TaskBOMapper taskBOMapper;


    public abstract DigitalServiceVersionBO toBusinessObject(final DigitalServiceVersion entity);

    /**
     * Map to Business Object list.
     *
     * @param source the source list.
     * @return the business object list.
     */
    public abstract List<DigitalServiceVersionBO> toBusinessObject(final List<DigitalServiceVersion> source);

    /**
     * Map to Business Object.
     *
     * @param entity the source.
     * @return the DigitalServiceBO.
     */
    @Named("fullMapping")
    public DigitalServiceVersionBO toFullBusinessObject(final DigitalServiceVersion entity) {
        if (entity == null) return null;

        DigitalService digitalService = entity.getDigitalService();

        DigitalServiceVersionBO boBuilder = DigitalServiceVersionBO.builder()
                .uid(entity.getUid())
                .description(entity.getDescription())
                .versionType(entity.getVersionType())
                .creationDate(entity.getCreationDate())
                .lastUpdateDate(entity.getLastUpdateDate())
                .lastCalculationDate(entity.getLastCalculationDate())
                .criteria(entity.getCriteria())
                .note(noteMapper.toBusinessObject(entity.getNote()))
                .tasks(taskBOMapper.toBOList(entity.getTasks())) // map Tasks → TaskBO
                .isShared(false)
                .name(digitalService.getName())
                .isAi(digitalService.isAi())
                .enableDataInconsistency(digitalService.isEnableDataInconsistency())
                .itemId(digitalService.getUid()).build();


        return boBuilder;
    }


    /**
     * Map DigitalServiceVersion and DigitalService to DigitalServiceVersionBO
     */
    public DigitalServiceVersionBO toBusinessObject(final DigitalServiceVersion version,
                                                    final DigitalService digitalService) {
        DigitalServiceVersionBO bo = toBusinessObject(version);

        // Add DigitalService fields
        bo.setUid(version.getUid());
        bo.setName(digitalService.getName());
        bo.setIsAi(digitalService.isAi());
        bo.setEnableDataInconsistency(digitalService.isEnableDataInconsistency());
        bo.setIsShared(false); // Default value, can be set later

        return bo;
    }

    /**
     * Merge two entities to update.
     *
     * @param target                           the digital service to update.
     * @param source                           the digital service from frontend.
     * @param digitalServiceReferentialService the service to retrieve referential data.
     */
    public void mergeEntity(@MappingTarget final DigitalServiceVersion target, final DigitalServiceVersionBO source,
                            @Context final DigitalServiceReferentialService digitalServiceReferentialService, @Context final User user) {
        if (source == null) {
            return;
        }


        target.setDescription(source.getDescription());
        target.getDigitalService().setEnableDataInconsistency(source.getEnableDataInconsistency());
        target.getDigitalService().setName(source.getName());
        target.setVersionType(source.getVersionType());
        List<String> sourceCriteria = source.getCriteria();
        List<String> targetCriteria = target.getCriteria();
        if (!Objects.equals(sourceCriteria, targetCriteria)) {
            //set criteria
            target.setCriteria(sourceCriteria);
            target.setLastUpdateDate(LocalDateTime.now());
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
