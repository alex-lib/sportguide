package com.sport.service.bot.commands.menu;

import com.sport.service.entities.place.District;
import com.sport.service.entities.place.PlaceType;
import com.sport.service.entities.place.Subdistrict;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;

public abstract class ChoosingPlaceOptionsMenu {

    public static InlineKeyboardMarkup createDistrictKeyboardForGetting(SendMessage answer) {
        answer.setText("\uD83D\uDDFA Выберите район Воронежа:");
        List<List<InlineKeyboardButton>> keyboard = List.of(
                createButtonRow("Железнодорожный", District.ZHELEZNODOROZHNYY.toString()),
                createButtonRow("Центральный", District.CENTRALNYY.toString()),
                createButtonRow("Коминтерновский", District.KOMINTERNOVSKYY.toString()),
                createButtonRow("Ленинский", District.LENINSKYY.toString()),
                createButtonRow("Советский", District.SOVETSKYY.toString()),
                createButtonRow("Левобережный", District.LEVOBEREZHNYY.toString()),
                createButtonRow("За городом", District.BEHIND_OF_CITY.toString()),
                createButtonRow("Поиск по всем районам", District.ALL_DISTRICTS.toString())
        );
        return InlineKeyboardMarkup.builder().keyboard(keyboard).build();
    }

    public static InlineKeyboardMarkup createDistrictKeyboardForCreating(SendMessage answer) {
        answer.setText("\uD83D\uDDFA Выберите район Воронежа:");
        List<List<InlineKeyboardButton>> keyboard = List.of(
                createButtonRow("Железнодорожный", District.ZHELEZNODOROZHNYY.toString()),
                createButtonRow("Центральный", District.CENTRALNYY.toString()),
                createButtonRow("Коминтерновский", District.KOMINTERNOVSKYY.toString()),
                createButtonRow("Ленинский", District.LENINSKYY.toString()),
                createButtonRow("Советский", District.SOVETSKYY.toString()),
                createButtonRow("Левобережный", District.LEVOBEREZHNYY.toString()),
                createButtonRow("За городом", District.BEHIND_OF_CITY.toString())
        );
        return InlineKeyboardMarkup.builder().keyboard(keyboard).build();
    }

    public static InlineKeyboardMarkup createOutdoorKeyboardForGettingPlace(SendMessage answer) {
        answer.setText("❔ Место на улице или в помещении?:");
        List<List<InlineKeyboardButton>> keyboard = List.of(
                createButtonRow("Улица", "true"),
                createButtonRow("Помещение", "false"),
                createButtonRow("Оба варианта", "null"),
                createButtonRow("ВЫБРАТЬ ТИП МЕСТА ЗАНОВО", "BACK")
        );
        return InlineKeyboardMarkup.builder().keyboard(keyboard).build();
    }

    public static InlineKeyboardMarkup createOutdoorKeyboardForCreatingPlace(SendMessage answer) {
        answer.setText("❔ Место на улице или в помещении?:");
        List<List<InlineKeyboardButton>> keyboard = List.of(
                createButtonRow("Улица", "true"),
                createButtonRow("Помещение", "false"),
                createButtonRow("ВЫБРАТЬ ТИП МЕСТА ЗАНОВО", "BACK")
        );
        return InlineKeyboardMarkup.builder().keyboard(keyboard).build();
    }

    public static InlineKeyboardMarkup createPlaceTypeKeyboard(SendMessage answer) {
        answer.setText("\uD83E\uDDBE Выберите тип места:");
        List<List<InlineKeyboardButton>> keyboard = List.of(
                createButtonRow("Открытая уличная спортивная площадка", PlaceType.SPORT_GROUND.toString()),
                createButtonRow("Футбольное поле", PlaceType.FOOTBALL_FIELD.toString()),
                createButtonRow("Баскетбольное поле", PlaceType.BASKETBALL_FIELD.toString()),
                createButtonRow("Волейбольное поле", PlaceType.VOLLEYBALL_FIELD.toString()),
                createButtonRow("Теннисный корт", PlaceType.TENNIS_COURT.toString()),
                createButtonRow("Пинг-понг стол", PlaceType.PINGPONG_TABLE.toString()),
                createButtonRow("Падел корт", PlaceType.PADEL_COURT.toString()),
                createButtonRow("Ледовая арена", PlaceType.ICE_RING.toString()),
                createButtonRow("Бассейн", PlaceType.SWIMMING_POOL.toString()),
                createButtonRow("Беговая зона", PlaceType.RUNNING_PLACE.toString()),
                createButtonRow("Зал для единоборств", PlaceType.MARTIAL_ARTS_HALL.toString()),
                createButtonRow("Тренажерный зал", PlaceType.GYM.toString()),
                createButtonRow("ВЫБРАТЬ ПОДРАЙОН/РАЙОН ЗАНОВО", "BACK")
        );
        return InlineKeyboardMarkup.builder().keyboard(keyboard).build();
    }

