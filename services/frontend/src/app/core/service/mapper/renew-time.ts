export const parseToLocalDate = (dateStr: string): Date | null => {
    if (!dateStr) return null;

    // ISO format
    if (/^\d{4}-\d{2}-\d{2}/.test(dateStr)) {
        const d = new Date(dateStr);
        return new Date(d.getFullYear(), d.getMonth(), d.getDate());
    }

    // DD/MM/YYYY
    if (/^\d{2}\/\d{2}\/\d{4}$/.test(dateStr)) {
        const [day, month, year] = dateStr.split("/").map(Number);
        return new Date(year, month - 1, day);
    }

    return null;
};

export const subtractDays = (date: Date, days: number): Date => {
    const d = new Date(date);
    d.setDate(d.getDate() - days);

    return new Date(d.getFullYear(), d.getMonth(), d.getDate());
};

export const shouldShowExpiryMessage = (expiryDateStr: string): boolean => {
    const expiryDate = parseToLocalDate(expiryDateStr);
    if (!expiryDate) return false;

    // check for 30 days before expiry
    const reminderDate = subtractDays(expiryDate, 30);

    const today = new Date();
    const todayLocal = new Date(today.getFullYear(), today.getMonth(), today.getDate());

    return todayLocal >= reminderDate;
};
