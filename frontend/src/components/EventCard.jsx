import { Card, CardTitle, CardText, Icon, DateBadge, Button } from '../ui';

const EventCard = ({ event, canEdit }) => {
  // API dates are calendar dates in Voronezh, not UTC timestamps.
  const date = /^\d{4}-\d{2}-\d{2}$/.test(event.date || '') ? new Date(`${event.date}T12:00:00`) : null;
  const validDate = date && !Number.isNaN(date.getTime());
  const weekday = validDate ? date.toLocaleDateString('ru-RU', { weekday: 'long' }) : null;
  const location = [event.address, event.district].filter(Boolean).join(' · ');
  const hasDetails = event.link && event.link !== '-';

  return (
    <Card className="event-card" data-tour="event-card">
      <div className="event-card-heading">
        <div className="event-card-date">
          <DateBadge date={validDate ? date : undefined} />
          {validDate && <span>{date.getFullYear()}</span>}
        </div>
        <div className="event-card-title-block">
          <CardTitle>{event.name}</CardTitle>
          <p className="event-card-time"><Icon name="clock" size={15} /><span>{event.time || 'Время уточняется'}</span>{weekday && <span className="event-card-weekday">{weekday}</span>}</p>
        </div>
      </div>

      {(event.placeName || location) && <div className="event-card-location">
        <Icon name="map-pin" size={18} />
        <div>{event.placeName && <strong>{event.placeName}</strong>}{location && <p>{location}</p>}</div>
      </div>}

      {event.description && (event.description.length > 180 ? (
        <details className="event-card-description">
          <summary>
            <span className="event-card-description-text">{event.description}</span>
            <span className="event-card-description-toggle"><span className="event-card-expand">Развернуть описание</span><span className="event-card-collapse">Свернуть описание</span><Icon name="chevron-down" size={15} /></span>
          </summary>
        </details>
      ) : <CardText className="event-card-description-text">{event.description}</CardText>)}

      {hasDetails && <Button href={event.link} target="_blank" rel="noopener noreferrer" variant="ghost" fullWidth className="event-card-open" data-tour="event-details">
        Подробнее<Icon name="arrow-right" size={17} />
      </Button>}
      {canEdit && event.id != null && <Button to={`/events/${event.id}/edit`} variant="tint" size="sm" fullWidth>
        <Icon name="pencil" size={15} />Редактировать событие
      </Button>}
    </Card>
  );
};

export default EventCard;
