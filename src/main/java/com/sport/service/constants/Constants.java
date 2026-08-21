package com.sport.service.constants;

public class Constants {

    public static final String WEATHER_CHANNEL_NAME = "notification:weather";
    public static final String EVENT_CHANNEL_NAME = "notification:event";
    public static final String SUBSCRIBER_TO_ADMIN_CHANNEL_NAME = "notification:subscriber_to_admin";
    public static final String ADMIN_TO_SUBSCRIBER_CHANNEL_NAME = "notification:admin_to_subscriber";
    public static final String KEY_OF_CHANNEL_NAME = "notification:";

    public static final String CRON_DELETE_EVENT = "0 0 0 * * *";
    public static final String CRON_DELETE_JOINT_TRAINING = "0 0 */4 * * *";
    public static final String TIME_ZONE = "Europe/Moscow";

    public static final String CRON_SEND_WEATHER = "0 0 6 * * *";
    public static final double[] COORDINATES = {51.694235, 39.227656};

    public static final String CRON_TURN_OFF_DISPLAY_IN_WEB_COACH = "0 0 0 * * *";
}