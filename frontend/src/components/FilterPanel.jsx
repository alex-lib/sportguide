import { useState } from 'react';
import '../App.css';

const FilterPanel = ({ filters, onFilterChange, onReset }) => {
  const [isOpen, setIsOpen] = useState(false);

  const renderFilterSection = (title, children) => (
    <div className="filter-section">
      <div className="filter-section-title">{title}</div>
      {children}
    </div>
  );

  const renderChipFilter = (options, selectedValue, onChange, key) => (
    <div className="filter-chips">
      {options.map((option) => (
        <button
          key={option.value}
          type="button"
          className={`filter-chip ${selectedValue === option.value ? 'active' : ''}`}
          onClick={() => onChange(option.value === selectedValue ? null : option.value)}
        >
          {option.label}
        </button>
      ))}
    </div>
  );

  const renderNumberRange = (label, value, onChange, min = 0, max = 100) => (
    <div className="filter-number-input">
      <label style={{ fontSize: '14px', fontWeight: '500', color: 'var(--text-secondary)' }}>
        {label}:
      </label>
      <input
        type="number"
        min={min}
        max={max}
        value={value || ''}
        onChange={(e) => onChange(e.target.value ? parseInt(e.target.value) : null)}
        placeholder={`${min}-${max}`}
      />
    </div>
  );

  const renderMultiSelect = (options, selectedValues, onChange) => (
    <div className="filter-chips">
      {options.map((option) => {
        const isSelected = selectedValues?.includes(option.value);
        return (
          <button
            key={option.value}
            type="button"
            className={`filter-chip ${isSelected ? 'active' : ''}`}
            onClick={() => {
              const newValues = selectedValues || [];
              if (isSelected) {
                onChange(newValues.filter((v) => v !== option.value));
              } else {
                onChange([...newValues, option.value]);
              }
            }}
          >
            {option.label}
          </button>
        );
      })}
    </div>
  );

  if (!filters || filters.length === 0) {
    return null;
  }

  // Check if any filters are active
  const hasActiveFilters = filters.some((filter) => {
    if (filter.type === 'multiselect') {
      return filter.value && filter.value.length > 0;
    }
    if (filter.type === 'number') {
      return filter.value !== null && filter.value !== undefined && filter.value !== '';
    }
    return filter.value !== null && filter.value !== undefined && filter.value !== '';
  });

  return (
    <div className="filter-container">
      <div style={{ display: 'flex', gap: '10px', alignItems: 'center', marginBottom: isOpen ? '16px' : '0' }}>
        <button
          type="button"
          className={`filter-toggle ${isOpen ? 'active' : ''}`}
          onClick={() => setIsOpen(!isOpen)}
        >
          <span>🔍</span>
          <span>Фильтры</span>
          {isOpen ? '▼' : '▶'}
        </button>
        
        {hasActiveFilters && (
          <button
            type="button"
            className="btn btn-primary btn-small"
            onClick={onReset}
            style={{ whiteSpace: 'nowrap' }}
          >
            Показать все
          </button>
        )}
      </div>

      {isOpen && (
        <div style={{ marginTop: '16px' }}>
          {filters.map((filter, index) => (
            <div key={index}>
              {filter.type === 'chip' &&
                renderFilterSection(
                  filter.title,
                  renderChipFilter(
                    filter.options,
                    filter.value,
                    (value) => onFilterChange(filter.key, value),
                    filter.key
                  )
                )}

              {filter.type === 'number' &&
                renderFilterSection(
                  filter.title,
                  renderNumberRange(
                    filter.label,
                    filter.value,
                    (value) => onFilterChange(filter.key, value),
                    filter.min,
                    filter.max
                  )
                )}

              {filter.type === 'multiselect' &&
                renderFilterSection(
                  filter.title,
                  renderMultiSelect(
                    filter.options,
                    filter.value,
                    (value) => onFilterChange(filter.key, value)
                  )
                )}
            </div>
          ))}

          <div className="filter-actions">
            <button type="button" className="btn btn-secondary btn-small" onClick={onReset}>
              Сбросить
            </button>
          </div>
        </div>
      )}
    </div>
  );
};

export default FilterPanel;
