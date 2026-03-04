package com.soprasteria.g4it.backend.external.boavizta.client;

import com.soprasteria.g4it.backend.apiinout.modeldb.InVirtualEquipment;
import com.soprasteria.g4it.backend.exception.ExternalApiException;
import com.soprasteria.g4it.backend.external.boavizta.model.request.BoaRequestRest;
import com.soprasteria.g4it.backend.external.boavizta.model.response.BoaResponseRest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BoaviztapiClientTest {

    @InjectMocks
    private BoaviztapiClient client;

    @Mock
    private WebClient webClient;

    // ---- RAW TYPES (IMPORTANT to avoid wildcard issues) ----
    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(client, "webClient", webClient);
    }

    // ============================================================
    // ===================== GET COUNTRIES ========================
    // ============================================================

    @Test
    void getAllCountries_shouldReturnMap() {

        Map<String, String> mockResponse = Map.of("France", "FRA");

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/v1/utils/country_code")).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class)))
                .thenReturn(Mono.just(mockResponse));

        Map<String, String> result = client.getAllCountries();

        assertEquals("FRA", result.get("France"));
    }

    @Test
    void getAllCountries_shouldThrowWhenNull() {

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class)))
                .thenReturn(Mono.empty());

        assertThrows(ExternalApiException.class, client::getAllCountries);
    }

    // ============================================================
    // ===================== GET PROVIDERS ========================
    // ============================================================

    @Test
    void getAllProviders_shouldReturnList() {

        String[] providers = {"aws", "azure"};

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/v1/cloud/instance/all_providers"))
                .thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String[].class))
                .thenReturn(Mono.just(providers));

        List<String> result = client.getAllProviders();

        assertEquals(2, result.size());
        assertTrue(result.contains("aws"));
    }

    @Test
    void getAllProviders_shouldThrowOnRequestException() {

        when(webClient.get()).thenThrow(WebClientRequestException.class);

        assertThrows(ExternalApiException.class, client::getAllProviders);
    }

    // ============================================================
    // ===================== GET INSTANCES ========================
    // ============================================================

    @Test
    void getAllInstances_shouldReturnList() {

        String[] instances = {"d2ads_v5", "d4ads_v5"};

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(contains("provider=azure")))
                .thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String[].class))
                .thenReturn(Mono.just(instances));

        List<String> result = client.getAllInstances("azure");

        assertEquals(2, result.size());
    }

    // ============================================================
    // ===================== RUN CALCULATION ======================
    // ============================================================

    @Test
    void runCalculation_shouldReturnResponse() {

        InVirtualEquipment virtualEquipment = mock(InVirtualEquipment.class);
        when(virtualEquipment.getProvider()).thenReturn("azure");
        when(virtualEquipment.getInstanceType()).thenReturn("d2ads_v5");
        when(virtualEquipment.getLocation()).thenReturn("FRA");
        when(virtualEquipment.getDurationHour()).thenReturn(8760d);
        when(virtualEquipment.getWorkload()).thenReturn(0.5);
        BoaResponseRest mockResponse = new BoaResponseRest();
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.accept(MediaType.APPLICATION_JSON))
                .thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(), eq(BoaRequestRest.class)))
                .thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(BoaResponseRest.class))
                .thenReturn(Mono.just(mockResponse));
        BoaResponseRest result = client.runCalculation(virtualEquipment);
        assertNotNull(result);
        assertEquals(mockResponse, result);
    }


    @Test
    void runCalculation_shouldThrowOnWebClientResponseException() {

        // GIVEN
        InVirtualEquipment virtualEquipment = mock(InVirtualEquipment.class);
        when(virtualEquipment.getProvider()).thenReturn("azure");
        when(virtualEquipment.getInstanceType()).thenReturn("d2ads_v5");
        when(virtualEquipment.getLocation()).thenReturn("FRA");
        when(virtualEquipment.getDurationHour()).thenReturn(8760d);
        when(virtualEquipment.getWorkload()).thenReturn(0.5);

        WebClientResponseException webClientException =
                WebClientResponseException.create(
                        400,
                        "Bad Request",
                        HttpHeaders.EMPTY,
                        "error".getBytes(),
                        StandardCharsets.UTF_8
                );

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.accept(MediaType.APPLICATION_JSON))
                .thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(), eq(BoaRequestRest.class)))
                .thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(BoaResponseRest.class))
                .thenReturn(Mono.error(webClientException));

        // WHEN + THEN
        assertThrows(
                ExternalApiException.class,
                () -> client.runCalculation(virtualEquipment)
        );
    }

}
