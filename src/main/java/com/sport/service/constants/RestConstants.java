package com.sport.service.constants;

public class RestConstants {

    public static final String WEATHER_CHANNEL_NAME = "notification:weather";
    public static final String EVENT_CHANNEL_NAME = "notification:event";
    public static final String SUBSCRIBER_TO_ADMIN_CHANNEL_NAME = "notification:subscriber_to_admin";
    public static final String ADMIN_TO_SUBSCRIBER_CHANNEL_NAME = "notification:admin_to_subscriber";
    public static final String KEY_OF_CHANNEL_NAME = "notification:";

    public static final String CRON_DELETE_EVENT = "0 0 0 * * *";
    public static final String TIME_ZONE = "Europe/Moscow";

    public static final double[] COORDINATES = {51.694235, 39.227656};
    public static final String CRON_SEND_WEATHER = "0 0 6 * * *";
}