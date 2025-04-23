/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apidigitalservice.business;

import com.soprasteria.g4it.backend.apidigitalservice.mapper.DatacenterDigitalServiceMapper;
import com.soprasteria.g4it.backend.apidigitalservice.mapper.DigitalServiceMapper;
import com.soprasteria.g4it.backend.apidigitalservice.model.*;
import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DatacenterDigitalService;
import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalService;
import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalServiceLink;
import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalServiceShared;
import com.soprasteria.g4it.backend.apidigitalservice.modeldb.referential.NetworkTypeRef;
import com.soprasteria.g4it.backend.apidigitalservice.modeldb.referential.ServerHostRef;
import com.soprasteria.g4it.backend.apidigitalservice.repository.DatacenterDigitalServiceRepository;
import com.soprasteria.g4it.backend.apidigitalservice.repository.DigitalServiceLinkRepository;
import com.soprasteria.g4it.backend.apidigitalservice.repository.DigitalServiceRepository;
import com.soprasteria.g4it.backend.apidigitalservice.repository.DigitalServiceSharedRepository;
import com.soprasteria.g4it.backend.apievaluating.business.asyncevaluatingservice.EvaluateCloudService;
import com.soprasteria.g4it.backend.apievaluating.business.asyncevaluatingservice.ExportService;
import com.soprasteria.g4it.backend.apiindicator.business.IndicatorService;
import com.soprasteria.g4it.backend.apiinout.repository.InDatacenterRepository;
import com.soprasteria.g4it.backend.apiinout.repository.InPhysicalEquipmentRepository;
import com.soprasteria.g4it.backend.apiinout.repository.InVirtualEquipmentRepository;
import com.soprasteria.g4it.backend.apiinout.repository.OutVirtualEquipmentRepository;
import com.soprasteria.g4it.backend.apiuser.business.OrganizationService;
import com.soprasteria.g4it.backend.apiuser.model.UserBO;
import com.soprasteria.g4it.backend.apiuser.model.UserInfoBO;
import com.soprasteria.g4it.backend.apiuser.modeldb.Organization;
import com.soprasteria.g4it.backend.apiuser.modeldb.User;
import com.soprasteria.g4it.backend.apiuser.repository.UserRepository;
import com.soprasteria.g4it.backend.client.gen.connector.apiexposition.dto.ModeRest;
import com.soprasteria.g4it.backend.common.criteria.CriteriaService;
import com.soprasteria.g4it.backend.common.filesystem.model.FileMapperInfo;
import com.soprasteria.g4it.backend.common.filesystem.model.FileType;
import com.soprasteria.g4it.backend.common.filesystem.model.Header;
import com.soprasteria.g4it.backend.common.model.Context;
import com.soprasteria.g4it.backend.common.task.business.TaskService;
import com.soprasteria.g4it.backend.common.task.model.TaskStatus;
import com.soprasteria.g4it.backend.common.task.modeldb.Task;
import com.soprasteria.g4it.backend.exception.G4itRestException;
import com.soprasteria.g4it.backend.exception.InvalidReferentialException;
import com.soprasteria.g4it.backend.exception.UnableToGenerateFileException;
import com.soprasteria.g4it.backend.external.numecoeval.business.NumEcoEvalRemotingService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Digital-Service service.
 */
@Service
@Slf4j
public class DigitalServiceService {

    private static final String DEFAULT_NAME_PREFIX = "Digital Service";
    @Autowired
    private DigitalServiceSharedRepository digitalServiceSharedRepository;
    @Autowired
    private DigitalServiceRepository digitalServiceRepository;
    @Autowired
    private DatacenterDigitalServiceRepository datacenterDigitalServiceRepository;
    @Autowired
    private DigitalServiceReferentialService digitalServiceReferentialService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private DigitalServiceLinkRepository digitalServiceLinkRepository;
    @Autowired
    private DigitalServiceMapper digitalServiceMapper;
    @Autowired
    private DatacenterDigitalServiceMapper datacenterDigitalServiceMapper;
    @Autowired
    private NumEcoEvalRemotingService numEcoEvalRemotingService;
    @Autowired
    private IndicatorService indicatorService;
    @Autowired
    private OrganizationService organizationService;
    @Autowired
    private CriteriaService criteriaService;
    @Autowired
    private FileMapperInfo fileInfo;
    @Autowired
    private InDatacenterRepository inDatacenterRepository;
    @Autowired
    private InPhysicalEquipmentRepository inPhysicalEquipmentRepository;
    @Autowired
    private InVirtualEquipmentRepository inVirtualEquipmentRepository;
    @Value("${batch.local.working.folder.base.path:}")
    private String localWorkingPath;
    @Autowired
    private TaskService taskService;
    @Autowired
    private EvaluateCloudService evaluateCloudService;
    @Autowired
    private OutVirtualEquipmentRepository outVirtualEquipmentRepository;
    @Autowired
    private ExportService exportService;

