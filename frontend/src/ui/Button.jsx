import { Link } from 'react-router-dom';

/**
 * Button - the single button primitive.
 * variant: 'primary' (brand gradient) | 'ghost' (brand on tint) |
 *          'tint' (neutral) | 'danger' (red). size: 'md' | 'sm'.
 * Renders <button>, <a href> or router <Link to> while keeping the look.
 */
const Button = ({
  variant = 'primary',
  size = 'md',
  fullWidth = false,
  to,
  href,
  className = '',
  children,
  ...rest
}) => {
  const classes = [
    'btn',
    `btn-${variant}`,
    size === 'sm' && 'btn-sm',
    fullWidth && 'btn-full',
    className,
  ]
    .filter(Boolean)
    .join(' ');

  if (to) {
    return (
      <Link to={to} className={classes} {...rest}>
        {children}
      </Link>
    );
  }
  if (href) {
    return (
      <a href={href} className={classes} {...rest}>
        {children}
      </a>
    );
  }
  return (
    <button className={classes} {...rest}>
      {children}
    </button>
  );
};

export default Button;
