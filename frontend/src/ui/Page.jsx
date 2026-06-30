import Icon from './Icon.jsx';

/** Page - scrolling screen body, padded to clear the floating tab bar. */
export const Page = ({ children, className = '', ...rest }) => (
  <div className={`page-body ${className}`.trim()} {...rest}>
    {children}
  </div>
);

/**
 * PageHeader - large-title header with optional eyebrow, an action slot
 * (e.g. an IconButton) and arbitrary children below the title (search, chips).
 */
export const PageHeader = ({ eyebrow, eyebrowIcon, title, action, children }) => (
  <div className="page-header">
    <div className="nav-row">
      <div>
        {eyebrow && (
          <p className="eyebrow">
            {eyebrowIcon && <Icon name={eyebrowIcon} size={14} />}
            {eyebrow}
          </p>
        )}
        <h1 className="page-title">{title}</h1>
      </div>
      {action}
    </div>
    {children}
  </div>
);

export const SectionLabel = ({ children }) => <div className="section-label">{children}</div>;

/** IconButton - round tinted button holding a single icon. */
export const IconButton = ({ icon, label, className = '', ...rest }) => (
  <button type="button" className={`icon-btn ${className}`.trim()} aria-label={label} {...rest}>
    <Icon name={icon} size={17} />
  </button>
);

export default Page;
