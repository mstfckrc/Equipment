const DAY_MS = 24 * 60 * 60 * 1000;

export function formatLocalDate(date: Date) {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
}

export function parseLocalDate(value: string) {
  const [year, month, day] = value.split("-").map(Number);
  return new Date(year, month - 1, day, 12);
}

export function addCalendarDays(value: string, days: number) {
  const date = parseLocalDate(value);
  date.setDate(date.getDate() + days);
  return formatLocalDate(date);
}

export function calendarDayDifference(from: string, to: string) {
  const fromParts = from.split("-").map(Number);
  const toParts = to.split("-").map(Number);
  return (Date.UTC(toParts[0], toParts[1] - 1, toParts[2])
    - Date.UTC(fromParts[0], fromParts[1] - 1, fromParts[2])) / DAY_MS;
}

export function todayInIstanbul() {
  const parts = new Intl.DateTimeFormat("en-CA", {
    timeZone: "Europe/Istanbul",
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
  }).formatToParts(new Date());
  const values = Object.fromEntries(parts.map((part) => [part.type, part.value]));
  return `${values.year}-${values.month}-${values.day}`;
}

export function formatDate(value: string) {
  const [year, month, day] = value.split("-").map(Number);
  return new Intl.DateTimeFormat("tr-TR", { dateStyle: "long", timeZone: "UTC" })
    .format(new Date(Date.UTC(year, month - 1, day, 12)));
}
