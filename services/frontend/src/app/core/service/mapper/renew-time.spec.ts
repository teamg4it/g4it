import { parseToLocalDate, shouldShowExpiryMessage, subtractDays } from "./renew-time";

describe("renew-time utilities", () => {
    describe("parseToLocalDate", () => {
        it("should return null for empty string", () => {
            expect(parseToLocalDate("")).toBeNull();
        });

        it("should return null for null input", () => {
            expect(parseToLocalDate(null as any)).toBeNull();
        });

        it("should return null for undefined input", () => {
            expect(parseToLocalDate(undefined as any)).toBeNull();
        });

        it("should parse ISO format date string (YYYY-MM-DD)", () => {
            const result = parseToLocalDate("2026-05-15");
            expect(result).not.toBeNull();
            expect(result!.getFullYear()).toBe(2026);
            expect(result!.getMonth()).toBe(4); // 0-indexed
            expect(result!.getDate()).toBe(15);
        });

        it("should parse ISO format with time component", () => {
            const result = parseToLocalDate("2026-12-25T10:30:00Z");
            expect(result).not.toBeNull();
            expect(result!.getFullYear()).toBe(2026);
            expect(result!.getMonth()).toBe(11); // December
            expect(result!.getDate()).toBe(25);
        });

        it("should parse DD/MM/YYYY format", () => {
            const result = parseToLocalDate("15/05/2026");
            expect(result).not.toBeNull();
            expect(result!.getFullYear()).toBe(2026);
            expect(result!.getMonth()).toBe(4); // 0-indexed
            expect(result!.getDate()).toBe(15);
        });

        it("should parse DD/MM/YYYY with single digit day/month", () => {
            const result = parseToLocalDate("05/01/2026");
            expect(result).not.toBeNull();
            expect(result!.getFullYear()).toBe(2026);
            expect(result!.getMonth()).toBe(0); // January
            expect(result!.getDate()).toBe(5);
        });

        it("should return null for invalid format", () => {
            expect(parseToLocalDate("invalid-date")).toBeNull();
        });

        it("should return null for MM/DD/YYYY format (not supported)", () => {
            expect(parseToLocalDate("05-15-2026")).toBeNull();
        });

        it("should return null for date with wrong separator", () => {
            expect(parseToLocalDate("15-05-2026")).toBeNull();
        });

        it("should return null for incomplete date", () => {
            expect(parseToLocalDate("15/05")).toBeNull();
        });

        it("should strip time component from ISO dates", () => {
            const result = parseToLocalDate("2026-05-15T14:30:00");
            expect(result!.getHours()).toBe(0);
            expect(result!.getMinutes()).toBe(0);
            expect(result!.getSeconds()).toBe(0);
        });

        it("should strip time component from DD/MM/YYYY dates", () => {
            const result = parseToLocalDate("15/05/2026");
            expect(result!.getHours()).toBe(0);
            expect(result!.getMinutes()).toBe(0);
            expect(result!.getSeconds()).toBe(0);
        });
    });

    describe("subtractDays", () => {
        it("should subtract days from a date", () => {
            const date = new Date(2026, 4, 15); // May 15, 2026
            const result = subtractDays(date, 5);
            expect(result.getFullYear()).toBe(2026);
            expect(result.getMonth()).toBe(4);
            expect(result.getDate()).toBe(10);
        });

        it("should subtract zero days", () => {
            const date = new Date(2026, 4, 15);
            const result = subtractDays(date, 0);
            expect(result.getFullYear()).toBe(2026);
            expect(result.getMonth()).toBe(4);
            expect(result.getDate()).toBe(15);
        });

        it("should handle month boundary", () => {
            const date = new Date(2026, 5, 3); // June 3, 2026
            const result = subtractDays(date, 5);
            expect(result.getFullYear()).toBe(2026);
            expect(result.getMonth()).toBe(4); // May
            expect(result.getDate()).toBe(29);
        });

        it("should handle year boundary", () => {
            const date = new Date(2026, 0, 5); // January 5, 2026
            const result = subtractDays(date, 10);
            expect(result.getFullYear()).toBe(2025);
            expect(result.getMonth()).toBe(11); // December
            expect(result.getDate()).toBe(26);
        });

        it("should handle leap year", () => {
            const date = new Date(2024, 2, 1); // March 1, 2024 (leap year)
            const result = subtractDays(date, 1);
            expect(result.getFullYear()).toBe(2024);
            expect(result.getMonth()).toBe(1); // February
            expect(result.getDate()).toBe(29); // Leap day
        });

        it("should strip time component from result", () => {
            const date = new Date(2026, 4, 15, 14, 30, 45);
            const result = subtractDays(date, 5);
            expect(result.getHours()).toBe(0);
            expect(result.getMinutes()).toBe(0);
            expect(result.getSeconds()).toBe(0);
        });

        it("should not mutate original date", () => {
            const date = new Date(2026, 4, 15);
            const originalDate = date.getDate();
            subtractDays(date, 5);
            expect(date.getDate()).toBe(originalDate);
        });

        it("should handle subtracting 30 days", () => {
            const date = new Date(2026, 4, 30);
            const result = subtractDays(date, 30);
            expect(result.getFullYear()).toBe(2026);
            expect(result.getMonth()).toBe(3); // April
            expect(result.getDate()).toBe(30);
        });

        it("should handle subtracting large number of days", () => {
            const date = new Date(2026, 4, 15);
            const result = subtractDays(date, 365);
            expect(result.getFullYear()).toBe(2025);
            expect(result.getMonth()).toBe(4);
            expect(result.getDate()).toBe(15);
        });
    });

    describe("shouldShowExpiryMessage", () => {
        let originalDate: Date;

        beforeEach(() => {
            // Mock today's date to May 4, 2026
            originalDate = new Date();
            jasmine.clock().install();
            jasmine.clock().mockDate(new Date(2026, 4, 4)); // May 4, 2026
        });

        afterEach(() => {
            jasmine.clock().uninstall();
        });

        it("should return false for empty string", () => {
            expect(shouldShowExpiryMessage("")).toBe(false);
        });

        it("should return false for null input", () => {
            expect(shouldShowExpiryMessage(null as any)).toBe(false);
        });

        it("should return false for invalid date string", () => {
            expect(shouldShowExpiryMessage("invalid-date")).toBe(false);
        });

        it("should return false when expiry is more than 30 days away", () => {
            // Expiry: June 10, 2026 (37 days away)
            expect(shouldShowExpiryMessage("2026-06-10")).toBe(false);
        });

        it("should return true when expiry is exactly 30 days away", () => {
            // Expiry: June 3, 2026 (30 days away from May 4)
            expect(shouldShowExpiryMessage("2026-06-03")).toBe(true);
        });

        it("should return true when expiry is within 30 days", () => {
            // Expiry: May 20, 2026 (16 days away)
            expect(shouldShowExpiryMessage("2026-05-20")).toBe(true);
        });

        it("should return true when expiry is today", () => {
            // Expiry: May 4, 2026 (today)
            expect(shouldShowExpiryMessage("2026-05-04")).toBe(true);
        });

        it("should return true when expiry has passed", () => {
            // Expiry: April 1, 2026 (past date)
            expect(shouldShowExpiryMessage("2026-04-01")).toBe(true);
        });

        it("should handle DD/MM/YYYY format for expiry date", () => {
            // Expiry: June 3, 2026 (30 days away)
            expect(shouldShowExpiryMessage("03/06/2026")).toBe(true);
        });

        it("should return true when expiry is 29 days away", () => {
            // Expiry: June 2, 2026 (29 days away)
            expect(shouldShowExpiryMessage("2026-06-02")).toBe(true);
        });

        it("should return false when expiry is 31 days away", () => {
            // Expiry: June 4, 2026 (31 days away)
            expect(shouldShowExpiryMessage("2026-06-04")).toBe(false);
        });

        it("should handle ISO date with time component", () => {
            // Expiry: May 20, 2026 with time (should ignore time)
            expect(shouldShowExpiryMessage("2026-05-20T23:59:59")).toBe(true);
        });

        it("should return true for expiry exactly 1 day away", () => {
            // Expiry: May 5, 2026 (tomorrow)
            expect(shouldShowExpiryMessage("2026-05-05")).toBe(true);
        });

        it("should handle month boundary correctly", () => {
            jasmine.clock().mockDate(new Date(2026, 4, 31)); // May 31, 2026
            // Expiry: July 1, 2026 (31 days away)
            expect(shouldShowExpiryMessage("2026-07-01")).toBe(false);
            // Expiry: June 30, 2026 (30 days away)
            expect(shouldShowExpiryMessage("2026-06-30")).toBe(true);
        });

        it("should handle year boundary correctly", () => {
            jasmine.clock().mockDate(new Date(2026, 11, 15)); // December 15, 2026
            // Expiry: January 20, 2027 (36 days away)
            expect(shouldShowExpiryMessage("2027-01-20")).toBe(false);
            // Expiry: January 14, 2027 (30 days away)
            expect(shouldShowExpiryMessage("2027-01-14")).toBe(true);
        });
    });
});
