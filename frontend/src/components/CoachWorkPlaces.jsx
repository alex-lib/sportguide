import { useState } from 'react';
import { Button, CardText, Icon, MapView } from '../ui';

const CoachWorkPlaces = ({ coach, detailed = false }) => {
  const [mapPlace, setMapPlace] = useState(null);
  return (
    <>
      {coach.workPlaces?.length > 0 ? (
        <div className={detailed ? 'coach-profile-workplaces' : undefined}
          style={detailed ? undefined : { marginTop: '12px', display: 'flex', flexWrap: 'wrap', gap: '8px' }}>
          {coach.workPlaces.map((place) => (
            <Button key={place.id} variant="ghost" size="sm" onClick={() => setMapPlace(place)}
              fullWidth={detailed} className={detailed ? 'coach-profile-place' : ''}
              aria-label={`Показать ${place.name} на карте`}>
              {detailed ? <><span><strong>{place.name}</strong>{place.address && <small>{place.address}</small>}<em>Показать на карте</em></span><Icon name="chevron-right" size={18} /></> : <><Icon name="map-pin" size={16} /> {place.name}</>}
            </Button>
          ))}
        </div>
      ) : coach.workPlacesNames?.length > 0 && (
        <CardText>{coach.workPlacesNames.join(', ')}</CardText>
      )}
      {mapPlace && <MapView places={[mapPlace]} open onClose={() => setMapPlace(null)} />}
    </>
  );
};

export default CoachWorkPlaces;
