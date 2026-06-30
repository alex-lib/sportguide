import Icon from './Icon.jsx';

/** Fab - floating action button (opens the create flow). */
const Fab = ({ icon = 'plus', label = 'Создать', onClick }) => (
  <button type="button" className="fab" aria-label={label} onClick={onClick}>
    <Icon name={icon} size={27} strokeWidth={2.4} />
  </button>
);

export default Fab;
