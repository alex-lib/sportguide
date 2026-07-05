import { useState } from 'react';
import {
  SearchField,
  ChipRail,
  FilterChip,
  Sheet,
  SheetSection,
  Button,
  Range,
  sportIconName,
} from '../ui';

/*
 * Shared, config-driven filter UI.
 *
 * Presentation (HIG 2026): a static search field + a horizontal quick-chip
 * rail surfacing the first chip/multiselect filter, plus a "Фильтры" chip that
 * opens a bottom sheet with every filter. Selecting a chip applies immediately
 * (pages reload on change); the sheet's "Применить" just closes it.
 *
 * Config item: { type: 'chip'|'multiselect'|'number', key, title, options?, value, min?, max?, label? }
 */

const isSportKey = (key = '') => key.toLowerCase().includes('sport');

const filterActive = (f) => {
  if (!f || !f.type) return false;
  if (f.type === 'multiselect') return Array.isArray(f.value) && f.value.length > 0;
  return f.value !== null && f.value !== undefined && f.value !== '';
};

const FilterPanel = ({ filters, onFilterChange, onReset, searchPlaceholder = 'Поиск' }) => {
  const [sheetOpen, setSheetOpen] = useState(false);

  if (!filters || !Array.isArray(filters) || filters.length === 0) return null;
  if (typeof onFilterChange !== 'function' || typeof onReset !== 'function') return null;

  const quick = filters.find((f) => f && (f.type === 'chip' || f.type === 'multiselect'));
  const activeCount = filters.filter(filterActive).length;

  const isSelected = (f, optValue) =>
    f.type === 'multiselect'
      ? Array.isArray(f.value) && f.value.includes(optValue)
      : f.value === optValue;

  const toggle = (f, optValue) => {
    if (f.type === 'multiselect') {
      const cur = Array.isArray(f.value) ? f.value : [];
      onFilterChange(f.key, cur.includes(optValue) ? cur.filter((v) => v !== optValue) : [...cur, optValue]);
    } else {
      onFilterChange(f.key, f.value === optValue ? null : optValue);
    }
  };

  const clear = (f) => onFilterChange(f.key, f.type === 'multiselect' ? [] : null);

  const optionIcon = (f, opt) => (isSportKey(f.key) ? sportIconName(opt.value) : opt.icon);

  const renderSheetFilter = (f) => {
    if (f.type === 'number') {
      return (
        <SheetSection key={f.key} title={f.title}>
          <Range
            min={f.min ?? 0}
            max={f.max ?? 100}
            value={f.value ?? ''}
            onChange={(e) => onFilterChange(f.key, e.target.value ? parseInt(e.target.value, 10) : null)}
          />
        </SheetSection>
      );
    }
    if (!Array.isArray(f.options)) return null;
    return (
      <SheetSection key={f.key} title={f.title}>
        <div className="sheet-chips">
          <FilterChip active={!filterActive(f)} onClick={() => clear(f)}>
            Все
          </FilterChip>
          {f.options.map((opt) =>
            opt && opt.value ? (
              <FilterChip
                key={opt.value}
                active={isSelected(f, opt.value)}
                icon={optionIcon(f, opt)}
                onClick={() => toggle(f, opt.value)}
              >
                {opt.label || opt.value}
              </FilterChip>
            ) : null
          )}
        </div>
      </SheetSection>
    );
  };

  return (
    <>
      <SearchField placeholder={searchPlaceholder} value="" onChange={() => {}} readOnly />

      <ChipRail>
        {quick && (
          <FilterChip active={!filterActive(quick)} onClick={() => clear(quick)}>
            Все
          </FilterChip>
        )}
        {quick?.options?.map((opt) =>
          opt && opt.value ? (
            <FilterChip
              key={opt.value}
              active={isSelected(quick, opt.value)}
              icon={optionIcon(quick, opt)}
              onClick={() => toggle(quick, opt.value)}
            >
              {opt.label || opt.value}
            </FilterChip>
          ) : null
        )}
        <FilterChip icon="sliders-horizontal" onClick={() => setSheetOpen(true)}>
          Фильтры{activeCount ? ` · ${activeCount}` : ''}
        </FilterChip>
      </ChipRail>

      <Sheet
        open={sheetOpen}
        title="Фильтры"
        onClose={() => setSheetOpen(false)}
        footer={
          <>
            <Button variant="tint" fullWidth onClick={onReset}>
              Сбросить
            </Button>
            <Button fullWidth onClick={() => setSheetOpen(false)}>
              Применить
            </Button>
          </>
        }
      >
        {filters.map(renderSheetFilter)}
      </Sheet>
    </>
  );
};

export default FilterPanel;
