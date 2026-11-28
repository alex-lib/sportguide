import { useEffect, useState } from 'react';
import { useLocation, useNavigate, useParams } from 'react-router-dom';
import WebApp from '@twa-dev/sdk';
import { apiService } from '../services/api.js';
import CatalogPhoto from '../components/CatalogPhoto.jsx';
import CoachWorkPlaces from '../components/CoachWorkPlaces.jsx';
import { SPORT_TYPES } from '../constants/filters.js';
import { coachTelegram } from '../utils/coachTelegram.js';
import { pluralRu } from '../utils/plural.js';
import { Page, PageHeader, Card, CardText, Icon, Button, Divider, EmptyState, ErrorBanner, SkeletonList } from '../ui';
import './Coaches.css';

const CoachProfile = () => {
  const { id } = useParams();
  const location = useLocation();
  const navigate = useNavigate();
  const [coach, setCoach] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [attempt, setAttempt] = useState(0);

  useEffect(() => {
    let active = true;
    setLoading(true);
    setCoach(null);
    setError(null);
    apiService.getCoachProfile(id)
      .then(data => { if (active) setCoach(data); })
      .catch(err => { if (active) setError(err.response?.status === 404 ? 'not-found' : 'Не удалось загрузить карточку тренера.'); })
      .finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, [id, attempt]);

  const telegram = coachTelegram(coach?.telegramUsername);
  const openChat = (event) => {
    if (typeof WebApp.openTelegramLink !== 'function') return;
    event.preventDefault();
    try {
      WebApp.openTelegramLink(telegram.url);
    } catch {
      window.location.assign(telegram.url);
    }
  };
  const goBack = () => {
    if (location.state?.fromCoaches) navigate(-1);
    else navigate('/coaches', { replace: true });
  };
  const sex = { MALE: 'Мужской', FEMALE: 'Женский', Мужчина: 'Мужской', Женщина: 'Женский' }[coach?.sex] || coach?.sex;

  return (
    <>
      <PageHeader title="Карточка тренера">
        <Button variant="ghost" size="sm" onClick={goBack}><Icon name="arrow-left" size={16} /> К тренерам</Button>
      </PageHeader>
      <Page>
        {loading ? <SkeletonList /> : error === 'not-found' ? (
          <EmptyState icon="user" title="Тренер не найден" message="Карточка удалена или больше не опубликована." />
        ) : error ? (
          <><div role="alert"><ErrorBanner>{error}</ErrorBanner></div><Button onClick={() => setAttempt(value => value + 1)}>Повторить</Button></>
        ) : coach && (
          <Card first className="coach-profile" data-tour="coach-profile">
            <CatalogPhoto catalog="coaches" photoUrl={coach.photoUrl} className="coach-profile-photo" alt={coach.name}
              fallback={<div className="coach-profile-photo coach-profile-placeholder"><Icon name="user" size={64} /></div>} />
            <h2 className="coach-profile-name">{coach.name}</h2>
            <dl className="coach-profile-facts">
              <div><dt>Telegram</dt><dd>{telegram ? <a href={telegram.url} onClick={openChat}>@{telegram.username}</a> : 'Не указан'}</dd></div>
              <div><dt>Вид спорта</dt><dd>{coach.sportTypes?.map(type => SPORT_TYPES.find(option => option.value === type)?.label || type).join(', ') || 'Не указан'}</dd></div>
              <div><dt>Возраст</dt><dd>{coach.age != null ? `${coach.age} ${pluralRu(coach.age, ['год', 'года', 'лет'])}` : 'Не указан'}</dd></div>
              <div><dt>Пол</dt><dd>{sex || 'Не указан'}</dd></div>
              <div><dt>Опыт работы тренером</dt><dd>{coach.yearsOfExperience != null ? `${coach.yearsOfExperience} ${pluralRu(coach.yearsOfExperience, ['год', 'года', 'лет'])}` : 'Не указан'}</dd></div>
              <div><dt>Телефон</dt><dd>{coach.phoneNumber ? <a href={`tel:${coach.phoneNumber}`}>{coach.phoneNumber}</a> : 'Не указан'}</dd></div>
            </dl>
            <Divider />
            <h3>Образование и квалификация</h3>
            <CardText className="coach-profile-text">{coach.education || 'Не указаны'}</CardText>
            <h3>О тренере</h3>
            <CardText className="coach-profile-text">{coach.description || 'Описание не указано'}</CardText>
            <h3>Место работы</h3>
            {coach.workPlaces?.length || coach.workPlacesNames?.length ? <CoachWorkPlaces coach={coach} /> : <CardText>Не указано</CardText>}
            <Divider />
            {telegram ? (
              <Button href={telegram.url} onClick={openChat} fullWidth data-tour="coach-contact">Связаться с тренером</Button>
            ) : <CardText>Контакт Telegram не указан.</CardText>}
          </Card>
        )}
      </Page>
    </>
  );
};

export default CoachProfile;
