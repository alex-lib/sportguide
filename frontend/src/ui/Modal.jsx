/**
 * Modal - full-screen presentation with a Cancel / title bar and an optional
 * sticky footer (e.g. the primary submit button). Use for the create flow.
 */
const Modal = ({ open, title, cancelLabel = 'Отмена', onCancel, footer, children }) => {
  if (!open) return null;
  return (
    <div className="modal" role="dialog" aria-modal="true">
      <div className="modal-bar">
        <button type="button" onClick={onCancel}>
          {cancelLabel}
        </button>
        <span className="title">{title}</span>
        <span className="spacer" />
      </div>
      <div className="modal-body">{children}</div>
      {footer && <div className="modal-footer">{footer}</div>}
    </div>
  );
};

export default Modal;
