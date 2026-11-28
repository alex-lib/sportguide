// The existing API returns a Markdown link; accept plain usernames as well.
export const coachTelegram = (value) => {
  if (typeof value !== 'string') return null;
  const text = value.trim();
  const username = text.match(/^@?([A-Za-z0-9_]{5,32})$/)?.[1]
    || text.match(/^https:\/\/t\.me\/([A-Za-z0-9_]{5,32})\/?$/)?.[1]
    || text.match(/^\[@([A-Za-z0-9_]{5,32})\]\(https:\/\/t\.me\/\1\)$/)?.[1];
  return username ? { username, url: `https://t.me/${username}` } : null;
};
