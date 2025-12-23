/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apisharedlink.controller;

import com.soprasteria.g4it.backend.apibusinesshours.business.BusinessHoursService;
import com.soprasteria.g4it.backend.apidigitalservice.business.DigitalServiceReferentialService;
import com.soprasteria.g4it.backend.apidigitalservice.business.DigitalServiceVersionService;
import com.soprasteria.g4it.backend.apidigitalservice.mapper.DigitalServiceReferentialRestMapper;
import com.soprasteria.g4it.backend.apidigitalservice.mapper.DigitalServiceRestMapper;
import com.soprasteria.g4it.backend.apidigitalservice.mapper.DigitalServiceVersionRestMapper;
import com.soprasteria.g4it.backend.apiinout.business.*;
import com.soprasteria.g4it.backend.apiversion.business.VersionService;
import com.soprasteria.g4it.backend.server.gen.api.SharedLinkDigitalServiceItemsApiDelegate;
import com.soprasteria.g4it.backend.server.gen.api.dto.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class SharedDigitalServiceProxyController implements SharedLinkDigitalServiceItemsApiDelegate {

    @Autowired
    private DigitalServiceRestMapper digitalServiceRestMapper;

    private InPhysicalEquipmentService inPhysicalEquipmentService;
    private InDatacenterService inDatacenterService;
    private InVirtualEquipmentService inVirtualEquipmentService;
    private DigitalServiceReferentialService digitalServiceReferentialService;
    private DigitalServiceReferentialRestMapper digitalServiceReferentialRestMapper;
    private DigitalServiceVersionService digitalServiceVersionService;
    private VersionService versionService;
    private DigitalServiceVersionRestMapper digitalServiceVersionRestMapper;
    private BusinessHoursService businessHoursService;
    /**
     * Service to access physical equipment output data.
     */
    private OutPhysicalEquipmentService outPhysicalEquipmentService;

    /**
     * Service to access virtual equipment output data.
     */
    private OutVirtualEquipmentService outVirtualEquipmentService;


    @Override
    public ResponseEntity<List<InDatacenterRest>> getSharedDigitalServiceInputsDatacentersRest(String digitalServiceUid,
                                                                                               String shareId) {
        return ResponseEntity.ok().body(inDatacenterService.getByDigitalServiceVersion(digitalServiceUid));
    }

    @Override
    public ResponseEntity<List<InPhysicalEquipmentRest>> getSharedDigitalServiceInputsPhysicalEquipmentsRest(String digitalServiceVersionUid,
                                                                                                             String shareId) {
        return ResponseEntity.ok().body(inPhysicalEquipmentService.getByDigitalServiceVersion(digitalServiceVersionUid));
    }

    @Override
    public ResponseEntity<List<InVirtualEquipmentRest>> getSharedDigitalServiceInputsVirtualEquipmentsRest(String digitalServiceVersionUid,
                                                                                                           String shareId) {
        return ResponseEntity.ok().body(inVirtualEquipmentService.getByDigitalServiceVersion(digitalServiceVersionUid));
    }

    @Override
    public ResponseEntity<List<OutPhysicalEquipmentRest>> getSharedDigitalServiceOutputsPhysicalEquipmentsRest(String digitalServiceVersionUid,
                                                                                                               String shareId) {
        return ResponseEntity.ok().body(outPhysicalEquipmentService.getByDigitalServiceVersionUid(digitalServiceVersionUid));
    }

    @Override
    public ResponseEntity<List<OutVirtualEquipmentRest>> getSharedDigitalServiceOutputsVirtualEquipmentsRest(String digitalServiceVersionUid,
                                                                                                             String shareId) {
        return ResponseEntity.ok().body(outVirtualEquipmentService.getByDigitalServiceVersionUid(digitalServiceVersionUid));
    }


    @Override
    public ResponseEntity<Boolean> getSharedDigitalServiceLinkValidation(String digitalServiceVersionUid,
                                                                         String shareId) {
        return ResponseEntity.ok().body(digitalServiceVersionService.validateDigitalServiceSharedLink(digitalServiceVersionUid, shareId));
    }


    @Override
    public ResponseEntity<SharedDigitalServiceReferentialRest> getSharedReferentialData(String digitalServiceUid, String shareId) {
        return ResponseEntity.ok().body(SharedDigitalServiceReferentialRest.builder()
                .terminalTypes(digitalServiceReferentialRestMapper.toDeviceTypeDto(digitalServiceReferentialService.getTerminalDeviceType()))
                .networkTypes(digitalServiceReferentialRestMapper.toNetworkTypeDto(digitalServiceReferentialService.getNetworkType()))
                .computeServerTypes(digitalServiceReferentialRestMapper.toServerHostDto(digitalServiceReferentialService.getServerHosts("Compute")))
                .storageServerTypes(digitalServiceReferentialRestMapper.toServerHostDto(digitalServiceReferentialService.getServerHosts("Storage")))
                .countries(digitalServiceReferentialService.getBoaviztaCountryMap())
                .build());
    }


    @Override
    public ResponseEntity<DigitalServiceVersionRest> getSharedDigitalServiceLinkMetadata(String digitalServiceVersionUid,
                                                                                         String shareId) {
        return ResponseEntity.ok(digitalServiceVersionRestMapper.toDto(digitalServiceVersionService.getDigitalServiceVersion(digitalServiceVersionUid)));
    }

    @Override
    public ResponseEntity<List<BusinessHoursRest>> getSharedDigitalServiceBusinessHours(String digitalServiceVersionUid,
                                                                                        String shareId) {
        return ResponseEntity.ok().body(businessHoursService.getBusinessHours());
    }

    @Override
    public ResponseEntity<VersionRest> getSharedVersion(String shareId) {
        return ResponseEntity.ok(versionService.getVersion());
    }
}
