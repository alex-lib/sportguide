import Icon from './Icon.jsx';

/** Pills - wrapping row of Pill tags. */
export const Pills = ({ children, className = '', ...rest }) => (
  <div className={`pills ${className}`.trim()} {...rest}>
    {children}
  </div>
);

/**
 * Pill - a single tag. tone: 'neutral' | 'brand' | 'success' | 'warning' | 'danger'.
 * Pass `icon` for a leading Lucide glyph. Brand-tinted pills carry primary info.
 */
export const Pill = ({ tone = 'neutral', icon, children, className = '', ...rest }) => {
  const toneClass = tone !== 'neutral' ? `pill-${tone}` : '';
  return (
    <span className={`pill ${toneClass} ${className}`.trim()} {...rest}>
      {icon && <Icon name={icon} size={13} />}
      {children}
    </span>
  );
};

/** MetaLine - a single icon + text line, e.g. an address. */
export const MetaLine = ({ icon = 'map-pin', children, className = '', ...rest }) => (
  <div className={`meta-line ${className}`.trim()} {...rest}>
    <Icon name={icon} size={15} />
    <span>{children}</span>
  </div>
);

export default Pills;