    private static List<InlineKeyboardButton> createButtonRow(String text, String callbackData) {
        return List.of(InlineKeyboardButton.builder()
                .text(text)
                .callbackData(callbackData)
                .build());
    }

    public static InlineKeyboardMarkup createZheleznodorozhnyySubdistrictsKeyboardForGetting(SendMessage answer) {
        answer.setText("\uD83D\uDDFA Выберите подрайон Железнодорожного района:");
        List<List<InlineKeyboardButton>> keyboard = List.of(
                createButtonRow("Процессор", Subdistrict.PROCESSOR.toString()),
                createButtonRow("От Остужевского кольца до Чернавской дамбы", Subdistrict.FROM_OSTUZHEVO_RING_TO_CHERNAVSKAAY_DAM.toString()),
                createButtonRow("Электроника", Subdistrict.ELECTRONIKA.toString()),
                createButtonRow("От Остужевского кольца до ЖД моста", Subdistrict.FROM_OSTUZHEVO_RING_TO_RAILWAY_BRIDGE.toString()),
                createButtonRow("Отрожка", Subdistrict.OTROZHKA.toString()),
                createButtonRow("Все подрайоны", Subdistrict.ALL_SUBDISTRICTS.toString()),
                createButtonRow("ВЫБРАТЬ РАЙОН ЗАНОВО", "BACK")
        );
        return InlineKeyboardMarkup.builder().keyboard(keyboard).build();
    }

    public static InlineKeyboardMarkup createLevoberezhnyySubdistrictsKeyboardForGetting(SendMessage answer) {
        answer.setText("\uD83D\uDDFA Выберите подрайон Левобережного района:");
        List<List<InlineKeyboardButton>> keyboard = List.of(
                createButtonRow("ВАИ и кватал Красный октябрь", Subdistrict.VAI_AND_QUARTER_KRASNYY_OKTYABR.toString()),
                createButtonRow("Вдоль улицы Димитрова(+ микрорайон Мостозавод и частный сектор)", Subdistrict.DIMITROVA_STREET.toString()),
                createButtonRow("От Чернавского моста до Вогресовского моста", Subdistrict.FROM_CHERNAVSKYY_BRIDGE_TO_VOGRESOVSKYY_BRIDGE.toString()),
                createButtonRow("Старый машмет", Subdistrict.STARYY_MASHMET.toString()),
                createButtonRow("БАМ", Subdistrict.BAM.toString()),
                createButtonRow("Песчанка, Озерки и Шинник-1", Subdistrict.PESCHANKA_AND_OZERKI_AND_SHINNIK_1.toString()),
                createButtonRow("Все подрайоны", Subdistrict.ALL_SUBDISTRICTS.toString()),
                createButtonRow("ВЫБРАТЬ РАЙОН ЗАНОВО", "BACK")
        );
        return InlineKeyboardMarkup.builder().keyboard(keyboard).build();
    }

    public static InlineKeyboardMarkup createSovetskyySubdistrictsKeyboardForGetting(SendMessage answer) {
        answer.setText("\uD83D\uDDFA Выберите подрайон Совесткого района:");
        List<List<InlineKeyboardButton>> keyboard = List.of(
                createButtonRow("Шилово", Subdistrict.SHILOVO.toString()),
                createButtonRow("ПридонскоЙ и Подкетное", Subdistrict.PRIDONSKOYY_AND_PODKLETNOE.toString()),
                createButtonRow("Первое мая, Юго-западный и Западный поселок", Subdistrict.PERVOE_MAAY_AND_UGO_ZAPADNYY_AND_ZAPADNYY_POSELOK.toString()),
                createButtonRow("Теннистый и Займище", Subdistrict.TENNISTYY_AND_ZAYYMISHE.toString()),
                createButtonRow("От микр. Новый Бомбей до ТЦ Армада и до микр. Депутатка", Subdistrict.FROM_NOVYY_BOMBEY_TO_ARMADA_TO_DEPUTATKA.toString()),
                createButtonRow("Все подрайоны", Subdistrict.ALL_SUBDISTRICTS.toString()),
                createButtonRow("ВЫБРАТЬ РАЙОН ЗАНОВО", "BACK")
        );
        return InlineKeyboardMarkup.builder().keyboard(keyboard).build();
    }

