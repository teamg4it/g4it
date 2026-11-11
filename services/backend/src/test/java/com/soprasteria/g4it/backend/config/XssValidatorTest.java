package com.soprasteria.g4it.backend.config;

import com.soprasteria.g4it.backend.exception.G4itRestException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class XssValidatorTest {

    @Test
    void shouldReturnNullOrBlankUnchanged() {
        assertEquals(null, XssValidator.validate(null));
        assertEquals("  ", XssValidator.validate("  "));
    }

    @Test
    void shouldAllowPlainText() {
        assertEquals("Hello world", XssValidator.validate("Hello world"));
    }

    @Test
    void shouldAllowSafeHtml() {
        String safe = "<p>Hello <strong>world</strong></p>";
        assertEquals(safe, XssValidator.validate(safe));
    }

    @Test
    void shouldAllowSafeLink() {
        String safe = "<a href=\"https://example.com\">link</a>";
        assertEquals(safe, XssValidator.validate(safe));
    }

    @Test
    void shouldRejectTopLevelScriptTag() {
        assertThrows(G4itRestException.class,
                () -> XssValidator.validate("<script>alert(1)</script>"));
    }

    @Test
    void shouldRejectNestedScriptTag() {
        // Nested payload: top-level child count stays the same (<p> before/after),
        // only the inner <script> is removed. A previous naive implementation
        // based on comparing body().childNodeSize() missed this case.
        assertThrows(G4itRestException.class,
                () -> XssValidator.validate("<p><script>alert(1)</script></p>"));
    }

    @Test
    void shouldRejectOnErrorAttributePayload() {
        assertThrows(G4itRestException.class,
                () -> XssValidator.validate("<img src=x onerror=alert(1)>"));
    }

    @Test
    void shouldRejectJavascriptProtocolInLink() {
        // href protocol not in the safelist (http/https/mailto only)
        assertThrows(G4itRestException.class,
                () -> XssValidator.validate("<a href=\"javascript:alert(1)\">click</a>"));
    }

    @Test
    void shouldRejectDisallowedAttributeOnAllowedTag() {
        assertThrows(G4itRestException.class,
                () -> XssValidator.validate("<div onclick=\"alert(1)\">hi</div>"));
    }
}

