package com.sport.service.components;

import com.sport.service.entities.Tour;
import com.sport.service.entities.Tooltip;
import com.sport.service.repositories.TourRepository;
import com.sport.service.repositories.TooltipRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class TourSeedService implements CommandLineRunner {
    private final TourRepository tourRepository;
    private final TooltipRepository tooltipRepository;

    @Override
    public void run(String... args) throws Exception {
        if (tourRepository.count() > 0) {
            log.info("Tours already seeded, skipping.");
            return;
        }

        log.info("Seeding tours...");

        // Home tour steps
        var homeTour = Tour.builder().route("/").name("Главная").build();
        tourRepository.save(homeTour);
        tooltipRepository.saveAll(List.of(
            Tooltip.builder().tour(homeTour).position(1).target("[data-tour=\"nav-/\"]").content("Главная — вы здесь. Отсюда вы можете перейти в любой раздел приложения.").placement("bottom").isPrimary(true).build(),
            Tooltip.builder().tour(homeTour).position(2).target("[data-tour=\"nav-/events\"]").content("Раздел \"События\" — спортивные мероприятия, встречи и соревнования.").placement("bottom").build(),
            Tooltip.builder().tour(homeTour).position(3).target("[data-tour=\"nav-/places\"]").content("Раздел \"Места\" — площадки, залы и спортивные объекты.").placement("bottom").build(),
            Tooltip.builder().tour(homeTour).position(4).target("[data-tour=\"nav-/joint-trainings\"]").content("Раздел \"Тренировки\" — совместные тренировки, где вы можете найти партнёров.").placement("bottom").build(),
            Tooltip.builder().tour(homeTour).position(5).target("[data-tour=\"nav-/coaches\"]").content("Раздел \"Тренеры\" — профессиональные тренеры по разным видам спорта.").placement("bottom").build(),
            Tooltip.builder().tour(homeTour).position(6).target("[data-tour=\"hero-events\"]").content("Главный промо-блок — быстрый переход к ближайшим спортивным событиям.").placement("bottom").isPrimary(true).build(),
            Tooltip.builder().tour(homeTour).position(7).target("[data-tour=\"tiles\"]").content("Быстрый доступ ко всем разделам приложения. Нажмите на карточку, чтобы перейти в нужный раздел.").placement("bottom").build(),
            Tooltip.builder().tour(homeTour).position(8).target(".icon-btn[aria-label=\"Настройки\"]").content("Переключите тему оформления приложения — светлую или тёмную.").placement("bottom").build()
        ));

        // Events tour steps
        var eventsTour = Tour.builder().route("/events").name("События").build();
        tourRepository.save(eventsTour);
        tooltipRepository.saveAll(List.of(
            Tooltip.builder().tour(eventsTour).position(1).target("[data-tour=\"search-field-wrapper\"]").content("Поиск событий — вводите название события или адрес.").placement("bottom").build(),
            Tooltip.builder().tour(eventsTour).position(2).target("[data-tour=\"chip-rail\"]").content("Быстрые фильтры — выберите дату или район для сужения поиска.").placement("bottom").build(),
            Tooltip.builder().tour(eventsTour).position(3).target(".filter-chip").content("Кнопка \"Фильтры\" — откроет расширенные настройки: все доступные фильтры в одном месте.").placement("bottom").build(),
            Tooltip.builder().tour(eventsTour).position(4).target("[data-tour=\"events-list\"]").content("Список событий. Каждое событие содержит дату, время, место и адрес.").placement("bottom").build(),
            Tooltip.builder().tour(eventsTour).position(5).target("[data-tour=\"event-details\"]").content("Кнопка \"Подробнее\" — ведёт на внешнюю страницу события.").placement("bottom").build(),
            Tooltip.builder().tour(eventsTour).position(6).target("[data-tour=\"events-empty\"]").content("Здесь будет сообщение, если по выбранным фильтрам ничего не найдено. Сбросьте фильтры, чтобы увидеть все события.").placement("bottom").build(),
            Tooltip.builder().tour(eventsTour).position(7).target("[data-tour=\"events-reset-filters\"]").content("Сбросить все применённые фильтры и показать полное списка событий.").placement("bottom").build()
        ));

        // Places tour steps
        var placesTour = Tour.builder().route("/places").name("Места").build();
        tourRepository.save(placesTour);
        tooltipRepository.saveAll(List.of(
            Tooltip.builder().tour(placesTour).position(1).target(".icon-btn[aria-label=\"Карта\"]").content("Открыть карту — все места отображаются на интерактивной карте.").placement("bottom").isPrimary(true).build(),
            Tooltip.builder().tour(placesTour).position(2).target("[data-tour=\"search-field-wrapper\"]").content("Поиск мест — вводите название места или адрес.").placement("bottom").build(),
            Tooltip.builder().tour(placesTour).position(3).target("[data-tour=\"chip-rail\"]").content("Быстрые фильтры — выберите тип места или район.").placement("bottom").build(),
            Tooltip.builder().tour(placesTour).position(4).target(".filter-chip").content("Расширенные фильтры: тип места, район, подряон и расположение (улица/зал).").placement("bottom").build(),
            Tooltip.builder().tour(placesTour).position(5).target("[data-tour=\"places-list\"]").content("Список мест. Каждая карточка содержит фото, описание, адрес и тип места.").placement("bottom").build(),
            Tooltip.builder().tour(placesTour).position(6).target("[data-tour=\"place-map\"]").content("Кнопка \"На карте\" — откроет место в навигаторе.").placement("bottom").build(),
            Tooltip.builder().tour(placesTour).position(7).target("[data-tour=\"place-website\"]").content("Ссылка на официальный сайт места, если он указан.").placement("bottom").build(),
            Tooltip.builder().tour(placesTour).position(8).target("[data-tour=\"places-empty\"]").content("Здесь будет сообщение, если по выбранным фильтрам ничего не найдено.").placement("bottom").build(),
            Tooltip.builder().tour(placesTour).position(9).target("[data-tour=\"places-reset-filters\"]").content("Сбросить все применённые фильтры и показать полный список мест.").placement("bottom").build()
        ));

        // Joint trainings tour steps
        var trainingsTour = Tour.builder().route("/joint-trainings").name("Тренировки").build();
        tourRepository.save(trainingsTour);
        tooltipRepository.saveAll(List.of(
            Tooltip.builder().tour(trainingsTour).position(1).target("[data-tour=\"search-field-wrapper\"]").content("Поиск тренировок — вводите ключевые слова: вид спорта, место, название.").placement("bottom").build(),
            Tooltip.builder().tour(trainingsTour).position(2).target("[data-tour=\"chip-rail\"]").content("Фильтры по виду спорта и району.").placement("bottom").build(),
            Tooltip.builder().tour(trainingsTour).position(3).target(".filter-chip").content("Расширенные фильтры — выберите вид спорта, район и другие параметры.").placement("bottom").build(),
            Tooltip.builder().tour(trainingsTour).position(4).target("[data-tour=\"trainings-list\"]").content("Список совместных тренировок. Каждая карточка содержит название, вид спорта, дату, место.").placement("bottom").build(),
            Tooltip.builder().tour(trainingsTour).position(5).target("[data-tour=\"training-write\"]").content("Написать создателю тренировки — перейдите в чат для обсуждения деталей.").placement("bottom").build(),
            Tooltip.builder().tour(trainingsTour).position(6).target("[data-tour=\"training-edit\"]").content("Редактировать тренировку — измените название, дату, место и другие параметры.").placement("bottom").build(),
            Tooltip.builder().tour(trainingsTour).position(7).target("[data-tour=\"training-delete\"]").content("Удалить тренировку — удалит тренировку с подтверждением.").placement("bottom").build(),
            Tooltip.builder().tour(trainingsTour).position(8).target("[data-tour=\"fab-create\"]").content("Создать совместную тренировку — заполните форму и найдите партнёров для занятий.").placement("bottom").isPrimary(true).build(),
            Tooltip.builder().tour(trainingsTour).position(9).target("[data-tour=\"trainings-empty\"]").content("Здесь будет сообщение, если по выбранным фильтрам ничего не найдено.").placement("bottom").build(),
            Tooltip.builder().tour(trainingsTour).position(10).target("[data-tour=\"trainings-reset-filters\"]").content("Сбросить все применённые фильтры и показать полный список тренировок.").placement("bottom").build(),
            Tooltip.builder().tour(trainingsTour).position(11).target("[data-tour=\"create-training-btn\"]").content("Создать тренировку — альтернативная кнопка создания, доступна в пустом списке.").placement("bottom").build()
        ));

        // Training programs tour steps
        var programsTour = Tour.builder().route("/training-programs").name("Программы тренировок").build();
        tourRepository.save(programsTour);
        tooltipRepository.saveAll(List.of(
            Tooltip.builder().tour(programsTour).position(1).target("[data-tour=\"search-field-wrapper\"]").content("Поиск программ — вводите название программы или вид спорта.").placement("bottom").build(),
            Tooltip.builder().tour(programsTour).position(2).target("[data-tour=\"chip-rail\"]").content("Фильтры — выберите вид спорта для отображения подходящих программ.").placement("bottom").build(),
            Tooltip.builder().tour(programsTour).position(3).target(".filter-chip").content("Расширенные фильтры — настройте вид спорта.").placement("bottom").build(),
            Tooltip.builder().tour(programsTour).position(4).target("[data-tour=\"programs-list\"]").content("Список программ тренировок. Каждая карточка содержит название, описание, длительность и уровень сложности.").placement("bottom").build(),
            Tooltip.builder().tour(programsTour).position(5).target("[data-tour=\"program-card\"]").content("Карточка программы — нажмите, чтобы просмотреть детали.").placement("bottom").build(),
            Tooltip.builder().tour(programsTour).position(6).target("[data-tour=\"programs-empty\"]").content("Здесь будет сообщение, если по выбранным фильтрам ничего не найдено.").placement("bottom").build(),
            Tooltip.builder().tour(programsTour).position(7).target("[data-tour=\"programs-reset-filters\"]").content("Сбросить все применённые фильтры и показать полный список программ.").placement("bottom").build()
        ));

        // Coaches tour steps
        var coachesTour = Tour.builder().route("/coaches").name("Тренеры").build();
        tourRepository.save(coachesTour);
        tooltipRepository.saveAll(List.of(
            Tooltip.builder().tour(coachesTour).position(1).target("[data-tour=\"search-field-wrapper\"]").content("Поиск тренеров — вводите имя, специализацию или район.").placement("bottom").build(),
            Tooltip.builder().tour(coachesTour).position(2).target("[data-tour=\"chip-rail\"]").content("Фильтры — выберите вид спорта, пол, возраст и опыт работы.").placement("bottom").build(),
            Tooltip.builder().tour(coachesTour).position(3).target(".filter-chip").content("Расширенные фильтры — настройте параметры поиска тренера.").placement("bottom").build(),
            Tooltip.builder().tour(coachesTour).position(4).target("[data-tour=\"coaches-list\"]").content("Список тренеров. Каждая карточка содержит имя, специализацию, рейтинг, опыт и район.").placement("bottom").build(),
            Tooltip.builder().tour(coachesTour).position(5).target("[data-tour=\"coach-contact\"]").content("Связаться с тренером — откроет возможность позвонить или написать.").placement("bottom").build(),
            Tooltip.builder().tour(coachesTour).position(6).target("[data-tour=\"coaches-empty\"]").content("Здесь будет сообщение, если по выбранным фильтрам ничего не найдено.").placement("bottom").build(),
            Tooltip.builder().tour(coachesTour).position(7).target("[data-tour=\"coaches-reset-filters\"]").content("Сбросить все применённые фильтры и показать полный список тренеров.").placement("bottom").build()
        ));

        log.info("Tours seeded successfully: {} tours, {} steps", 6, 50);
    }
}