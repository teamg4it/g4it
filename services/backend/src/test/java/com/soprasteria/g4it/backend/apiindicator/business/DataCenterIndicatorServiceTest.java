/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiindicator.business;

import com.soprasteria.g4it.backend.apiindicator.mapper.DataCenterIndicatorMapper;
import com.soprasteria.g4it.backend.apiindicator.model.DataCentersInformationBO;
import com.soprasteria.g4it.backend.apiindicator.modeldb.InDatacenterIndicatorView;
import com.soprasteria.g4it.backend.apiindicator.repository.InDatacenterViewRepository;
import com.soprasteria.g4it.backend.apiuser.business.OrganizationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataCenterIndicatorServiceTest {

    @Mock
    private InDatacenterViewRepository inDatacenterViewRepository;

    @Mock
    private DataCenterIndicatorMapper dataCenterIndicatorMapper;

    @Mock
    private OrganizationService organizationService;

    @InjectMocks
    private DataCenterIndicatorService dataCenterIndicatorService;

    @Test
    void retrievesDataCenterIndicatorsSuccessfully() {
        Long inventoryId = 1L;
        List<InDatacenterIndicatorView> mockResult = List.of(InDatacenterIndicatorView.builder().name("name").build());
        List<DataCentersInformationBO> mappedResult = List.of(new DataCentersInformationBO());
        when(inDatacenterViewRepository.findDataCenterIndicators(inventoryId)).thenReturn(mockResult);
        when(dataCenterIndicatorMapper.toInDataCentersDto(mockResult)).thenReturn(mappedResult);

        List<DataCentersInformationBO> result = dataCenterIndicatorService.getDataCenterIndicators(inventoryId);

        assertEquals(mappedResult, result);
        verify(inDatacenterViewRepository).findDataCenterIndicators(inventoryId);
        verify(dataCenterIndicatorMapper).toInDataCentersDto(mockResult);
    }

    @Test
    void returnsEmptyListWhenNoDataCenterIndicatorsFound() {
        Long inventoryId = 1L;

        when(inDatacenterViewRepository.findDataCenterIndicators(inventoryId)).thenReturn(List.of());
        when(dataCenterIndicatorMapper.toInDataCentersDto(List.of())).thenReturn(List.of());

        List<DataCentersInformationBO> result = dataCenterIndicatorService.getDataCenterIndicators(inventoryId);

        assertTrue(result.isEmpty());
        verify(inDatacenterViewRepository).findDataCenterIndicators(inventoryId);
        verify(dataCenterIndicatorMapper).toInDataCentersDto(List.of());
    }

    @Test
    void handlesRepositoryReturningNull() {
        Long inventoryId = 1L;

        when(inDatacenterViewRepository.findDataCenterIndicators(inventoryId)).thenReturn(null);

        List<DataCentersInformationBO> result = dataCenterIndicatorService.getDataCenterIndicators(inventoryId);

        assertTrue(result.isEmpty());
        verify(inDatacenterViewRepository).findDataCenterIndicators(inventoryId);
    }

    @Test
    void handlesMapperReturningNull() {
        Long inventoryId = 1L;
        List<InDatacenterIndicatorView> mockResult = List.of(InDatacenterIndicatorView.builder().name("name").build());

        when(inDatacenterViewRepository.findDataCenterIndicators(inventoryId)).thenReturn(mockResult);
        when(dataCenterIndicatorMapper.toInDataCentersDto(mockResult)).thenReturn(List.of());

        List<DataCentersInformationBO> result = dataCenterIndicatorService.getDataCenterIndicators(inventoryId);

        assertTrue(result.isEmpty());
        verify(inDatacenterViewRepository).findDataCenterIndicators(inventoryId);
        verify(dataCenterIndicatorMapper).toInDataCentersDto(mockResult);
    }

}
