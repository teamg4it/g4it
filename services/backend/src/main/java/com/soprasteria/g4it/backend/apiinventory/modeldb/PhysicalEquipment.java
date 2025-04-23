/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiinventory.modeldb;

import com.soprasteria.g4it.backend.common.dbmodel.AbstractValidationBaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
@NoArgsConstructor
@Entity
@SuperBuilder
@Table(name = "equipement_physique")
public class PhysicalEquipment extends AbstractValidationBaseEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(max = 255, message = "{nomequipementphysique.size}")
    @NotBlank(message = "{nomequipementphysique.not.blank}")
    private String nomEquipementPhysique;

    @Size(max = 255, message = "{nomentite.size}")
    private String nomEntite;

    @Size(max = 255, message = "{nomsourcedonnee.size}")
    private String nomSourceDonnee;

    @Size(max = 255, message = "{modele.size}")
    private String modele;

    @Pattern(regexp = "^[1-9][\\d]*$", message = "{quantite.pattern}")
    @NotBlank(message = "{quantite.not.blank}")
    private String quantite;

    @NotBlank(message = "{type.not.blank}")
    @Size(max = 255, message = "{type.size}")
    private String type;

    @Size(max = 255, message = "{statut.size}")
    private String statut;

    @Size(max = 255, message = "{paysdutilisation.size}")
    @NotBlank(message = "{paysdutilisation.not.blank}")
    @Column(name = "pays_utilisation")
    private String paysDUtilisation;

    @Size(max = 255, message = "{utilisateur.size}")
    private String utilisateur;

    @Pattern(regexp = "^\\d{4}\\-(0?[1-9]|1[012])\\-(0?[1-9]|[12]\\d|3[01])$|", message = "{dateachat.pattern}")
    private String dateAchat;

    @Pattern(regexp = "^\\d{4}\\-(0?[1-9]|1[012])\\-(0?[1-9]|[12]\\d|3[01])$|", message = "{dateretrait.pattern}")
    private String dateRetrait;

    @Pattern(regexp = "\\d*", message = "{nbcoeur.pattern}")
    private String nbCoeur;

    @Pattern(regexp = "^\\+?(\\.\\d+|\\d+\\.?\\d*)$|", message = "{consoelecannuelle.pattern}")
    private String consoElecAnnuelle;

    @Size(max = 255, message = "{nomcourtdatacenter.size}")
    private String nomCourtDatacenter;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventoryId", referencedColumnName = "inventoryId", insertable = false, updatable = false)
    @JoinColumn(name = "nomCourtDatacenter", referencedColumnName = "nomCourtDatacenter", insertable = false, updatable = false)
    private DataCenter datacenter;

    @Size(max = 255, message = "{fabricant.size}")
    private String fabricant;

    @Pattern(regexp = "\\d*", message = "{tailledudisque.pattern}")
    private String tailleDuDisque;

    @Pattern(regexp = "\\d*", message = "{taillememoire.pattern}")
    private String tailleMemoire;

    @Size(max = 255, message = "{typedeprocesseur.size}")
    private String typeDeProcesseur;
}
