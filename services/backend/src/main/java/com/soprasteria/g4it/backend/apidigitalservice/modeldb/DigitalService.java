/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apidigitalservice.modeldb;

import com.soprasteria.g4it.backend.apiuser.modeldb.Organization;
import com.soprasteria.g4it.backend.apiuser.modeldb.User;
import com.soprasteria.g4it.backend.common.dbmodel.Note;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Digital Service Entity
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
@NoArgsConstructor
@Entity
@SuperBuilder
@Table(name = "digital_service")
public class DigitalService {

    /**
     * Primary Key : uid.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String uid;

    /**
     * Service name.
     */
    @NotNull
    private String name;

    /**
     * The Criterias key.
     */
    private List<String> criteria;

    /**
     * Linked organization.
     */
    @ToString.Exclude
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", referencedColumnName = "id")
    private Organization organization;

    /**
     * The user behind the digital service.
     */
    @NotNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    /**
     * Last calculation date.
     */
    private LocalDateTime lastCalculationDate;

    /**
     * Auto creation date.
     */
    private LocalDateTime creationDate;

    /**
     * Last update date.
     */
    private LocalDateTime lastUpdateDate;

    /**
     * is new arch
     */
    private Boolean isNewArch;

    /**
     * is migrated to new arc
     */
    private Boolean isMigrated;

    /**
     * Attached note.
     */
    @OnDelete(action = OnDeleteAction.CASCADE)
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "note_id", referencedColumnName = "id")
    private Note note;

    /**
     * Terminals.
     */
    @ToString.Exclude
    @Builder.Default
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "digitalService")
    private List<Terminal> terminals = new ArrayList<>();

    /**
     * Networks
     */
    @ToString.Exclude
    @Builder.Default
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "digitalService")
    private List<Network> networks = new ArrayList<>();

    /**
     * Servers
     */
    @ToString.Exclude
    @Builder.Default
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "digitalService")
    private List<Server> servers = new ArrayList<>();

    /**
     * Datacenter
     */
    @ToString.Exclude
    @Builder.Default
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "digitalService")
    private List<DatacenterDigitalService> datacenterDigitalServices = new ArrayList<>();

    /**
     * Digital service links
     */
    @ToString.Exclude
    @OneToMany(mappedBy = "digitalService", cascade = CascadeType.ALL, orphanRemoval = true,
            fetch = FetchType.LAZY)
    private List<DigitalServiceLink> digitalServiceLinks;

    /**
     * Mapped digital services with users
     */
    @ToString.Exclude
    @OneToMany(mappedBy = "digitalService", cascade = CascadeType.ALL, orphanRemoval = true,
            fetch = FetchType.LAZY)
    private List<DigitalServiceShared> digitalServiceShared;

    /**
     * Add Terminal.
     *
     * @param terminal terminal to add.
     */
    public void addTerminal(final Terminal terminal) {
        terminal.setDigitalService(this);
        this.terminals.add(terminal);
    }

    /**
     * Add Network.
     *
     * @param network network to add.
     */
    public void addNetwork(final Network network) {
        network.setDigitalService(this);
        this.networks.add(network);
    }

    /**
     * Add Server.
     *
     * @param server server to add.
     */
    public void addServer(final Server server) {
        server.setDigitalService(this);
        this.servers.add(server);
    }

    /**
     * Add Datacenter.
     *
     * @param datacenter datacenter to add.
     */
    public void addDatacenter(final DatacenterDigitalService datacenter) {
        datacenter.setDigitalService(this);
        this.datacenterDigitalServices.add(datacenter);
    }
}
