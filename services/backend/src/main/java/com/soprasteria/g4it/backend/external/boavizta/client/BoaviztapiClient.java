/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.external.boavizta.client;

import com.soprasteria.g4it.backend.apiinout.modeldb.InVirtualEquipment;
import com.soprasteria.g4it.backend.common.utils.JsonUtils;
import com.soprasteria.g4it.backend.exception.ExternalApiException;
import com.soprasteria.g4it.backend.external.boavizta.model.request.BoaRequestRest;
import com.soprasteria.g4it.backend.external.boavizta.model.request.BoaTimeWorkloadRest;
import com.soprasteria.g4it.backend.external.boavizta.model.request.BoaUsageRest;
import com.soprasteria.g4it.backend.external.boavizta.model.response.BoaResponseRest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class BoaviztapiClient {

    @Autowired
    @Qualifier("webClientBoaviztapi")
    WebClient webClient;

    private static final String CRITERIA = "criteria";
    private static final int USE_TIME_RATIO = 1;

    /**
     * hours life time is calculated by h5 * 365 * 24;
     */
    private static final int HOURS_LIFE_TIME = 43800;
    public static final String BOAVIZTAPI_VERSION = "1.3";
    public static final String BOAVIZTAPI_ENGINE = "BoaviztAPI";

    /**
     * Get all available countries from BoaviztAPI
     *
     * @return a map of (country, country_trigram)
     */
    public Map<String, String> getAllCountries() {
        try {
            Map<String, String> response = webClient.get().uri("/v1/utils/country_code").retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, String>>() {
                    }).block();
            if (response == null) {
                throw new ExternalApiException(404, "boavitzapi-no-country-found");
            }
            return response;
        } catch (WebClientRequestException e) {
            throw new ExternalApiException(404, "boavitzapi-no-country-found");
        }
    }

    /**
     * Get all available cloud providers from BoaviztAPI
     *
     * @return the list of cloud providers
     */
    public List<String> getAllProviders() {
        try {
            String[] response = webClient.get().uri("/v1/cloud/instance/all_providers").retrieve().bodyToMono(String[].class).block();
            if (response == null) {
                throw new ExternalApiException(404, "boavitzapi-no-cloud-provider-found");
            }
            return Arrays.asList(response);
        } catch (WebClientRequestException e) {
            throw new ExternalApiException(404, "boavitzapi-no-cloud-provider-found");
        }
    }

    /**
     * Get all available instances for one specific cloud provider from BoaviztAPI
     *
     * @return the list of instances name
     */
    public List<String> getAllInstances(String cloudProvider) {
        try {
            String[] response = webClient.get().uri("/v1/cloud/instance/all_instances?provider=" + cloudProvider).retrieve().bodyToMono(String[].class).block();
            if (response == null) {
                throw new ExternalApiException(404, "boavitzapi-no-instance-found");
            }
            return Arrays.asList(response);
        } catch (WebClientRequestException e) {
            throw new ExternalApiException(404, "boavitzapi-no-instance-found");
        }
    }

    /**
     * Run calcaulation on BoavitzAPI
     *
     * @param virtualEquipment the usage virtualEquipment
     * @return the response
     */
    public BoaResponseRest runCalculation(InVirtualEquipment virtualEquipment) {

        String provider = virtualEquipment.getProvider();
        String instanceType = virtualEquipment.getInstanceType();
        String location = virtualEquipment.getLocation();
        String context = String.join("-", provider, instanceType, location);

        BoaRequestRest request = BoaRequestRest.builder()
                .provider(provider)
                .instanceType(instanceType)
                .usage(BoaUsageRest.builder()
                        .usageLocation(location)
                        .useTimeRatio(USE_TIME_RATIO)
                        .hoursLifeTime(HOURS_LIFE_TIME)
                        .timeWorkload(List.of(
                                BoaTimeWorkloadRest.builder()
                                        .timePercentage(100)
                                        .loadPercentage((int) (virtualEquipment.getWorkload() * 100))
                                        .build()
                        ))
                        .build())
                .build();

        UriComponents uriComponents = UriComponentsBuilder.newInstance()
                .path("/v1/cloud/instance")
                .queryParam("verbose", false)
                .queryParam("duration", virtualEquipment.getDurationHour())
                .queryParam(CRITERIA, "gwp")
                .queryParam(CRITERIA, "adpe")
                .queryParam(CRITERIA, "ir")
                .queryParam(CRITERIA, "ap")
                .queryParam(CRITERIA, "pm")
                .queryParam(CRITERIA, "odp")
                .queryParam(CRITERIA, "pocp")
                .queryParam(CRITERIA, "ept")
                .queryParam(CRITERIA, "epf")
                .queryParam(CRITERIA, "epm")
                .queryParam(CRITERIA, "adpf")
                .build();
        try {
            BoaResponseRest response = webClient.post()
                    .uri(uriComponents.toUriString())
                    .accept(MediaType.APPLICATION_JSON)
                    .body(Mono.just(request), BoaRequestRest.class)
                    .retrieve()
                    .bodyToMono(BoaResponseRest.class)
                    .block();

            if (response == null) {
                throw new ExternalApiException(500,
                        String.format("the response is null when calling with %s", context));
            }
            return response;
        } catch (WebClientRequestException e) {
            log.error("Error calling boaviztAPI with body {}", JsonUtils.toJson(request));
            throw new ExternalApiException(400, String.format("Context: %s, error: %s", context, e.getMessage()));
        } catch (WebClientResponseException e) {
            log.error("Error calling boaviztAPI with body {}", JsonUtils.toJson(request));
            throw new ExternalApiException(e.getStatusCode().value(), String.format("Context: %s, error: %s", context, e.getMessage()));
        }
    }
}
