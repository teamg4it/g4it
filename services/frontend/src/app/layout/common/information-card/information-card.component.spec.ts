/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { ComponentRef } from "@angular/core";
import { ComponentFixture, TestBed } from "@angular/core/testing";
import { DomSanitizer, SafeHtml } from "@angular/platform-browser";
import { InformationCardComponent } from "./information-card.component";

describe("InformationCardComponent", () => {
    let component: InformationCardComponent;
    let fixture: ComponentFixture<InformationCardComponent>;
    let componentRef: ComponentRef<InformationCardComponent>;
    let sanitizer: DomSanitizer;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
    imports: [InformationCardComponent],
}).compileComponents();

        fixture = TestBed.createComponent(InformationCardComponent);
        component = fixture.componentInstance;
        componentRef = fixture.componentRef;
        sanitizer = TestBed.inject(DomSanitizer);
        fixture.detectChanges();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });

    describe("Input signals", () => {
        it("should initialize with undefined title and content", () => {
            expect(component.title()).toBeUndefined();
            expect(component.content()).toBeUndefined();
        });

        it("should set title input signal", () => {
            const titleValue = "Test Title";
            componentRef.setInput("title", titleValue);
            fixture.detectChanges();

            expect(component.title()).toBe(titleValue);
        });

        it("should set content input signal", () => {
            const contentValue = "Test content text";
            componentRef.setInput("content", contentValue);
            fixture.detectChanges();

            expect(component.content()).toBe(contentValue);
        });

        it("should update title input signal when changed", () => {
            componentRef.setInput("title", "Initial Title");
            fixture.detectChanges();
            expect(component.title()).toBe("Initial Title");

            componentRef.setInput("title", "Updated Title");
            fixture.detectChanges();
            expect(component.title()).toBe("Updated Title");
        });

        it("should update content input signal when changed", () => {
            componentRef.setInput("content", "Initial content");
            fixture.detectChanges();
            expect(component.content()).toBe("Initial content");

            componentRef.setInput("content", "Updated content");
            fixture.detectChanges();
            expect(component.content()).toBe("Updated content");
        });

        it("should handle empty string for title", () => {
            componentRef.setInput("title", "");
            fixture.detectChanges();

            expect(component.title()).toBe("");
        });

        it("should handle empty string for content", () => {
            componentRef.setInput("content", "");
            fixture.detectChanges();

            expect(component.content()).toBe("");
        });
    });

    describe("Computed signals", () => {
        describe("safeTitle", () => {
            it("should sanitize title HTML content", () => {
                const htmlTitle = "<strong>Bold Title</strong>";
                componentRef.setInput("title", htmlTitle);
                fixture.detectChanges();

                const result = component.safeTitle();
                expect(result).toBeTruthy();
                expect(typeof result).toBe("object");
            });

            it("should return empty string when title is undefined", () => {
                componentRef.setInput("title", undefined);
                fixture.detectChanges();

                const result = component.safeTitle();
                expect(result).toBe("");
            });

            it("should return empty string when title is empty string", () => {
                componentRef.setInput("title", "");
                fixture.detectChanges();

                const result = component.safeTitle();
                expect(result).toBe("");
            });

            it("should update when title changes", () => {
                componentRef.setInput("title", "<h1>First</h1>");
                fixture.detectChanges();
                const firstResult = component.safeTitle();

                componentRef.setInput("title", "<h2>Second</h2>");
                fixture.detectChanges();
                const secondResult = component.safeTitle();

                expect(firstResult).not.toBe(secondResult);
            });

            it("should handle HTML with special characters", () => {
                const htmlTitle = "<p>Price: €100 & £80</p>";
                componentRef.setInput("title", htmlTitle);
                fixture.detectChanges();

                const result = component.safeTitle();
                expect(result).toBeTruthy();
            });
        });

        describe("safeContent", () => {
            it("should sanitize content HTML", () => {
                const htmlContent = "<p>This is <em>content</em></p>";
                componentRef.setInput("content", htmlContent);
                fixture.detectChanges();

                const result = component.safeContent();
                expect(result).toBeTruthy();
                expect(typeof result).toBe("object");
            });

            it("should return empty string when content is undefined", () => {
                componentRef.setInput("content", undefined);
                fixture.detectChanges();

                const result = component.safeContent();
                expect(result).toBe("");
            });

            it("should return empty string when content is empty string", () => {
                componentRef.setInput("content", "");
                fixture.detectChanges();

                const result = component.safeContent();
                expect(result).toBe("");
            });

            it("should update when content changes", () => {
                componentRef.setInput("content", "<div>First content</div>");
                fixture.detectChanges();
                const firstResult = component.safeContent();

                componentRef.setInput("content", "<div>Second content</div>");
                fixture.detectChanges();
                const secondResult = component.safeContent();

                expect(firstResult).not.toBe(secondResult);
            });

            it("should handle complex HTML structures", () => {
                const htmlContent = `
                    <div>
                        <h1>Title</h1>
                        <ul>
                            <li>Item 1</li>
                            <li>Item 2</li>
                        </ul>
                    </div>
                `;
                componentRef.setInput("content", htmlContent);
                fixture.detectChanges();

                const result = component.safeContent();
                expect(result).toBeTruthy();
            });

            it("should handle HTML with attributes", () => {
                const htmlContent =
                    '<a href="https://example.com" target="_blank">Link</a>';
                componentRef.setInput("content", htmlContent);
                fixture.detectChanges();

                const result = component.safeContent();
                expect(result).toBeTruthy();
            });
        });
    });

    describe("renderHTML method", () => {
        it("should return SafeHtml when HTML string is provided", () => {
            const html = "<div>Test HTML</div>";
            const result = component.renderHTML(html);

            expect(result).toBeTruthy();
            expect(typeof result).toBe("object");
        });

        it("should return empty string when undefined is provided", () => {
            const result = component.renderHTML(undefined);

            expect(result).toBe("");
        });

        it("should sanitize HTML using DomSanitizer", () => {
            const html = "<script>alert('test')</script><p>Content</p>";
            spyOn(sanitizer, "bypassSecurityTrustHtml").and.callThrough();

            component.renderHTML(html);

            expect(sanitizer.bypassSecurityTrustHtml).toHaveBeenCalledWith(html);
        });

        it("should handle plain text", () => {
            const plainText = "Just plain text";
            const result = component.renderHTML(plainText);

            expect(result).toBeTruthy();
        });

        it("should handle HTML with inline styles", () => {
            const html = '<p style="color: red;">Styled text</p>';
            const result = component.renderHTML(html);

            expect(result).toBeTruthy();
        });

        it("should handle nested HTML elements", () => {
            const html = "<div><span><strong>Nested</strong></span></div>";
            const result = component.renderHTML(html);

            expect(result).toBeTruthy();
        });

        it("should handle HTML entities", () => {
            const html = "&lt;div&gt;Escaped HTML&lt;/div&gt;";
            const result = component.renderHTML(html);

            expect(result).toBeTruthy();
        });

        it("should handle multi-line HTML", () => {
            const html = `
                <div>
                    <p>Line 1</p>
                    <p>Line 2</p>
                </div>
            `;
            const result = component.renderHTML(html);

            expect(result).toBeTruthy();
        });
    });

    describe("DomSanitizer integration", () => {
        it("should inject DomSanitizer", () => {
            expect(component["sanitizer"]).toBeTruthy();
            expect(component["sanitizer"]).toBe(sanitizer);
        });

        it("should use sanitizer in renderHTML", () => {
            const html = "<b>Bold</b>";
            spyOn(sanitizer, "bypassSecurityTrustHtml").and.returnValue({} as SafeHtml);

            component.renderHTML(html);

            expect(sanitizer.bypassSecurityTrustHtml).toHaveBeenCalledTimes(1);
            expect(sanitizer.bypassSecurityTrustHtml).toHaveBeenCalledWith(html);
        });

        it("should not call sanitizer when html is undefined", () => {
            spyOn(sanitizer, "bypassSecurityTrustHtml");

            component.renderHTML(undefined);

            expect(sanitizer.bypassSecurityTrustHtml).not.toHaveBeenCalled();
        });
    });

    describe("Component lifecycle", () => {
        it("should initialize computed signals on creation", () => {
            const newFixture = TestBed.createComponent(InformationCardComponent);
            const newComponent = newFixture.componentInstance;

            expect(newComponent.safeTitle()).toBe("");
            expect(newComponent.safeContent()).toBe("");
        });

        it("should react to input changes through computed signals", () => {
            componentRef.setInput("title", "<h1>Title</h1>");
            componentRef.setInput("content", "<p>Content</p>");
            fixture.detectChanges();

            const titleResult = component.safeTitle();
            const contentResult = component.safeContent();

            expect(titleResult).toBeTruthy();
            expect(contentResult).toBeTruthy();
        });
    });

    describe("Edge cases", () => {
        it("should handle null-like values in title", () => {
            componentRef.setInput("title", null as any);
            fixture.detectChanges();

            const result = component.safeTitle();
            expect(result).toBe("");
        });

        it("should handle null-like values in content", () => {
            componentRef.setInput("content", null as any);
            fixture.detectChanges();

            const result = component.safeContent();
            expect(result).toBe("");
        });

        it("should handle very long HTML strings", () => {
            const longHtml = "<p>" + "a".repeat(10000) + "</p>";
            const result = component.renderHTML(longHtml);

            expect(result).toBeTruthy();
        });

        it("should handle HTML with multiple nested levels", () => {
            const nestedHtml =
                "<div><div><div><div><p>Deep nesting</p></div></div></div></div>";
            const result = component.renderHTML(nestedHtml);

            expect(result).toBeTruthy();
        });

        it("should handle HTML with unicode characters", () => {
            const unicodeHtml = "<p>Hello 世界 🌍</p>";
            const result = component.renderHTML(unicodeHtml);

            expect(result).toBeTruthy();
        });

        it("should handle malformed HTML gracefully", () => {
            const malformedHtml = "<div><p>Unclosed tags";
            const result = component.renderHTML(malformedHtml);

            expect(result).toBeTruthy();
        });

        it("should handle whitespace-only strings", () => {
            const whitespace = "   \n\t  ";
            const result = component.renderHTML(whitespace);

            expect(result).toBeTruthy();
        });
    });

    describe("Multiple inputs interaction", () => {
        it("should handle both title and content set simultaneously", () => {
            componentRef.setInput("title", "<h1>Title</h1>");
            componentRef.setInput("content", "<p>Content</p>");
            fixture.detectChanges();

            expect(component.title()).toBe("<h1>Title</h1>");
            expect(component.content()).toBe("<p>Content</p>");
            expect(component.safeTitle()).toBeTruthy();
            expect(component.safeContent()).toBeTruthy();
        });

        it("should update independently", () => {
            componentRef.setInput("title", "<h1>Original Title</h1>");
            componentRef.setInput("content", "<p>Original Content</p>");
            fixture.detectChanges();

            const originalSafeTitle = component.safeTitle();

            componentRef.setInput("content", "<p>Updated Content</p>");
            fixture.detectChanges();

            expect(component.safeTitle()).toBe(originalSafeTitle);
            expect(component.content()).toBe("<p>Updated Content</p>");
        });
    });
});
