import Icon from './Icon.jsx';

/**
 * Кастомный тултип для react-joyride в стиле дизайн-системы SportGuide.
 * Размер подстраивается под текст (width: max-content), темы — через CSS-токены.
 */
const TourTooltip = ({
  index,
  size,
  isLastStep,
  step,
  backProps,
  closeProps,
  primaryProps,
  skipProps,
  tooltipProps,
}) => (
  <div className="tour-tooltip" {...tooltipProps}>
    <div className="tour-tooltip-head">
      <div className="tour-tooltip-dots" aria-label={`Шаг ${index + 1} из ${size}`}>
        {Array.from({ length: size }, (_, i) => (
          <span key={i} className={`tour-dot ${i === index ? 'active' : ''}`} />
        ))}
      </div>
      <button className="tour-tooltip-close" aria-label="Закрыть" {...closeProps}>
        <Icon name="x" size={14} />
      </button>
    </div>

    <div className="tour-tooltip-content">{step.content}</div>

    <div className="tour-tooltip-actions">
      {!isLastStep && (
        <button className="tour-btn tour-btn-skip" {...skipProps}>
          Пропустить
        </button>
      )}
      <div className="tour-tooltip-nav">
        {index > 0 && (
          <button className="tour-btn tour-btn-back" {...backProps}>
            Назад
          </button>
        )}
        <button className="tour-btn tour-btn-next" {...primaryProps}>
          {isLastStep ? 'Готово' : 'Далее'}
        </button>
      </div>
    </div>
  </div>
);

export default TourTooltip;