    public static InlineKeyboardMarkup createCentralnyySubdistrictsKeyboardForGetting(SendMessage answer) {
        answer.setText("\uD83D\uDDFA Выберите подрайон Центрального района:");
        List<List<InlineKeyboardButton>> keyboard = List.of(
                createButtonRow("От ВГУ до Северного моста", Subdistrict.FROM_VGU_TO_SEVERNYY_BRIDGE.toString()),
                createButtonRow("От Северного моста и дальше", Subdistrict.FROM_SEVERNYY_BRIDGE_TO_FURTHER.toString()),
                createButtonRow("Московский пр-кт вдоль ул. Ломоносова", Subdistrict.MP_ALONG_LOMONOSOVA_STREET_SIDE.toString()),
                createButtonRow("Все подрайоны", Subdistrict.ALL_SUBDISTRICTS.toString()),
                createButtonRow("ВЫБРАТЬ РАЙОН ЗАНОВО", "BACK")
        );
        return InlineKeyboardMarkup.builder().keyboard(keyboard).build();
    }

    public static InlineKeyboardMarkup createKominternovskyySubdistrictsKeyboardForGetting(SendMessage answer) {
        answer.setText("\uD83D\uDDFA Выберите подрайон Коминтерновского района:");
        List<List<InlineKeyboardButton>> keyboard = List.of(
                createButtonRow("Подгорное", Subdistrict.PODGORNOE.toString()),
                createButtonRow("Северный", Subdistrict.SEVERNYY.toString()),
                createButtonRow("От ул. 45-ой Стрелковой дивизии до 9-ое января и до Московского пр-кта", Subdistrict.FROM_45_DIVISII_STREET_TO_9_YANVARY_STREET_AND_TO_MP.toString()),
                createButtonRow("Квартал Ипподром, от Московского пр-кта до ул. Уритского", Subdistrict.QUARTER_IPPODROM_AND_FROM_MP_TO_ROTONDA_AND_TO_URITSKOGO_STREET.toString()),
                createButtonRow("Все подрайоны", Subdistrict.ALL_SUBDISTRICTS.toString()),
                createButtonRow("ВЫБРАТЬ РАЙОН ЗАНОВО", "BACK")
        );
        return InlineKeyboardMarkup.builder().keyboard(keyboard).build();
    }

    public static InlineKeyboardMarkup createZheleznodorozhnyySubdistrictsKeyboardForCreating(SendMessage answer) {
        answer.setText("\uD83D\uDDFA Выберите подрайон Железнодорожного района:");
        List<List<InlineKeyboardButton>> keyboard = List.of(
                createButtonRow("Процессор", Subdistrict.PROCESSOR.toString()),
                createButtonRow("От Остужевского кольца до Чернавской дамбы", Subdistrict.FROM_OSTUZHEVO_RING_TO_CHERNAVSKAAY_DAM.toString()),
                createButtonRow("Электроника", Subdistrict.ELECTRONIKA.toString()),
                createButtonRow("От Остужевского кольца до ЖД моста", Subdistrict.FROM_OSTUZHEVO_RING_TO_RAILWAY_BRIDGE.toString()),
                createButtonRow("Отрожка", Subdistrict.OTROZHKA.toString()),
                createButtonRow("ВЫБРАТЬ РАЙОН ЗАНОВО", "BACK")
        );
        return InlineKeyboardMarkup.builder().keyboard(keyboard).build();
    }

    public static InlineKeyboardMarkup createLevoberezhnyySubdistrictsKeyboardForCreating(SendMessage answer) {
        answer.setText("\uD83D\uDDFA Выберите подрайон Левобережного района:");
        List<List<InlineKeyboardButton>> keyboard = List.of(
                createButtonRow("ВАИ и кватал Красный октябрь", Subdistrict.VAI_AND_QUARTER_KRASNYY_OKTYABR.toString()),
                createButtonRow("Вдоль улицы Димитрова(+ микрорайон Мостозавод и частный сектор)", Subdistrict.DIMITROVA_STREET.toString()),
                createButtonRow("От Чернавского моста до Вогресовского моста", Subdistrict.FROM_CHERNAVSKYY_BRIDGE_TO_VOGRESOVSKYY_BRIDGE.toString()),
                createButtonRow("Старый машмет", Subdistrict.STARYY_MASHMET.toString()),
                createButtonRow("БАМ", Subdistrict.BAM.toString()),
                createButtonRow("Песчанка, Озерки и Шинник-1", Subdistrict.PESCHANKA_AND_OZERKI_AND_SHINNIK_1.toString()),
                createButtonRow("ВЫБРАТЬ РАЙОН ЗАНОВО", "BACK")
        );
        return InlineKeyboardMarkup.builder().keyboard(keyboard).build();
    }

