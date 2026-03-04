package com.soprasteria.g4it.backend.config;

import com.soprasteria.g4it.backend.exception.G4itRestException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;

public final class XssValidator {

    private static final Safelist SAFE_HTML_SAFELIST = new Safelist()

            // ===== Text & formatting =====
            .addTags(
                    "p", "br", "span", "div",
                    "b", "strong", "i", "em", "u",
                    "s", "strike", "del", "ins",
                    "small", "sub", "sup",
                    "code", "pre", "kbd", "var", "samp"
            )

            // ===== Headings =====
            .addTags(
                    "h1", "h2", "h3",
                    "h4", "h5", "h6"
            )

            // ===== Lists =====
            .addTags(
                    "ul", "ol", "li",
                    "dl", "dt", "dd"
            )

            // ===== Sections / blocks =====
            .addTags(
                    "section", "article", "header",
                    "footer", "main", "nav",
                    "aside", "blockquote"
            )

            // ===== Tables =====
            .addTags(
                    "table", "thead", "tbody", "tfoot",
                    "tr", "th", "td", "caption",
                    "colgroup", "col"
            )

            // ===== Links =====
            .addTags("a")
            .addAttributes(
                    "a",
                    "href", "target", "rel", "title"
            )
            .addProtocols(
                    "a",
                    "href",
                    "http", "https", "mailto"
            )

            // ===== Media containers =====
            .addTags("figure", "figcaption")

            // ===== Misc =====
            .addTags(
                    "hr", "abbr", "cite",
                    "q", "time", "address"
            )

            // ===== Global safe attributes =====
            .addAttributes(
                    ":all",
                    "class",
                    "title",
                    "aria-label",
                    "aria-hidden",
                    "role"
            );

    private XssValidator() {
        // Utility class
    }

    public static String validate(String value) {

        if (value == null || value.trim().isEmpty()) {
            return value;
        }

        String cleaned = Jsoup.clean(value, SAFE_HTML_SAFELIST);

        Document inputDoc = Jsoup.parseBodyFragment(value);
        Document cleanedDoc = Jsoup.parseBodyFragment(cleaned);

        // If Jsoup removed nodes, unsafe content was present
        if (cleanedDoc.body().childNodeSize()
                < inputDoc.body().childNodeSize()) {

            throw new G4itRestException(
                    "400",
                    "Invalid input detected. Unsafe HTML or script content is not allowed"
            );
        }

        return value;
    }
}