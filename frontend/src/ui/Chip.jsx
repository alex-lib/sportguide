import Icon from './Icon.jsx';

/** FilterChip - a single quick-filter pill (chip rail / sheet). */
export const FilterChip = ({ active = false, icon, dot = false, className = '', children, ...rest }) => (
  <button
    type="button"
    className={`filter-chip ${active ? 'active' : ''} ${className}`.trim()}
    {...rest}
  >
    {icon && <Icon name={icon} size={14} />}
    {dot && <span className="dot" />}
    {children}
  </button>
);

/** ChipRail - horizontally scrolling row of FilterChips. */
export const ChipRail = ({ children, className = '', ...rest }) => (
  <div className={`chip-rail ${className}`.trim()} {...rest}>
    {children}
  </div>
);

/** SearchField - static-friendly search input with a leading icon. */
export const SearchField = ({ placeholder = 'Поиск', value, onChange, readOnly, onClick }) => (
  <div className="search-field" onClick={onClick}>
    <Icon name="search" size={18} />
    <input
      type="search"
      placeholder={placeholder}
      value={value}
      onChange={onChange}
      readOnly={readOnly}
    />
  </div>
);

export default FilterChip;
