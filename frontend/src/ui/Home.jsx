import { Link } from 'react-router-dom';
import Icon from './Icon.jsx';

/** Hero - branded promo block on Home; renders a router Link when `to` set. */
export const Hero = ({ title, subtitle, cta, to }) => {
  const inner = (
    <>
      <h2>{title}</h2>
      {subtitle && <p>{subtitle}</p>}
      {cta && (
        <span className="cta">
          {cta} <Icon name="arrow-right" size={15} />
        </span>
      )}
    </>
  );
  return to ? (
    <Link className="hero" to={to}>
      {inner}
    </Link>
  ) : (
    <div className="hero">{inner}</div>
  );
};

/** Tiles - 2-column quick-access grid. */
export const Tiles = ({ children }) => <div className="tiles">{children}</div>;

/** Tile - one quick-access entry; `wide` spans both columns. */
export const Tile = ({ to, icon, accent = false, title, subtitle, wide = false }) => (
  <Link to={to} className={`tile ${wide ? 'tile-wide' : ''}`.trim()}>
    <div className={`ic ${accent ? 'tile-ic-accent' : 'tile-ic-brand'}`}>
      <Icon name={icon} size={21} />
    </div>
    <div>
      <h4>{title}</h4>
      <span>{subtitle}</span>
    </div>
  </Link>
);

export default Hero;