    /**
     * Create a new digital service.
     *
     * @param organizationId the linked organization's id.
     * @param userId         the userId.
     * @return the business object corresponding on the digital service created.
     */
    public DigitalServiceBO createDigitalService(final Long organizationId, final long userId) {
        // Get the linked organization.
        final Organization linkedOrganization = organizationService.getOrganizationById(organizationId);

        // Get last index to create digital service.
        final List<DigitalService> userDigitalServices = digitalServiceRepository.findByOrganizationAndUserId(linkedOrganization, userId);
        final Integer lastDigitalServiceDefaultNumber = userDigitalServices
                .stream()
                .map(DigitalService::getName)
                .filter(name -> name.matches("^" + DEFAULT_NAME_PREFIX + " \\d+$"))
                .map(name -> name.replace(DEFAULT_NAME_PREFIX + " ", ""))
                .map(Integer::valueOf)
                .max(Comparator.naturalOrder()).orElse(0);

        // Get the linked user.
        final User user = userRepository.findById(userId).orElseThrow();

        // Save the digital service with +1 on index name.
        final LocalDateTime now = LocalDateTime.now();
        final DigitalService digitalServiceToSave = DigitalService
                .builder()
                .name(DEFAULT_NAME_PREFIX + " " + (lastDigitalServiceDefaultNumber + 1))
                .user(user)
                .organization(linkedOrganization)
                .creationDate(now)
                .lastUpdateDate(now)
                .isNewArch(true)
                .build();
        digitalServiceToSave.addDatacenter(DatacenterDigitalService.builder()
                .name("Default DC")
                .location("France")
                .pue(BigDecimal.valueOf(1.5))
                .build());
        final DigitalService digitalServiceSaved = digitalServiceRepository.save(digitalServiceToSave);

        // Return the business object.
        return digitalServiceMapper.toBusinessObject(digitalServiceSaved);
    }

