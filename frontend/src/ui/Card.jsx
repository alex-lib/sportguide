import { Link } from 'react-router-dom';
import Icon from './Icon.jsx';

/** Card - surface container. Interactive (link/onClick) gets pointer + reset. */
export const Card = ({ to, href, onClick, first, className = '', children, ...rest }) => {
  const interactive = Boolean(to || href || onClick);
  const classes = ['card', first && 'first', interactive && 'card-interactive', className]
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
    <div className={classes} onClick={onClick} {...rest}>
      {children}
    </div>
  );
};

export const CardTitle = ({ children, className = '', ...rest }) => (
  <h3 className={`card-title ${className}`.trim()} {...rest}>
    {children}
  </h3>
);

export const CardText = ({ children, className = '', ...rest }) => (
  <p className={`card-text ${className}`.trim()} {...rest}>
    {children}
  </p>
);

export const CardRow = ({ children, className = '', ...rest }) => (
  <div className={`card-row ${className}`.trim()} {...rest}>
    {children}
  </div>
);

export const CardActions = ({ children, className = '', ...rest }) => (
  <div className={`card-actions ${className}`.trim()} {...rest}>
    {children}
  </div>
);

export const Divider = () => <div className="divider" />;

export const CardChevron = () => (
  <span className="card-chev">
    <Icon name="chevron-right" size={18} />
  </span>
);

/** Thumb - brand-gradient rounded square holding an icon. */
export const Thumb = ({ icon, accent = false, size = 25, className = '', children }) => (
  <div className={`thumb ${accent ? 'thumb-accent' : ''} ${className}`.trim()}>
    {icon ? <Icon name={icon} size={size} /> : children}
  </div>
);

/** AvatarImg - real circular-ish photo for a person. */
export const AvatarImg = ({ src, alt = '', className = '', ...rest }) => (
  <img src={src} alt={alt} className={`avatar-img ${className}`.trim()} {...rest} />
);

/** Photo - edge-to-edge image header for a card. */
export const Photo = ({ src, alt = '', ...rest }) => (
  <img src={src} alt={alt} className="card-photo" {...rest} />
);

/** PhotoPlaceholder - gradient block with an icon when there is no image. */
export const PhotoPlaceholder = ({ icon = 'building-2' }) => (
  <div className="card-photo-ph">
    <Icon name={icon} size={46} />
  </div>
);

export default Card;
