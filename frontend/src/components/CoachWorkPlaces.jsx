import { useState } from 'react';
import { Button, CardText, Icon, MapView } from '../ui';

const CoachWorkPlaces = ({ coach }) => {
  const [mapPlace, setMapPlace] = useState(null);
  return (
    <>
      {coach.workPlaces?.length > 0 ? (
        <div style={{ marginTop: '12px', display: 'flex', flexWrap: 'wrap', gap: '8px' }}>
          {coach.workPlaces.map((place) => (
            <Button key={place.id} variant="ghost" size="sm" onClick={() => setMapPlace(place)}
              aria-label={`Показать ${place.name} на карте`}>
              <Icon name="map-pin" size={16} /> {place.name}
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
