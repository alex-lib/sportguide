/** Form primitives. Controls forward all native props. */

export const Field = ({ label, children, className = '', ...rest }) => (
  <div className={`field ${className}`.trim()} {...rest}>
    {label && <label>{label}</label>}
    {children}
  </div>
);

export const FieldRow = ({ children, className = '', ...rest }) => (
  <div className={`field-row ${className}`.trim()} {...rest}>
    {children}
  </div>
);

export const Input = ({ className = '', ...rest }) => (
  <input className={`input ${className}`.trim()} {...rest} />
);

export const Textarea = ({ className = '', ...rest }) => (
  <textarea className={`textarea ${className}`.trim()} {...rest} />
);

export const Select = ({ className = '', children, ...rest }) => (
  <select className={`select ${className}`.trim()} {...rest}>
    {children}
  </select>
);

/** Range - slider with a live value readout. */
export const Range = ({ value, min = 0, max = 100, suffix = '', placeholder = '—', onChange, ...rest }) => (
  <div className="range-wrap">
    <input
      type="range"
      className="range"
      min={min}
      max={max}
      value={value ?? min}
      onChange={onChange}
      {...rest}
    />
    <span className="range-value">{value != null && value !== '' ? `${value}${suffix}` : placeholder}</span>
  </div>
);

/** Segmented - mutually-exclusive options (incl. an explicit default). */
export const Segmented = ({ options = [], value, onChange }) => (
  <div className="segmented">
    {options.map((opt) => (
      <button
        key={String(opt.value)}
        type="button"
        className={value === opt.value ? 'active' : ''}
        onClick={() => onChange(opt.value)}
      >
        {opt.label}
      </button>
    ))}
  </div>
);

export default Field;
