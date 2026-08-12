// Formats a Date (or a value accepted by the Date constructor) as a local
// calendar date string "YYYY-MM-DD". Unlike Date#toISOString, this uses the
// user's local timezone, so it never rolls back a day in positive-offset
// zones (e.g. Voronezh, MSK/UTC+3).
export function toLocalISODate(value = new Date()) {
    const d = value instanceof Date ? value : new Date(value);
    const year = d.getFullYear();
    const month = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
}