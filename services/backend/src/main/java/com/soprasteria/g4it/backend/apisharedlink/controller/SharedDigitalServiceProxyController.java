/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apisharedlink.controller;

import com.soprasteria.g4it.backend.apidigitalservice.business.DigitalServiceReferentialService;
import com.soprasteria.g4it.backend.apidigitalservice.mapper.DigitalServiceReferentialRestMapper;
import com.soprasteria.g4it.backend.apiinout.business.InDatacenterService;
import com.soprasteria.g4it.backend.apiinout.business.InPhysicalEquipmentService;
import com.soprasteria.g4it.backend.apiinout.business.InVirtualEquipmentService;
import com.soprasteria.g4it.backend.apiinout.business.OutPhysicalEquipmentService;
import com.soprasteria.g4it.backend.apiinout.business.OutVirtualEquipmentService;
import com.soprasteria.g4it.backend.server.gen.api.SharedLinkDigitalServiceItemsApiDelegate;
import com.soprasteria.g4it.backend.server.gen.api.dto.InDatacenterRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.InPhysicalEquipmentRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.InVirtualEquipmentRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.OutPhysicalEquipmentRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.OutVirtualEquipmentRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.SharedDigitalServiceReferentielRest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class SharedDigitalServiceProxyController implements SharedLinkDigitalServiceItemsApiDelegate {

    private InPhysicalEquipmentService inPhysicalEquipmentService;
    private InDatacenterService inDatacenterService;
    private InVirtualEquipmentService inVirtualEquipmentService;
    private DigitalServiceReferentialService digitalServiceReferentialService;
    private DigitalServiceReferentialRestMapper digitalServiceReferentialRestMapper;
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
        return ResponseEntity.ok().body(inDatacenterService.getByDigitalService(digitalServiceUid));
    }

    @Override
    public ResponseEntity<List<InPhysicalEquipmentRest>> getSharedDigitalServiceInputsPhysicalEquipmentsRest(String digitalServiceUid,
                                                                                                             String shareId) {
        return ResponseEntity.ok().body(inPhysicalEquipmentService.getByDigitalService(digitalServiceUid));
    }

    @Override
    public ResponseEntity<List<InVirtualEquipmentRest>> getSharedDigitalServiceInputsVirtualEquipmentsRest(String digitalServiceUid,
                                                                                                           String shareId) {
        return ResponseEntity.ok().body(inVirtualEquipmentService.getByDigitalService(digitalServiceUid));
    }

    @Override
    public ResponseEntity<List<OutPhysicalEquipmentRest>> getSharedDigitalServiceOutputsPhysicalEquipmentsRest(String digitalServiceUid,
                                                                                                               String shareId) {
        return ResponseEntity.ok().body(outPhysicalEquipmentService.getByDigitalServiceUid(digitalServiceUid));
    }

    @Override
    public ResponseEntity<List<OutVirtualEquipmentRest>> getSharedDigitalServiceOutputsVirtualEquipmentsRest(String digitalServiceUid,
                                                                                                             String shareId) {
        return ResponseEntity.ok().body(outVirtualEquipmentService.getByDigitalServiceUid(digitalServiceUid));
    }


    @Override
    public ResponseEntity<SharedDigitalServiceReferentielRest> getSharedReferentielData(String shareId) {
        return ResponseEntity.ok().body(SharedDigitalServiceReferentielRest.builder()
                .deviceTypeRef(digitalServiceReferentialRestMapper.toDeviceTypeDto(digitalServiceReferentialService.getTerminalDeviceType()))
                .networkTypeRef(digitalServiceReferentialRestMapper.toNetworkTypeDto(digitalServiceReferentialService.getNetworkType()))
                .serverHostRefTypeCompute(digitalServiceReferentialRestMapper.toServerHostDto(digitalServiceReferentialService.getServerHosts("Compute")))
                .serverHostRefTypeStorage(digitalServiceReferentialRestMapper.toServerHostDto(digitalServiceReferentialService.getServerHosts("Storage")))
                .countries(digitalServiceReferentialService.getBoaviztaCountryMap())
                .build());
    }
}
