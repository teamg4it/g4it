/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

import { Injectable, Renderer2, RendererFactory2 } from "@angular/core";

@Injectable({
    providedIn: "root",
})
export class MatomoScriptService {
    private readonly renderer: Renderer2;

    constructor(rendererFactory: RendererFactory2) {
        this.renderer = rendererFactory.createRenderer(null, null);
    }

    appendScriptToHead(matomoTagManagerUrl: string): void {
        const scriptContent = `
            var _mtm = (window._mtm = window._mtm || []);
            _mtm.push({ "mtm.startTime": new Date().getTime(), event: "mtm.Start" });
            (function () {
                var d = document,
                    g = d.createElement("script"),
                    s = d.getElementsByTagName("script")[0];
                g.async = true;
                g.src =
                    "${matomoTagManagerUrl}";
                s.parentNode.insertBefore(g, s);
            })();
        `;
        const script = this.renderer.createElement("script");
        script.type = "text/javascript";
        script.text = scriptContent;
        script.async = true;
        this.renderer.appendChild(document.head, script); // Appends to <head> of index.html
    }

    trackEvent(category: string, action: string, name?: string): void {
        if (globalThis.window === undefined) {
            console.warn("[Matomo] Window object not available");
            return;
        }

        if ((globalThis as any)._paq) {
            (globalThis as any)._paq.push(["trackEvent", category, action, name]);
        } else {
            console.warn(
                "[Matomo] Cannot track event - Direct tracker (_paq) not initialized",
                { category, action, name },
            );
        }
    }
}
