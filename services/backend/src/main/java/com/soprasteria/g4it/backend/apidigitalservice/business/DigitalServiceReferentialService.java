/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apidigitalservice.business;

import com.soprasteria.g4it.backend.apidigitalservice.mapper.DigitalServiceReferentialMapper;
import com.soprasteria.g4it.backend.apidigitalservice.model.DeviceTypeBO;
import com.soprasteria.g4it.backend.apidigitalservice.model.EcomindTypeBO;
import com.soprasteria.g4it.backend.apidigitalservice.model.NetworkTypeBO;
import com.soprasteria.g4it.backend.apidigitalservice.model.ServerHostBO;
import com.soprasteria.g4it.backend.apidigitalservice.modeldb.referential.DeviceTypeRef;
import com.soprasteria.g4it.backend.apidigitalservice.modeldb.referential.EcomindTypeRef;
import com.soprasteria.g4it.backend.apidigitalservice.modeldb.referential.NetworkTypeRef;
import com.soprasteria.g4it.backend.apidigitalservice.modeldb.referential.ServerHostRef;
import com.soprasteria.g4it.backend.apidigitalservice.repository.DeviceTypeRefRepository;
import com.soprasteria.g4it.backend.apidigitalservice.repository.EcomindTypeRefRepository;
import com.soprasteria.g4it.backend.apidigitalservice.repository.NetworkTypeRefRepository;
import com.soprasteria.g4it.backend.apidigitalservice.repository.ServerHostRefRepository;
import com.soprasteria.g4it.backend.apireferential.business.ReferentialGetService;
import com.soprasteria.g4it.backend.exception.InvalidReferentialException;
import com.soprasteria.g4it.backend.external.boavizta.business.BoaviztapiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Digital Service Referential Service.
 */
@Service
public class DigitalServiceReferentialService {

    /**
     * Repository to access data.
     */
    @Autowired
    private DeviceTypeRefRepository deviceTypeRefRepository;

    /**
     * Network type referential repository.
     */
    @Autowired
    private NetworkTypeRefRepository networkTypeRefRepository;

    /**
     * Server host referential repository.
     */
    @Autowired
    private ServerHostRefRepository serverHostRefRepository;

    /**
     * Ecomind type referential repository.
     */
    @Autowired
    private EcomindTypeRefRepository ecomindTypeRefRepository;

    /**
     * Referential Mapper.
     */
    @Autowired
    private DigitalServiceReferentialMapper digitalServiceReferentialMapper;

    /**
     * Boaviztapi  Service.
     */
    @Autowired
    private BoaviztapiService boaviztapiService;

    /**
     * Referential Service.
     */
    @Autowired
    private ReferentialGetService referentialGetService;


    /**
     * Get terminal device type referential data.
     *
     * @return the list of device type (business object).
     */
    public List<DeviceTypeBO> getTerminalDeviceType() {
        return digitalServiceReferentialMapper.toDeviceTypeBusinessObject(deviceTypeRefRepository.findAll());
    }

    /**
     * Get all Ecomind type referential data.
     *
     * @return the list of ecomind type (business object).
     */
    public List<EcomindTypeBO> getEcomindDeviceType() {
        return digitalServiceReferentialMapper.toEcomindTypeBusinessObject(ecomindTypeRefRepository.findAll());
    }

    /**
     * Get Ecomind type by reference.
     *
     * @param reference the searched reference
     * @return the referential data or else throw runtime exception.
     */
    public EcomindTypeRef getEcomindDeviceType(final String reference) {
        return ecomindTypeRefRepository.findByReference(reference).orElseThrow(() -> new InvalidReferentialException("Ecomind type code not found"));
    }

    /**
     * Get terminal device type by reference.
     *
     * @param reference the searched reference
     * @return the referential data or else throw runtime exception.
     */
    public DeviceTypeRef getTerminalDeviceType(final String reference) {
        return deviceTypeRefRepository.findByReference(reference).orElseThrow(() -> new InvalidReferentialException("terminal.type.code"));
    }

    /**
     * Get country from NumEcoEval.
     *
     * @return list of country.
     */
    public List<String> getCountry() {
        return referentialGetService.getCountries(null);
    }

    /**
     * Get network type referential data.
     *
     * @return the list of network type (business object).
     */
    public List<NetworkTypeBO> getNetworkType() {
        return digitalServiceReferentialMapper.toNetworkTypeBusinessObject(networkTypeRefRepository.findAll());
    }

    /**
     * Get network type by reference.
     *
     * @param reference the searched reference
     * @return the referential data or else throw runtime exception.
     */
    public NetworkTypeRef getNetworkType(final String reference) {
        return networkTypeRefRepository.findByReference(reference).orElseThrow(() -> new InvalidReferentialException("network.type.code"));
    }

    /**
     * Get server host type referential data.
     *
     * @return the list of server host (business object).
     */
    public List<ServerHostBO> getServerHosts(final String type) {
        return digitalServiceReferentialMapper.serverDTOtoServerHostBusinessObject(serverHostRefRepository.findServerHostRefByType(type));
    }

    /**
     * Get server host referential data.
     *
     * @param id the server host id.
     * @return the list of server host (business object).
     */
    public ServerHostRef getServerHost(final long id) {
        return serverHostRefRepository.findById(id)
                .orElseThrow(() -> new InvalidReferentialException("server.host.code"));
    }

    /**
     * Get BoaviztAPI countries.
     *
     * @return country list (string).
     */
    public Map<String, String> getBoaviztaCountryMap() {
        return boaviztapiService.getCountryMap();
    }

    /**
     * Get BoaviztAPI cloud providers.
     *
     * @return providers name list (string).
     */
    public List<String> getCloudProviders() {
        return boaviztapiService.getProviderList();
    }

    /**
     * Get BoaviztAPI instances for one specific cloud provider.
     *
     * @return instances name list (string).
     */
    public List<String> getCloudInstances(String cloudProviderName) {
        return boaviztapiService.getInstanceList(cloudProviderName);
    }
}
