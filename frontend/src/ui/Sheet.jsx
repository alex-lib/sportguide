/**
 * Sheet - bottom sheet with a grabber. Tapping the scrim closes it.
 * `footer` renders a sticky action row (e.g. Reset / Apply).
 */
export const Sheet = ({ open, title, onClose, footer, children }) => {
  if (!open) return null;
  return (
    <div className="scrim" onClick={onClose}>
      <div className="sheet" onClick={(e) => e.stopPropagation()} role="dialog" aria-modal="true">
        <div className="sheet-grabber" />
        {title && <h2 className="sheet-title">{title}</h2>}
        {children}
        {footer && <div className="sheet-actions">{footer}</div>}
      </div>
    </div>
  );
};

export const SheetSection = ({ title, children }) => (
  <div className="sheet-section">
    {title && <div className="sheet-section-title">{title}</div>}
    {children}
  </div>
);

export default Sheet;