    /**
     * Get the digital service list linked to a user.
     *
     * @param organizationId the organization's id.
     * @param userId         the userId.
     * @return the digital service list.
     */
    public List<DigitalServiceBO> getDigitalServices(final Long organizationId, final long userId) {
        final Organization linkedOrganization = organizationService.getOrganizationById(organizationId);

        // Retrieve digital services created by the user
        List<DigitalService> digitalServices = digitalServiceRepository.findByOrganizationAndUserId(linkedOrganization, userId);

        // Retrieve shared digital services for the user
        List<DigitalService> sharedDigitalServices = digitalServiceSharedRepository.findByOrganizationAndUserId(linkedOrganization, userId)
                .stream()
                .map(DigitalServiceShared::getDigitalService)
                .toList();

        final List<DigitalService> combinedDigitalServices = Stream.concat(digitalServices.stream(), sharedDigitalServices.stream())
                .toList();
        List<DigitalServiceBO> allDigitalServicesBO = digitalServiceMapper.toBusinessObject(combinedDigitalServices);


        return allDigitalServicesBO.stream().peek(digitalServiceBO -> {
            User user = getDigitalServiceEntity(digitalServiceBO.getUid()).getUser();
            //set creator info
            digitalServiceBO.setCreator(UserInfoBO.builder().id(user.getId())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName()).build());

            List<DigitalServiceShared> shared = digitalServiceSharedRepository.findByDigitalServiceUid(digitalServiceBO.getUid());
            List<UserInfoBO> members = null;
            if (shared != null) {
                members = shared.stream().map(
                        sharedDigitalService -> {
                            User userEntity = sharedDigitalService.getUser();
                            return UserInfoBO.builder().id(userEntity.getId())
                                    .firstName(userEntity.getFirstName())
                                    .lastName(userEntity.getLastName())
                                    .build();
                        }).collect(Collectors.toList());
            }
            //set member info
            digitalServiceBO.setMembers(members);

        }).toList();
    }

    /**
     * Delete a digital service.
     *
     * @param digitalServiceUid the digital service UID.
     */
    public void deleteDigitalService(final String digitalServiceUid) {
        indicatorService.deleteIndicators(digitalServiceUid);
        inVirtualEquipmentRepository.deleteByDigitalServiceUid(digitalServiceUid);
        inPhysicalEquipmentRepository.deleteByDigitalServiceUid(digitalServiceUid);
        inDatacenterRepository.deleteByDigitalServiceUid(digitalServiceUid);
        digitalServiceRepository.deleteById(digitalServiceUid);
    }

    public void updateLastUpdateDate(final String digitalServiceUid) {
        digitalServiceRepository.updateLastUpdateDate(LocalDateTime.now(), digitalServiceUid);
    }

    /**
     * Update a digital service.
     *
     * @param digitalService the business object containing data to update.
     * @param user           the user entity
     * @return the updated digital service.
     */
    public DigitalServiceBO updateDigitalService(final DigitalServiceBO digitalService, final UserBO user) {

        // Check if digital service exist.
        final DigitalService digitalServiceToUpdate = getDigitalServiceEntity(digitalService.getUid());

        // Check if digital service was updated.
        final DigitalServiceBO digitalServiceToUpdateBO = digitalServiceMapper.toFullBusinessObject(digitalServiceToUpdate);
        if (digitalService.equals(digitalServiceToUpdateBO)) {
            return digitalServiceToUpdateBO;
        }

        // Merge digital service.
        digitalServiceMapper.mergeEntity(digitalServiceToUpdate, digitalService, digitalServiceReferentialService, User.builder().id(user.getId()).build());

        // Save the updated digital service.
        DigitalServiceBO updateDigitalServiceBO = digitalServiceMapper.toFullBusinessObject(digitalServiceRepository.save(digitalServiceToUpdate));
        updateDigitalServiceBO.setCreator(digitalService.getCreator());
        updateDigitalServiceBO.setMembers(digitalService.getMembers());
        return updateDigitalServiceBO;
    }

    /**
     * Get a digital service.
     *
     * @param digitalServiceUid the digital service id.
     * @return the business object.
     */
    public DigitalServiceBO getDigitalService(final String digitalServiceUid) {
        DigitalService digitalServiceEntity = getDigitalServiceEntity(digitalServiceUid);

        DigitalServiceBO digitalServiceBO = digitalServiceMapper.toFullBusinessObject(digitalServiceEntity);

        // Set the creator information
        User user = digitalServiceEntity.getUser();
        digitalServiceBO.setCreator(UserInfoBO.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .build());

        List<DigitalServiceShared> sharedDigitalServices = digitalServiceSharedRepository.findByDigitalServiceUid(digitalServiceUid);

        List<UserInfoBO> members = sharedDigitalServices.stream()
                .map(sharedDigitalService -> {
                    User sharedUser = sharedDigitalService.getUser();
                    return UserInfoBO.builder()
                            .id(sharedUser.getId())
                            .firstName(sharedUser.getFirstName())
                            .lastName(sharedUser.getLastName())
                            .build();
                })
                .collect(Collectors.toList());
        // Set the members' information
        digitalServiceBO.setMembers(members);

        return digitalServiceBO;
    }

    /**
     * Get the existing data centers linked to digital service.
     *
     * @param digitalServiceUid the inventory date.
     * @return existing datacenters linked to digital service.
     */
    public List<ServerDataCenterBO> getDigitalServiceDataCenter(final String digitalServiceUid) {
        return datacenterDigitalServiceMapper.toBusinessObject(datacenterDigitalServiceRepository.findByDigitalServiceUid(digitalServiceUid));
    }

    /**
     * Run calculations in numEcoEval.
     *
     * @param organizationId    the linked organization's id.
     * @param digitalServiceUid the digital service id.
     */
    public void runCalculations(final String subscriber, final Long organizationId, final String digitalServiceUid) {
        final Organization linkedOrganization = organizationService.getOrganizationById(organizationId);

        // Remove indicators.
        indicatorService.deleteIndicators(digitalServiceUid);
        taskService.getTask(digitalServiceUid)
                .ifPresent(value -> outVirtualEquipmentRepository.deleteByTaskId(value.getId()));

        final DigitalServiceBO digitalService = getDigitalService(digitalServiceUid);

        // Build Physical Equipment file content.
        final String physicalEquipmentHeadersFileContent = fileInfo.getMapping(FileType.EQUIPEMENT_PHYSIQUE).stream().map(Header::getName).collect(Collectors.joining(";"));
        final String physicalEquipmentTerminalsFileContent = Optional.ofNullable(digitalService.getTerminals()).orElse(new ArrayList<>()).stream().map(this::buildLineForTerminal).collect(Collectors.joining(System.lineSeparator()));
        final String physicalEquipmentNetworksFileContent = Optional.ofNullable(digitalService.getNetworks()).orElse(new ArrayList<>()).stream().map(this::buildLineForNetwork).collect(Collectors.joining(System.lineSeparator()));
        final String physicalEquipmentServerFileContent = Optional.ofNullable(digitalService.getServers()).orElse(new ArrayList<>()).stream().map(this::buildLineForServer).collect(Collectors.joining(System.lineSeparator()));
        final String physicalEquipmentFileContent = String.join(System.lineSeparator(), physicalEquipmentHeadersFileContent, physicalEquipmentTerminalsFileContent, physicalEquipmentNetworksFileContent, physicalEquipmentServerFileContent);

        // Build Datacenter file content.
        final String datacenterHeadersFileContent = fileInfo.getMapping(FileType.DATACENTER).stream().map(Header::getName).collect(Collectors.joining(";"));
        final String datacenterServerFileContent = Optional.ofNullable(digitalService.getServers()).orElse(new ArrayList<>()).stream().map(ServerBO::getDatacenter).map(this::buildDatacenterLineWithServer).collect(Collectors.joining(System.lineSeparator()));
        final String datacenterFileContent = String.join(System.lineSeparator(), datacenterHeadersFileContent, datacenterServerFileContent);

        final String virtualEquipmentHeadersFileContent = fileInfo.getMapping(FileType.EQUIPEMENT_VIRTUEL).stream().map(Header::getName).collect(Collectors.joining(";"));
        final String virtualEquipmentServerFileContent = Optional.ofNullable(digitalService.getServers())
                .orElse(new ArrayList<>())
                .stream()
                .map(this::buildVirtualEquipmentLineWithServer)
                .collect(Collectors.joining(System.lineSeparator()));
        final String virtualEquipmentFileContent = String.join(System.lineSeparator(), virtualEquipmentHeadersFileContent, virtualEquipmentServerFileContent);

        // Write Files.
        final File dir = Path.of(localWorkingPath, UUID.randomUUID().toString()).toFile();
        if (dir.mkdirs()) {
            final FileSystemResource physicalEquipmentResource = writeToFile(dir, "physicalequipment.csv", physicalEquipmentFileContent);
            final FileSystemResource datacenterResource = writeToFile(dir, "datacenter.csv", datacenterFileContent);
            final FileSystemResource virtualEquipmentResource = writeToFile(dir, "virtualequipment.csv", virtualEquipmentFileContent);

            // Call numEcoEval.
            numEcoEvalRemotingService.callInputDataExposition(datacenterResource, physicalEquipmentResource, virtualEquipmentResource, null,
                    String.valueOf(linkedOrganization.getId()), null, digitalServiceUid);

            final boolean isDelete = FileSystemUtils.deleteRecursively(dir);
            if (!isDelete) {
                log.error("Unable to delete temp csv folder {}", dir.getAbsolutePath());
                throw new UnableToGenerateFileException();
            }
            // Get criteria for digital service
            List<String> criteriaKeyList = criteriaService.getSelectedCriteriaForDigitalService(subscriber, organizationId, digitalService.getCriteria()).active();
            // Call NumEcoEval in SYNC mode, it means calculations are finished after this statement
            numEcoEvalRemotingService.callCalculation(digitalServiceUid, ModeRest.SYNC, criteriaKeyList);

            // Update last calculation date.
            final DigitalService digitalServiceToUpdate = getDigitalServiceEntity(digitalServiceUid);
            digitalServiceToUpdate.setLastCalculationDate(LocalDateTime.now());
            digitalServiceRepository.save(digitalServiceToUpdate);

            if (inVirtualEquipmentRepository.countByDigitalServiceUid(digitalServiceUid) == 0) return;

            Task task = taskService.createDigitalServiceTask(digitalServiceToUpdate, criteriaKeyList);

            Context context = Context.builder()
                    .subscriber(subscriber)
                    .organizationId(organizationId)
                    .digitalServiceUid(digitalServiceUid)
                    .digitalServiceName(digitalService.getName())
                    .datetime(LocalDateTime.now())
                    .build();

            Path exportDirectory = exportService.createExportDirectory(task.getId());
            evaluateCloudService.doEvaluate(context, task, exportDirectory);
            exportService.uploadExportZip(task.getId(), context.getSubscriber(), context.getOrganizationId().toString());

            task.setProgressPercentage("100%");
            task.setStatus(TaskStatus.COMPLETED.toString());
            task.setLastUpdateDate(LocalDateTime.now());
            taskService.saveTask(task);
        } else {
            log.error("Unable to write in folder {}.", dir.getAbsolutePath());
            throw new UnableToGenerateFileException();
        }
    }

    private DigitalService getDigitalServiceEntity(final String digitalServiceUid) {
        return digitalServiceRepository.findById(digitalServiceUid)
                .orElseThrow(() -> new G4itRestException("404", String.format("Digital Service %s not found.", digitalServiceUid)));
    }

    private FileSystemResource writeToFile(final File dir, final String filename, final String fileContent) {
        final File file = new File(dir, filename);
        try (final FileWriter fileWriter = new FileWriter(file)) {
            final PrintWriter printWriter = new PrintWriter(fileWriter);
            printWriter.print(fileContent);
            printWriter.close();
        } catch (final IOException e) {
            log.error("Unable to create temp csv file {}", fileContent, e);
            throw new UnableToGenerateFileException();
        }
        return new FileSystemResource(file);
    }

    private String buildLineForTerminal(final TerminalBO terminalBO) {
        return String.join(";",
                terminalBO.getUid(), // physicalEquipment name.
                "", // entity name.
                "", // source.
                terminalBO.getType().getCode(), // model (type is mandatory, so, not nullable.
                String.valueOf(calculateQuantity(terminalBO)), // quantity.
                "Terminal", // type.
                "", // status.
                Optional.ofNullable(terminalBO.getCountry()).orElse(""), // country of use.
                "", // user.
                LocalDate.now()
                        .minusDays((long) Math.floor(365d * terminalBO.getLifespan()))
                        .format(DateTimeFormatter.ISO_LOCAL_DATE), // date of purchase.
                LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE), // date of withdrawal.
                "", // core number.
                "", // datacenter short name.
                "", // annual electricity consumption.
                "", // manufacturer.
                "", // hard drive size.
                "", // memory size.
                "" // processor type.
        );
    }

    private String buildLineForNetwork(final NetworkBO networkBO) {
        final NetworkTypeRef networkType = digitalServiceReferentialService.getNetworkType(networkBO.getType().getCode());
        final String country = networkType.getCountry();
        return String.join(";",
                networkBO.getUid(), // physicalEquipment name.
                "", // entity name.
                "", // source.
                networkBO.getType().getCode(), // model (type is mandatory, so, not nullable.
                String.valueOf(calculateQuantity(networkBO, networkType.getAnnualQuantityOfGo())), // quantity.
                "Network", // type.
                "", // status.
                Optional.ofNullable(country).orElse(""), // country of use.
                "", // user.
                LocalDate.now().minusYears(1).format(DateTimeFormatter.ISO_LOCAL_DATE), // date of purchase.
                LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE), // date of withdrawal.
                "", // core number.
                "", // datacenter short name.
                "", // annual electricity consumption.
                "", // manufacturer.
                "", // hard drive size.
                "", // memory size.
                "" // processor type.
        );
    }

    private String buildLineForServer(final ServerBO serverBO) {
        final ServerHostRef host = digitalServiceReferentialService.getServerHost(serverBO.getHost().getCode());
        return String.join(";",
                serverBO.getUid(), // physicalEquipment name.
                "", // entity name.
                "", // source.
                host.getReference(), // model (type is mandatory, so, not nullable.
                String.valueOf(calculateQuantity(serverBO)), // quantity.
                StringUtils.equalsIgnoreCase("Dedicated", serverBO.getMutualizationType()) ? "Dedicated Server" : "Shared Server", // type.
                "", // status.
                serverBO.getDatacenter().getLocation(), // country of use.
                "", // user.
                LocalDate.now().minusDays((long) Math.floor(365d * serverBO.getLifespan())).format(DateTimeFormatter.ISO_LOCAL_DATE), // date of purchase.
                LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE), // date of withdrawal.
                "", // core number.
                serverBO.getDatacenter().getUid(), // datacenter short name.
                String.valueOf(serverBO.getAnnualElectricConsumption()), // annual electricity consumption.
                "", // manufacturer.
                "", // hard drive size.
                "", // memory size.
                "" // processor type.
        );
    }

    private String buildDatacenterLineWithServer(final ServerDataCenterBO datacenterBO) {
        return String.join(";",
                datacenterBO.getUid(), // nomCourtDatacenter
                datacenterBO.getName(), // nomLongDatacenter
                String.valueOf(datacenterBO.getPue()), // pue
                datacenterBO.getLocation(), // country
                "", // entityName
                "" // source
        );
    }

    private String buildVirtualEquipmentLineWithServer(final ServerBO serverBO) {
        if (!serverBO.getVm().isEmpty()) {
            return serverBO.getVm().stream().map(vm -> buildVirtualEquipmentLineWithServer(vm, serverBO)).collect(Collectors.joining(System.lineSeparator()));
        }
        return "";
    }

    private String buildVirtualEquipmentLineWithServer(final VirtualEquipmentBO virtualEquipmentBO,
                                                       final ServerBO serverBO) {
        return String.join(";",
                virtualEquipmentBO.getUid(), // virtual equipment name.
                serverBO.getUid(), // physical equipment name.
                "", // source physical equipment.
                "", // VCPU.
                "", // entity name.
                "", // Cluster.
                "", // electricity consumption.
                "", // type.
                String.valueOf(StringUtils.equalsIgnoreCase("Compute", serverBO.getType()) ?
                        (virtualEquipmentBO.getVCpu().doubleValue() / serverBO.getTotalVCpu().doubleValue()) * (virtualEquipmentBO.getAnnualOperatingTime().doubleValue() / 8760d) * virtualEquipmentBO.getQuantity() :
                        (virtualEquipmentBO.getDisk().doubleValue() / serverBO.getTotalDisk().doubleValue()) * (virtualEquipmentBO.getAnnualOperatingTime().doubleValue() / 8760d) * virtualEquipmentBO.getQuantity()), // repartition.
                "", // source.
                "" // hdd capacity.
        );
    }

    /**
     * Calculate quantity.
     *
     * @param networkBO          object contains the data necessary for the calculation.
     * @param annualQuantityOfGo referential data for fixed network.
     * @return the result.
     */
    private Double calculateQuantity(final NetworkBO networkBO, final Integer annualQuantityOfGo) {
        final double yearlyQuantityOfGbExchanged = Optional.ofNullable(networkBO.getYearlyQuantityOfGbExchanged()).orElse((double) 0);
        if (networkBO.getType().getCode().contains("mobile")) {
            return yearlyQuantityOfGbExchanged;
        } else {
            if (annualQuantityOfGo != null && annualQuantityOfGo != 0) {
                return (yearlyQuantityOfGbExchanged) / ((double) annualQuantityOfGo);
            }
            log.error("Annual quantity of Go in referential table is invalid (null or zero).");
            throw new InvalidReferentialException("network.type.code.annualQuantityOfGo");
        }
    }

    /**
     * Calculate quantity.
     *
     * @param terminalBO object contains the data necessary for the calculation.
     * @return the result.
     */
    private Double calculateQuantity(final TerminalBO terminalBO) {
        final Double numberOfUser = Optional.ofNullable(terminalBO.getNumberOfUsers()).map(Double::valueOf).orElse((double) 0);
        final Double yearlyUsageTime = Optional.ofNullable(terminalBO.getYearlyUsageTimePerUser()).map(Double::valueOf).orElse((double) 0);
        return (numberOfUser * yearlyUsageTime) / (365 * 24);
    }

    /**
     * Calculate quantity.
     *
     * @param serverBO object containing the data necessary to calculation.
     * @return the result.
     */
    private Double calculateQuantity(final ServerBO serverBO) {
        if (StringUtils.equalsIgnoreCase("Dedicated", serverBO.getMutualizationType())) {
            return serverBO.getQuantity().doubleValue() * (serverBO.getAnnualOperatingTime().doubleValue() / 8760d);
        } else {
            return 1d;
        }
    }

    /**
     * Generate the link to share the digital service
     *
     * @param subscriber        the client subscriber name.
     * @param organizationId    the linked organization's id.
     * @param digitalServiceUid the digital service id.
     * @return the url.
     */
    public String shareDigitalService(final String subscriber, final Long organizationId,
                                      final String digitalServiceUid) {
        DigitalService digitalService = digitalServiceRepository.findById(digitalServiceUid).orElseThrow(() ->
                new G4itRestException("404", String.format("Digital service %s not found in %s/%d", digitalServiceUid, subscriber, organizationId))
        );

        DigitalServiceLink linkToCreate = DigitalServiceLink.builder()
                .digitalService(digitalService)
                .expirationDate(LocalDateTime.now().plusDays(1))
                .build();
        String uid = digitalServiceLinkRepository.save(linkToCreate).getUid();
        return String.format("/subscribers/%s/organizations/%d/digital-services/%s/share/%s",
                subscriber, organizationId, digitalServiceUid, uid);
    }

    /**
     * Associate the digital service to the user accessing the shared link
     *
     * @param subscriber        the client subscriber name.
     * @param organizationId    the linked organization's id.
     * @param digitalServiceUid the digital service id.
     * @param sharedUid         the unique id of url shared
     * @param userId            userId of the user accessing the link
     */
    public void linkDigitalServiceToUser(final String subscriber, final Long organizationId,
                                         final String digitalServiceUid, final String sharedUid, final long userId) {
        // Check if digital service exists
        DigitalService digitalService = digitalServiceRepository.findById(digitalServiceUid).orElseThrow(() ->
                new G4itRestException("404", String.format("Digital service %s not found in %s/%d", digitalServiceUid, subscriber, organizationId))
        );

        // Validate if the shared url is not expired
        Optional<DigitalServiceLink> sharedLink = digitalServiceLinkRepository.findById(sharedUid);
        if (sharedLink.isEmpty()) {
            throw new G4itRestException("410", String.format("The shared url for Digital service %s/%s/%d has expired.", digitalServiceUid, subscriber, organizationId));
        }

        User user = userRepository.findById(userId).orElseThrow();

        // if the current user owns or has already accessed the digital service then return
        if (digitalServiceRepository.existsByUidAndUserId(digitalServiceUid, userId) ||
                digitalServiceSharedRepository.existsByDigitalServiceUidAndUserId(digitalServiceUid, userId)) {
            return;
        }

        DigitalServiceShared newDigitalServiceShared = DigitalServiceShared.builder().digitalService(digitalService)
                .user(user).organization(organizationService.getOrganizationById(organizationId))
                .build();
        digitalServiceSharedRepository.save(newDigitalServiceShared);

    }

    /**
     * Unlink the shared digital service from user
     *
     * @param digitalServiceUid the shared digital service's uid
     * @param userId            the user id
     */
    public void unlinkSharedDigitalService(final String digitalServiceUid, final long userId) {

        Optional<DigitalServiceShared> optDigitalServiceShared = digitalServiceSharedRepository.findByDigitalServiceUidAndUserId(digitalServiceUid, userId);
        optDigitalServiceShared.ifPresent(digitalServiceShared -> digitalServiceSharedRepository.delete(digitalServiceShared));
    }

}
