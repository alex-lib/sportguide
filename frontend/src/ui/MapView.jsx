import { MapContainer, TileLayer, Marker, Popup } from 'react-leaflet';
import L from 'leaflet';
import Modal from './Modal.jsx';

const extractCoords = (url) => {
  if (!url) return null;
  const match = url.match(/q=([-\d.]+),([-\d.]+)/);
  if (!match) return null;
  const lat = parseFloat(match[1]);
  const lng = parseFloat(match[2]);
  if (isNaN(lat) || isNaN(lng)) return null;
  return [lat, lng];
};

const defaultIcon = new L.Icon({
  iconUrl:
    'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
  iconRetinaUrl:
    'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png',
  shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
  iconSize: [25, 41],
  iconAnchor: [12, 41],
  popupAnchor: [1, -34],
  shadowSize: [41, 41],
});

const MapView = ({ places, open, onClose }) => {
  const validPlaces = places
    .map((p) => ({ ...p, latLng: extractCoords(p.coordinates) }))
    .filter((p) => p.latLng != null);

  if (validPlaces.length === 0) {
    return null;
  }

  const center = [
    validPlaces.reduce((s, p) => s + p.latLng[0], 0) / validPlaces.length,
    validPlaces.reduce((s, p) => s + p.latLng[1], 0) / validPlaces.length,
  ];

  return (
    <Modal open={open} title="Карта" onCancel={onClose}>
      <div className="map-container">
        <MapContainer
          center={center}
          zoom={13}
          style={{ height: '100%', width: '100%' }}
        >
          <TileLayer
            url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
            attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
          />
          {validPlaces.map((place, i) => (
            <Marker key={place.id || i} position={place.latLng} icon={defaultIcon}>
              <Popup>
                <strong>{place.name}</strong>
                {place.address && <br />}
                {place.address}
                <br />
                <a
                  href={place.coordinates}
                  target="_blank"
                  rel="noopener noreferrer"
                >
                  Открыть в Google Maps
                </a>
              </Popup>
            </Marker>
          ))}
        </MapContainer>
      </div>
    </Modal>
  );
};

export default MapView;
