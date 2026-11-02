import { useEffect, useState } from 'react';
import { apiService } from '../services/api.js';

const CatalogPhoto = ({ catalog, photoUrl, className, alt = '', fallback = null }) => {
  const [src, setSrc] = useState('');
  useEffect(() => {
    let active = true;
    let objectUrl;
    setSrc('');
    if (photoUrl) apiService.getCatalogPhoto(catalog, photoUrl)
      .then((blob) => {
        if (!active) return;
        objectUrl = URL.createObjectURL(blob);
        setSrc(objectUrl);
      })
      .catch(() => {});
    return () => { active = false; if (objectUrl) URL.revokeObjectURL(objectUrl); };
  }, [catalog, photoUrl]);
  return src ? <img src={src} className={className} alt={alt} /> : fallback;
};
export default CatalogPhoto;
