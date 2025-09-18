/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.checkobject;

import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalServiceType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class DigitalServiceRuleFactory {

    @Autowired
    private NetworkDigitalServiceRule networkDigitalServiceRule;

    @Autowired
    private TerminalDigitalServiceRule terminalDigitalServiceRule;

    @Autowired
    private NonCloudDigitalServiceRule nonCloudDigitalServiceRule;

    private final Map<String, DigitalServiceRule> rules = new HashMap<>();

    @Autowired
    public void initializeRules() {
        rules.put(DigitalServiceType.NETWORK.getValue(), networkDigitalServiceRule);
        rules.put(DigitalServiceType.TERMINAL.getValue(), terminalDigitalServiceRule);
        rules.put(DigitalServiceType.SHARED_SERVER.getValue(), nonCloudDigitalServiceRule);
        rules.put(DigitalServiceType.DEDICATED_SERVER.getValue(), nonCloudDigitalServiceRule);
    }

    public DigitalServiceRule getRule(String digitalServiceType) {
        return rules.get(digitalServiceType);
    }
}
