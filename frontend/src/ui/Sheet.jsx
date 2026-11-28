import { useEffect, useId, useRef } from 'react';

/** Bottom sheet with a scrolling body and always-visible actions. */
export const Sheet = ({ open, title, onClose, footer, children }) => {
  const panel = useRef(null);
  const titleId = useId();
  const close = useRef(onClose);
  close.current = onClose;

  useEffect(() => {
    if (!open) return;
    const previous = document.activeElement;
    const overflow = document.body.style.overflow;
    document.body.style.overflow = 'hidden';
    const focusable = () => [...panel.current.querySelectorAll('button:not(:disabled), a[href], input:not(:disabled), select:not(:disabled), textarea:not(:disabled), [tabindex="0"]')];
    (focusable()[0] || panel.current).focus();
    const onKeyDown = event => {
      if (event.key === 'Escape') { event.preventDefault(); close.current(); }
      if (event.key !== 'Tab') return;
      const items = focusable();
      const first = items[0], last = items.at(-1);
      if (!first) { event.preventDefault(); return; }
      if (event.shiftKey && (document.activeElement === first || document.activeElement === panel.current)) {
        event.preventDefault(); last.focus();
      } else if (!event.shiftKey && document.activeElement === last) {
        event.preventDefault(); first.focus();
      }
    };
    document.addEventListener('keydown', onKeyDown);
    return () => {
      document.removeEventListener('keydown', onKeyDown);
      document.body.style.overflow = overflow;
      if (previous?.isConnected) previous.focus();
    };
  }, [open]);

  if (!open) return null;
  return (
    <div className="scrim" onClick={onClose}>
      <div className="sheet" ref={panel} tabIndex={-1} onClick={(e) => e.stopPropagation()}
        role="dialog" aria-modal="true" aria-labelledby={title ? titleId : undefined}>
        <div className="sheet-grabber" />
        {title && <h2 className="sheet-title" id={titleId}>{title}</h2>}
        <div className="sheet-content">{children}</div>
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
