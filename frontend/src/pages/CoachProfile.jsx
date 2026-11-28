import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import WebApp from '@twa-dev/sdk';
import { apiService } from '../services/api.js';
import CatalogPhoto from '../components/CatalogPhoto.jsx';
import CoachWorkPlaces from '../components/CoachWorkPlaces.jsx';
import { SPORT_TYPES } from '../constants/filters.js';
import { coachTelegram } from '../utils/coachTelegram.js';
import { pluralRu } from '../utils/plural.js';
import { Page, PageHeader, Card, CardText, Icon, Button, EmptyState, ErrorBanner, SkeletonList } from '../ui';
import './Coaches.css';

const CoachProfile = () => {
  const { id } = useParams();
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
  const sex = { MALE: 'Мужской', FEMALE: 'Женский', Мужчина: 'Мужской', Женщина: 'Женский' }[coach?.sex] || coach?.sex;
  const education = coach?.education?.split(/;\s*|\n+/).map(part => part.trim()).filter(Boolean) || [];

  return (
    <>
      <div className="coach-profile-header"><PageHeader title="Тренер" /></div>
      <Page className="coach-profile-page">
        {loading ? <SkeletonList /> : error === 'not-found' ? (
          <EmptyState icon="user" title="Тренер не найден" message="Карточка удалена или больше не опубликована." />
        ) : error ? (
          <><div role="alert"><ErrorBanner>{error}</ErrorBanner></div><Button onClick={() => setAttempt(value => value + 1)}>Повторить</Button></>
        ) : coach && (
          <div className="coach-profile" data-tour="coach-profile">
            <Card first className="coach-profile-hero">
              <div className="coach-profile-identity">
                <CatalogPhoto catalog="coaches" photoUrl={coach.photoUrl} className="coach-profile-photo" alt={coach.name}
                  fallback={<div className="coach-profile-photo coach-profile-placeholder"><Icon name="user" size={40} /></div>} />
                <div className="coach-profile-heading">
                  {coach.sportTypes?.length > 0 && <div className="coach-profile-specialties">
                    {coach.sportTypes.map(type => <span key={type}>{SPORT_TYPES.find(option => option.value === type)?.label || type}</span>)}
                  </div>}
                  <h2 className="coach-profile-name">{coach.name}</h2>
                  {telegram && <a className="coach-profile-username" href={telegram.url} onClick={openChat}>@{telegram.username}</a>}
                </div>
              </div>
              <dl className="coach-profile-facts">
                <div><dt>Опыт</dt><dd>{coach.yearsOfExperience != null ? `${coach.yearsOfExperience} ${pluralRu(coach.yearsOfExperience, ['год', 'года', 'лет'])}` : 'Не указан'}</dd></div>
                <div><dt>Возраст</dt><dd>{coach.age != null ? `${coach.age} ${pluralRu(coach.age, ['год', 'года', 'лет'])}` : 'Не указан'}</dd></div>
                <div><dt>Пол</dt><dd>{sex || 'Не указан'}</dd></div>
              </dl>
              {telegram ? (
                <div className="coach-profile-contact">
                  <Button href={telegram.url} onClick={openChat} fullWidth data-tour="coach-contact"><Icon name="message-circle" size={18} />Связаться с тренером</Button>
                  <p>Личный чат в Telegram</p>
                </div>
              ) : <CardText>Контакт Telegram не указан.</CardText>}
            </Card>

            <div className="coach-profile-sections">
              <Card className="coach-profile-section coach-profile-services">
                <h3><Icon name="dumbbell" size={19} />Чем могу помочь</h3>
                <CardText className="coach-profile-text">{coach.description || 'Описание не указано'}</CardText>
              </Card>
              <Card className="coach-profile-section coach-profile-location">
                <h3><Icon name="map-pin" size={19} />Место работы</h3>
                {coach.workPlaces?.length || coach.workPlacesNames?.length ? <CoachWorkPlaces coach={coach} detailed /> : <CardText>Не указано</CardText>}
              </Card>
              <Card className="coach-profile-section coach-profile-qualifications">
                <h3><Icon name="graduation-cap" size={19} />Образование и квалификация</h3>
                {education.length > 0 ? <ul className="coach-profile-education">
                  {education.map((item, index) => <li key={index}>{item}</li>)}
                </ul> : <CardText>Не указаны</CardText>}
              </Card>
              <Card className="coach-profile-section coach-profile-contacts">
                <h3><Icon name="phone" size={19} />Телефон</h3>
                {coach.phoneNumber ? <a className="coach-profile-phone" href={`tel:${coach.phoneNumber}`}>
                  <span><strong>{coach.phoneNumber}</strong><small>Позвонить тренеру</small></span><Icon name="arrow-right" size={18} />
                </a> : <CardText>Не указан</CardText>}
              </Card>
            </div>
          </div>
        )}
      </Page>
    </>
  );
};

export default CoachProfile;