    public static InlineKeyboardMarkup createSovetskyySubdistrictsKeyboardForCreating(SendMessage answer) {
        answer.setText("\uD83D\uDDFA Выберите подрайон Совесткого района:");
        List<List<InlineKeyboardButton>> keyboard = List.of(
                createButtonRow("Шилово", Subdistrict.SHILOVO.toString()),
                createButtonRow("ПридонскоЙ и Подкетное", Subdistrict.PRIDONSKOYY_AND_PODKLETNOE.toString()),
                createButtonRow("Первое мая, Юго-западный и Западный поселок", Subdistrict.PERVOE_MAAY_AND_UGO_ZAPADNYY_AND_ZAPADNYY_POSELOK.toString()),
                createButtonRow("Теннистый и Займище", Subdistrict.TENNISTYY_AND_ZAYYMISHE.toString()),
                createButtonRow("От микр. Новый Бомбей до ТЦ Армада и до микр. Депутатка", Subdistrict.FROM_NOVYY_BOMBEY_TO_ARMADA_TO_DEPUTATKA.toString()),
                createButtonRow("ВЫБРАТЬ РАЙОН ЗАНОВО", "BACK")
        );
        return InlineKeyboardMarkup.builder().keyboard(keyboard).build();
    }

    public static InlineKeyboardMarkup createCentralnyySubdistrictsKeyboardForCreating(SendMessage answer) {
        answer.setText("\uD83D\uDDFA Выберите подрайон Центрального района:");
        List<List<InlineKeyboardButton>> keyboard = List.of(
                createButtonRow("От ВГУ до Северного моста", Subdistrict.FROM_VGU_TO_SEVERNYY_BRIDGE.toString()),
                createButtonRow("От Северного моста и дальше", Subdistrict.FROM_SEVERNYY_BRIDGE_TO_FURTHER.toString()),
                createButtonRow("Московский пр-кт вдоль ул. Ломоносова", Subdistrict.MP_ALONG_LOMONOSOVA_STREET_SIDE.toString()),
                createButtonRow("ВЫБРАТЬ РАЙОН ЗАНОВО", "BACK")
        );
        return InlineKeyboardMarkup.builder().keyboard(keyboard).build();
    }

    public static InlineKeyboardMarkup createKominternovskyySubdistrictsKeyboardForCreating(SendMessage answer) {
        answer.setText("\uD83D\uDDFA Выберите подрайон Центрального района:");
        List<List<InlineKeyboardButton>> keyboard = List.of(
                createButtonRow("Подгорное", Subdistrict.PODGORNOE.toString()),
                createButtonRow("Северный", Subdistrict.SEVERNYY.toString()),
                createButtonRow("От ул. 45-ой Стрелковой дивизии до 9-ое января и до Московского пр-кта", Subdistrict.FROM_45_DIVISII_STREET_TO_9_YANVARY_STREET_AND_TO_MP.toString()),
                createButtonRow("Квартал Ипподром, от Московского пр-кта до ул. Уритского", Subdistrict.QUARTER_IPPODROM_AND_FROM_MP_TO_ROTONDA_AND_TO_URITSKOGO_STREET.toString()),
                createButtonRow("ВЫБРАТЬ РАЙОН ЗАНОВО", "BACK")
        );
        return InlineKeyboardMarkup.builder().keyboard(keyboard).build();
    }

    public static void chooseSubdistrictsMenuForSpecificDistrict(District district, SendMessage answer) {
        switch (district) {
            case District.ZHELEZNODOROZHNYY ->
                    answer.setReplyMarkup(ChoosingPlaceOptionsMenu.createZheleznodorozhnyySubdistrictsKeyboardForGetting(answer));
            case District.LEVOBEREZHNYY ->
                    answer.setReplyMarkup(ChoosingPlaceOptionsMenu.createLevoberezhnyySubdistrictsKeyboardForGetting(answer));
            case District.CENTRALNYY ->
                    answer.setReplyMarkup(ChoosingPlaceOptionsMenu.createCentralnyySubdistrictsKeyboardForGetting(answer));
            case District.SOVETSKYY ->
                    answer.setReplyMarkup(ChoosingPlaceOptionsMenu.createSovetskyySubdistrictsKeyboardForGetting(answer));
            case District.KOMINTERNOVSKYY ->
                    answer.setReplyMarkup(ChoosingPlaceOptionsMenu.createKominternovskyySubdistrictsKeyboardForGetting(answer));
        }
    }
}