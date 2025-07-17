/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.rules;

import com.soprasteria.g4it.backend.common.model.LineError;
import com.soprasteria.g4it.backend.common.utils.InfrastructureType;
import com.soprasteria.g4it.backend.external.boavizta.business.BoaviztapiService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RuleVirtualEquipmentServiceTest {

    @InjectMocks
    private RuleVirtualEquipmentService service;

    @Mock
    private MessageSource messageSource;

    @Mock
    private BoaviztapiService boaviztapiService;

    private final Locale locale = Locale.getDefault();
    private final String filename = "filename";
    private final int line = 1;

    @Test
    void testInvalidInfrastructureTypeError() {
        when(messageSource.getMessage(eq("typeInfrastructure.values"), any(), eq(locale)))
                .thenReturn("Invalid infrastructure type");

        var actual = service.checkInfrastructureType(locale, filename, line, "INVALID");
        Assertions.assertTrue(actual.isPresent());
        Assertions.assertEquals(new LineError(filename,1, "Invalid infrastructure type"), actual.get());

    }

    @Test
    void testValidInfrastructureTypeOk() {
        var actual = service.checkInfrastructureType(locale, filename, line, InfrastructureType.CLOUD_SERVICES.name());
        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    void testNullPhysicalEquipmentForNonCloudError() {
        when(messageSource.getMessage(eq("virtual.equipment.must.have.physical.equipment"), any(), eq(locale)))
                .thenReturn("Physical equipment required");

        var actual = service.checkPhysicalEquipmentLinked(locale, filename, line, InfrastructureType.NON_CLOUD_SERVERS.name(), null);
        Assertions.assertTrue(actual.isPresent());
        Assertions.assertEquals(new LineError(filename,1, "Physical equipment required"), actual.get());

    }

    @Test
    void testNullPhysicalEquipmentForCloudServiceOk() {
        var actual = service.checkPhysicalEquipmentLinked(locale, filename, line, InfrastructureType.CLOUD_SERVICES.name(), null);
        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    void testEmptyTypeError(){
        when(messageSource.getMessage(eq("virtual.equipment.must.have.typeEqv"), any(), eq(locale)))
                .thenReturn("TypeEqv required");

        var actual = service.checkType(locale, filename, line, "", 0D, 0D,false);
        Assertions.assertTrue(actual.isPresent());
        Assertions.assertEquals(new LineError(filename,1, "TypeEqv required"), actual.get());
    }

    @Test
    void testTypeOk(){
        var actual = service.checkType(locale, filename, line, "TYPE", 0D, 0D,false);
        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    void testEmptyCloudLocationError(){
        when(messageSource.getMessage(eq("cloud.location.blank"), any(), eq(locale)))
                .thenReturn("Location is blank");

        var actual = service.checkCloudLocation(locale, filename, line, "");
        Assertions.assertTrue(actual.isPresent());
        Assertions.assertEquals(new LineError(filename,1, "Location is blank"), actual.get());
    }

    @Test
    void testInvalidCloudLocationInReferentialError(){
        String location = "location";
        when(boaviztapiService.getCountryMap()).thenReturn(Collections.emptyMap());
        when(messageSource.getMessage(eq("boaviztAPI.referential.location.not.exist"), any(), eq(locale)))
                .thenReturn("Location does not exist");

        var actual = service.checkCloudLocation(locale, filename, line, location);
        Assertions.assertTrue(actual.isPresent());
        Assertions.assertEquals(new LineError(filename,1, "Location does not exist"), actual.get());
    }

    @Test
    void testValidCloudLocationInReferentialOk(){
        String location = "France";
        Map<String, String> countryMap = new HashMap<>();
        countryMap.put(location, "FR");
        when(boaviztapiService.getCountryMap()).thenReturn(countryMap);

        var actual = service.checkCloudLocation(locale, filename, line, location);
        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    void testEmptyCloudProviderError(){
        when(messageSource.getMessage(eq("cloud.provider.blank"), any(), eq(locale)))
                .thenReturn("Provider is blank");

        var actual = service.checkCloudProvider(locale, filename, line, "");
        Assertions.assertTrue(actual.isPresent());
        Assertions.assertEquals(new LineError(filename,1, "Provider is blank"), actual.get());
    }

    @Test
    void testInvalidCloudProviderInReferentialError(){
        String provider = "Unknown";
        when(boaviztapiService.getProviderList()).thenReturn(Collections.emptyList());
        when(messageSource.getMessage(eq("boaviztAPI.referential.provider.not.exist"), any(), eq(locale)))
                .thenReturn("Provider not exist");

        var actual = service.checkCloudProvider(locale, filename, line, provider);
        Assertions.assertTrue(actual.isPresent());
        Assertions.assertEquals(new LineError(filename,1, "Provider not exist"), actual.get());
    }

    @Test
    void testValidCloudProviderInReferentialOk(){
        String provider = "AWS";
        when(boaviztapiService.getProviderList()).thenReturn(List.of(provider));

        var actual = service.checkCloudProvider(locale, filename, line, provider);
        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    void testEmptyCloudInstanceError(){
        when(messageSource.getMessage(eq("cloud.typeInstance.blank"), any(), eq(locale)))
                .thenReturn("InstanceType is blank");

        var actual = service.checkCloudInstanceType(locale, filename, line, "AWS", "");
        Assertions.assertTrue(actual.isPresent());
        Assertions.assertEquals(new LineError(filename,1, "InstanceType is blank"), actual.get());
    }

    @Test
    void testInvalidCloudInstanceInReferentialError(){
        String provider = "AWS";
        String instanceType = "test.medium";
        when(boaviztapiService.getProviderList()).thenReturn(List.of(provider));
        when(boaviztapiService.getInstanceList(provider)).thenReturn(List.of("m5.large"));
        when(messageSource.getMessage(eq("boaviztAPI.referential.typeInstance.not.exist"), any(), eq(locale)))
                .thenReturn("InstanceType not exist");

        var actual = service.checkCloudInstanceType(locale, filename, line, provider, instanceType);
        Assertions.assertTrue(actual.isPresent());
        Assertions.assertEquals(new LineError(filename,1, "InstanceType not exist"), actual.get());
    }

    @Test
    void testValidCloudInstanceInReferentialOk(){
        String provider = "AWS";
        String instanceType = "t2.micro";
        when(boaviztapiService.getProviderList()).thenReturn(List.of(provider));
        when(boaviztapiService.getInstanceList(provider)).thenReturn(List.of(instanceType));

        var actual = service.checkCloudInstanceType(locale, filename, line, provider, instanceType);
        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    void testInValidCloudQuantityError(){
        when(messageSource.getMessage(eq("cloud.quantity.blank"), any(), eq(locale)))
                .thenReturn("Quantity is blank");

        Assertions.assertTrue(service.checkCloudQuantity(locale, filename, line, null).isPresent());
        Assertions.assertTrue(service.checkCloudQuantity(locale, filename, line, 0.0).isPresent());
    }

    @Test
    void testValidCloudQuantityOk(){
        Assertions.assertTrue(service.checkCloudQuantity(locale, filename, line, 10.0).isEmpty());
    }

    @Test
    void testInValidCloudUsageDurationError(){
        when(messageSource.getMessage(eq("cloud.duration.blank"), any(), eq(locale)))
                .thenReturn("Duration is blank");

        Assertions.assertTrue(service.checkCloudUsageDuration(locale, filename, line, null).isPresent());
        Assertions.assertTrue(service.checkCloudUsageDuration(locale, filename, line, -1.0).isPresent());
    }

    @Test
    void testInValidCloudUsageDurationGreaterThan8760Error(){
        when(messageSource.getMessage(eq("cloud.duration.invalid"), any(), eq(locale)))
                .thenReturn("Duration invalid");

        Assertions.assertTrue(service.checkCloudUsageDuration(locale, filename, line, 9000.0).isPresent());
    }

    @Test
    void testValidCloudUsageDurationOk() {
        Assertions.assertTrue(service.checkCloudUsageDuration(locale, filename, line, 100.0).isEmpty());
    }

    @Test
    void testNullCloudWorkloadError(){
        when(messageSource.getMessage(eq("cloud.workload.blank"), any(), eq(locale)))
                .thenReturn("Workload blank");

        Assertions.assertTrue(service.checkCloudWorkload(locale, filename, line, null).isPresent());
    }

    @Test
    void testInvalidCloudWorkloadError() {
        when(messageSource.getMessage(eq("cloud.workload.invalid"), any(), eq(locale)))
                .thenReturn("Workload invalid");

        Assertions.assertTrue(service.checkCloudWorkload(locale, filename, line, -1.0).isPresent());
        Assertions.assertTrue(service.checkCloudWorkload(locale, filename, line, 101.0).isPresent());
    }

    @Test
    void testValidCloudWorkloadOk() {
        Assertions.assertTrue(service.checkCloudWorkload(locale, filename, line, 50.0).isEmpty());
    }

    @Test
    void testNullVirtualEquipmentNameError(){
        when(messageSource.getMessage(eq("cloud.equipment.blank"), any(), eq(locale)))
                .thenReturn("nomEquipementVirtuel is mandatory");

        Set<String> names = new HashSet<>();
        var actual = service.checkVirtualEquipmentName(locale, filename, line, null, names, true);
        Assertions.assertTrue(actual.isPresent());
        Assertions.assertEquals(new LineError(filename,1, "nomEquipementVirtuel is mandatory"), actual.get());
    }

    @Test
    void testDuplicateVirtualEquipmentNameError() {
        when(messageSource.getMessage(eq("cloud.equipment.unique"), any(), eq(locale)))
                .thenReturn("nomEquipementVirtuel should be unique");

        Set<String> names = new HashSet<>(Set.of("VM1"));
        var actual = service.checkVirtualEquipmentName(locale, filename, line, "VM1", names, true);
        Assertions.assertTrue(actual.isPresent());
        Assertions.assertEquals(new LineError(filename,1, "nomEquipementVirtuel should be unique"), actual.get());
    }

    @Test
    void testUniqueVirtualEquipmentNameOk() {
        Set<String> names = new HashSet<>();
        var actual = service.checkVirtualEquipmentName(locale, filename, line, "VM2", names, true);
        Assertions.assertTrue(actual.isEmpty());
        Assertions.assertTrue(names.contains("VM2"));
    }
    @Test
    void testCheckInfrastructureTypeNullIsValid() {
        Assertions.assertTrue(service.checkInfrastructureType(locale, filename, line, null).isEmpty());
    }

    @Test
    void testCheckInfrastructureTypeCloudServicesIsValid() {
        Assertions.assertTrue(service.checkInfrastructureType(locale, filename, line, InfrastructureType.CLOUD_SERVICES.name()).isEmpty());
    }

    @Test
    void testCheckInfrastructureTypeNonCloudServersIsValid() {
        Assertions.assertTrue(service.checkInfrastructureType(locale, filename, line, InfrastructureType.NON_CLOUD_SERVERS.name()).isEmpty());
    }

    @Test
    void testCheckPhysicalEquipmentLinkedWithNotCloudAndNonNullEquipment() {
        Assertions.assertTrue(service.checkPhysicalEquipmentLinked(locale, filename, line, InfrastructureType.NON_CLOUD_SERVERS.name(), "someEquip").isEmpty());
    }

    @Test
    void testCheckPhysicalEquipmentLinkedWithCloudAndNotNullEquipment() {
        Assertions.assertTrue(service.checkPhysicalEquipmentLinked(locale, filename, line, InfrastructureType.CLOUD_SERVICES.name(), "someEquip").isEmpty());
    }

    @Test
    void testCheckTypeDigitalServiceInvalidTypeEqv() {
        when(messageSource.getMessage(eq("typeEqv.invalid"), any(), eq(locale)))
                .thenReturn("Field 'typeEqv' support only two values calcul and stockage. " +
                        "Value INVALID is not recognized as an authorized value.");
        Optional<LineError> result = service.checkType(locale, filename, line, "INVALID", 1.0, 1.0, true);
        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(new LineError(filename, line, "Field 'typeEqv' support only two values calcul and stockage. " +
                "Value INVALID is not recognized as an authorized value."), result.get());
    }

    @Test
    void testCheckTypeDigitalServiceCalculNullVCpu() {
        when(messageSource.getMessage(eq("vCpu.blank"), any(), eq(locale)))
                .thenReturn("Field 'vCPU' is mandatory for type \"calcul\".");
        Optional<LineError> result = service.checkType(locale, filename, line, "calcul", 1.0, null, true);
        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(new LineError(filename, line, "Field 'vCPU' is mandatory for type \"calcul\"."), result.get());
    }

    @Test
    void testCheckTypeDigitalServiceStockageNullDiskSize() {
        when(messageSource.getMessage(eq("diskSize.blank"), any(), eq(locale)))
                .thenReturn("Field 'capaciteStockage' is mandatory for type \"stockage\".");
        Optional<LineError> result = service.checkType(locale, filename, line, "stockage", null, 1.0, true);
        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(new LineError(filename, line, "Field 'capaciteStockage' is mandatory for type \"stockage\"."), result.get());
    }

    @Test
    void testCheckTypeDigitalServiceValidCalcul() {
        Optional<LineError> result = service.checkType(locale, filename, line, "calcul", 10.0, 1.0, true);
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void testCheckTypeDigitalServiceValidStockage() {
        Optional<LineError> result = service.checkType(locale, filename, line, "stockage", 5.0, 1.0, true);
        Assertions.assertTrue(result.isEmpty());
    }

    // checkCloudLocation edge: location found
    @Test
    void testCheckCloudLocationPresentInReferentialIsOk(){
        String location = "US";
        Map<String, String> countryMap = new HashMap<>();
        countryMap.put(location, "US");
        when(boaviztapiService.getCountryMap()).thenReturn(countryMap);

        var result = service.checkCloudLocation(locale, filename, line, location);
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void testCheckCloudProviderWithNullGetProviderList() {
        when(boaviztapiService.getProviderList()).thenReturn(List.of());
        when(messageSource.getMessage(eq("boaviztAPI.referential.provider.not.exist"), any(), eq(locale))).
                thenReturn("Provider XXX does not exist in BoaviztAPI. Please check your references.");
        var result = service.checkCloudProvider(locale, filename, line, "XXX");
        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(new LineError(filename, 1, "Provider XXX does not exist in BoaviztAPI. Please check your references."), result.get());
    }
    @Test
    void testCheckCloudInstanceTypeWithEmptyProviderNoReferentialCheck() {
        when(messageSource.getMessage(eq("cloud.typeInstance.blank"), any(), eq(locale))).thenReturn("typeInstance is mandatory for cloud services");
        var result = service.checkCloudInstanceType(locale, filename, line, "", "");
        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(new LineError(filename,1, "typeInstance is mandatory for cloud services"), result.get());
    }

    @Test
    void testCheckCloudInstanceTypeWithProviderListNull() {
        when(boaviztapiService.getProviderList()).thenReturn(List.of());
        String provider = "Test";
        String instanceType = "anything";
        var result = service.checkCloudInstanceType(locale, filename, line, provider, instanceType);
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void testCheckCloudQuantityNegative() {
        when(messageSource.getMessage(eq("cloud.quantity.blank"), any(), eq(locale))).thenReturn
                ("quantity is mandatory for cloud services");
        var result = service.checkCloudQuantity(locale, filename, line, -1.0);
        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(new LineError(filename,1, "quantity is mandatory for cloud services"), result.get());
    }

    @Test
    void testCheckCloudUsageDurationZeroIsOk() {
        Assertions.assertTrue(service.checkCloudUsageDuration(locale, filename, line, 0.0).isEmpty());
    }

    @Test
    void testCheckCloudUsageDurationMaxLimitIsOk() {
        Assertions.assertTrue(service.checkCloudUsageDuration(locale, filename, line, 8760.0).isEmpty());
    }

    @Test
    void testCheckCloudWorkloadZeroBoundary() {
        Assertions.assertTrue(service.checkCloudWorkload(locale, filename, line, 0.0).isEmpty());
    }
    @Test
    void testCheckCloudWorkloadMaxBoundary() {
        Assertions.assertTrue(service.checkCloudWorkload(locale, filename, line, 100.0).isEmpty());
    }

    @Test
    void testCheckCloudWorkloadNegativeInvalid() {
        when(messageSource.getMessage(eq("cloud.workload.invalid"), any(), eq(locale))).thenReturn("Workload invalid");
        Assertions.assertTrue(service.checkCloudWorkload(locale, filename, line, -0.01).isPresent());
    }


    @Test
    void testCheckVirtualEquipmentNameNullNonCloudService() {
        when(messageSource.getMessage(eq("nomequipementvirtuel.not.blank"), any(), eq(locale)))
                .thenReturn("Field 'nomEquipementVirtuel' is mandatory.");
        Set<String> names = new HashSet<>();
        var result = service.checkVirtualEquipmentName(locale, filename, line, null, names, false);
        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(new LineError(filename,1,"Field 'nomEquipementVirtuel' is mandatory."), result.get());
    }

    // checkVirtualEquipmentName unique for !isCloudService
    @Test
    void testCheckVirtualEquipmentNameDuplicateNonCloudService() {
        when(messageSource.getMessage(eq("cloud.equipment.unique"), any(), eq(locale))).
                thenReturn("nomEquipementVirtuel should be unique");
        Set<String> names = new HashSet<>(Set.of("N1"));
        var result = service.checkVirtualEquipmentName(locale, filename, line, "N1", names, false);
        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(new LineError(filename,1,"nomEquipementVirtuel should be unique"), result.get());
    }

    @Test
    void testCheckVirtualEquipmentNameAddToSet() {
        Set<String> names = new HashSet<>();
        var result = service.checkVirtualEquipmentName(locale, filename, line, "name", names, false);
        Assertions.assertTrue(result.isEmpty());
        Assertions.assertTrue(names.contains("name"));
    }
}
