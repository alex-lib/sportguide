import Icon from './Icon.jsx';

/**
 * EmptyState - rounded icon tile + title + message + optional action.
 * Name the active filters in `message` and offer a one-tap reset as `action`.
 */
export const EmptyState = ({ icon = 'search', accent = false, title, message, action }) => (
  <div className="empty">
    <div className={`ico ${accent ? 'accent' : ''}`.trim()}>
      <Icon name={icon} size={40} />
    </div>
    {title && <h3>{title}</h3>}
    {message && <p>{message}</p>}
    {action}
  </div>
);

/** ErrorBanner - inline error row. */
export const ErrorBanner = ({ children }) => (
  <div className="error-banner">
    <Icon name="triangle-alert" size={16} />
    <span>{children}</span>
  </div>
);

/** SkeletonList - content-shaped placeholders shown while loading. */
export const SkeletonList = ({ count = 4 }) => (
  <div>
    {Array.from({ length: count }).map((_, i) => (
      <div key={i} className="skel" />
    ))}
  </div>
);

/** Loading - branded full-screen boot indicator (mark + progress bar). */
export const Loading = ({ message = 'Готовим SportGuide…' }) => (
  <div className="boot">
    <div className="boot-mark">
      <Icon name="footprints" size={34} />
    </div>
    <div className="boot-bar">
      <i />
    </div>
    <p>{message}</p>
  </div>
);

export default EmptyState;
