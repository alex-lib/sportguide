import { useEffect, useState } from 'react';
import { apiService } from '../services/api.js';
import { Button, CardActions, ErrorBanner, Modal, CardText } from '../ui';

export const useCatalogPermissions = (catalog) => {
  const [allowed, setAllowed] = useState(false);
  useEffect(() => {
    let active = true;
    apiService.getCatalogPermissions(catalog)
      .then((permissions) => { if (active) setAllowed(permissions.canManage === true); })
      .catch(() => { if (active) setAllowed(false); });
    return () => { active = false; };
  }, [catalog]);
  return allowed;
};

export const CatalogActions = ({ catalog, item, onDeleted }) => {
  const [confirming, setConfirming] = useState(false);
  const [deleting, setDeleting] = useState(false);
  const [error, setError] = useState('');
  const remove = async () => {
    if (deleting) return;
    setDeleting(true);
    setError('');
    try {
      await apiService.deleteCatalogItem(catalog, item.id);
      setConfirming(false);
      onDeleted();
    } catch (failure) {
      if (failure.response?.status === 404) { setConfirming(false); onDeleted(); }
      else setError(failure.response?.status === 403 ? 'Недостаточно прав для удаления.' : 'Не удалось удалить. Попробуйте ещё раз.');
    } finally { setDeleting(false); }
  };
  return (
    <>
      <CardActions className="catalog-admin-actions">
        <Button to={`/${catalog}/${item.id}/edit`} size="sm" variant="tint" fullWidth>Редактировать</Button>
        <Button onClick={() => { setError(''); setConfirming(true); }} size="sm" variant="danger">Удалить</Button>
      </CardActions>
      <Modal open={confirming} title="Удаление" onCancel={() => { if (!deleting) setConfirming(false); }}>
        <CardText>Удалить «{item.name}»? Восстановить запись после удаления не получится.</CardText>
        {catalog === 'places' && <CardText>Место также исчезнет из списка мест работы тренеров.</CardText>}
        {error && <div role="alert"><ErrorBanner>{error}</ErrorBanner></div>}
        <Button variant="danger" fullWidth disabled={deleting} onClick={remove}>{deleting ? 'Удаляем…' : 'Подтвердить удаление'}</Button>
        <Button variant="ghost" fullWidth disabled={deleting} onClick={() => setConfirming(false)}>Отмена</Button>
      </Modal>
    </>
  );
};
