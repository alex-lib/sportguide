import { Link } from 'react-router-dom';
import { Card, CardTitle, CardText, Icon, Button } from '../ui';
import { SPORT_TYPES } from '../constants/filters.js';
import { pluralRu } from '../utils/plural.js';
import CatalogPhoto from './CatalogPhoto.jsx';
import { CatalogActions } from './CatalogAdmin.jsx';
import CoachWorkPlaces from './CoachWorkPlaces.jsx';

const CoachMiniCard = ({ coach, canManage, onDeleted }) => {
  const specialties = coach.sportTypes?.map(type => SPORT_TYPES.find(option => option.value === type)?.label || type).join(' · ') || coach.specialization;
  return (
    <Card className="coach-mini" data-tour="coach-card">
      <Link className="coach-profile-link coach-mini-header" to={`/coaches/${coach.id}`}>
        <CatalogPhoto catalog="coaches" photoUrl={coach.photoUrl} className="coach-mini-photo" alt={coach.name}
          fallback={<div className="coach-mini-photo coach-mini-placeholder"><Icon name="user" size={28} /></div>} />
        <div className="coach-mini-identity">
          <CardTitle className="coach-mini-name">{coach.name}</CardTitle>
          {specialties && <p className="coach-mini-specialties">{specialties}</p>}
          {coach.yearsOfExperience != null && <p className="coach-mini-experience">
            <Icon name="award" size={14} />
            {coach.yearsOfExperience} {pluralRu(coach.yearsOfExperience, ['год', 'года', 'лет'])} опыта
          </p>}
        </div>
      </Link>
      {coach.description && <CardText className="coach-mini-description">{coach.description}</CardText>}
      <CoachWorkPlaces coach={coach} compact />
      <Button to={`/coaches/${coach.id}`} variant="ghost" fullWidth className="coach-mini-open" data-tour="coach-open">
        Открыть карточку<Icon name="arrow-right" size={17} />
      </Button>
      {canManage && <CatalogActions catalog="coaches" item={coach} onDeleted={onDeleted} />}
    </Card>
  );
};

export default CoachMiniCard;
