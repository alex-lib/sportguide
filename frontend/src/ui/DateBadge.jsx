const MONTHS_RU = ['янв', 'фев', 'мар', 'апр', 'май', 'июн', 'июл', 'авг', 'сен', 'окт', 'ноя', 'дек'];

/**
 * DateBadge - compact day + short-month badge for event/training rows.
 * Falls back to a calendar glyph spot when the date can't be parsed.
 */
const DateBadge = ({ date }) => {
  let day = '—';
  let month = '';
  try {
    const d = new Date(date);
    if (!Number.isNaN(d.getTime())) {
      day = d.getDate();
      month = MONTHS_RU[d.getMonth()];
    }
  } catch {
    /* keep fallback */
  }
  return (
    <div className="date-badge">
      <span className="d">{day}</span>
      {month && <span className="m">{month}</span>}
    </div>
  );
};

export default DateBadge;
