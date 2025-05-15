/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { RouterModule, Routes } from "@angular/router";
import { TitleResolver } from "../common/title-resolver.service";
import { EcoMindAiComponent } from "./eco-mind-ai.component";
import { ShareDigitalServiceComponent } from "./share-eco-mind-ai/share-eco-mind-ai.component";

const routes: Routes = [
    {
        path: "",
        component: EcoMindAiComponent,
        resolve: {
            title: TitleResolver,
        },
        data: {
            titleKey: "digital-services.page-title",
        },
    },
    {
        path: ":id/share/:generatedId",
        component: ShareDigitalServiceComponent,
    },
];

export const EcoMindAiRouter = RouterModule.forChild(routes);
