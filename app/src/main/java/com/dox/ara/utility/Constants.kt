package com.dox.ara.utility

import com.dox.ara.BuildConfig
import com.dox.ara.ui.data.NavItem
import com.dox.ara.ui.data.RouteItem

object Constants {
    // Build Constants
    const val APP_ID = "ara"
    const val APP_NAME = "ARA"
    const val PACKAGE_NAME = BuildConfig.APPLICATION_ID

    // API Constants
    const val BASE_URL = BuildConfig.BASE_URL

    // Encryption constants
    const val ENCRYPTION_ALGORITHM = "AES"
    const val DELIMITER_DATA_DATE = "<DATA_DATE>"

    // UI Constants
    val START_ROUTE = RouteItem.Home.route
    val START_PAGE = NavItem.Chats.page

    // Intent Extra Constants
    const val START_ROUTE_EXTRA = "startRoute"
    const val NAVIGATE_TO_EXTRA = "navigateTo"
    const val START_PAGE_EXTRA = "startPage"
    const val ALARM_ID_EXTRA = "alarmId"

    // Shared Preferences Constants
    const val APP_PREFS = "${APP_ID}_shared_prefs"
    const val AUTH_TOKEN_KEY = "auth_token"
    const val DEVICE_ID_KEY = "device_id"
    const val DYNAMIC_URL_KEY = "dynamic_url"
    const val PAYMENT_CODE_KEY = "payment_code"
    const val DEVICE_UNLOCK_CODE_KEY = "device_unlock"
    const val ASSISTANT_IDLE_AUTO_RESPONSE_TIME_KEY = "assistant_idle_auto_response_time"
    const val ASSISTANT_OPEN_TRIGGER_SEQUENCE_KEY = "assistant_open_trigger_sequence"
    const val ASSISTANT_LISTEN_TRIGGER_SEQUENCE_KEY = "assistant_listen_trigger_sequence"
    const val OVERRIDE_GOOGLE_ASSISTANT_KEY = "override_google_assistant"

    // Message Command Constants
    const val BREAK = "<BREAK>"
    const val TEST = "<TEST>"

    // Permission Constants
    const val SCREEN_CAPTURE_ACTION = "${PACKAGE_NAME}.SCREEN_CAPTURE_ACTION"
    const val SCREEN_CAPTURE_PERMISSION_DATA = "SCREEN_CAPTURE_PERMISSION_DATA"

    enum class SpecialPermission(val value: String) {
        NOTIFICATION("Notification"),
        OVERLAY("Overlay"),
        SCREEN_CAPTURE("Screen Capture"),
        APP_USAGE("App Usage"),
        ACCESSIBILITY("Accessibility"),
        STORAGE("Storage")
    }

    // Package Constants
    const val SETTINGS_PACKAGE_NAME = "com.android.settings"
    const val SYSTEM_UI_PACKAGE_NAME = "com.android.systemui"
    const val BLUETOOTH_PACKAGE_NAME = "com.oplus.wirelesssettings"
    const val PAYTM_PACKAGE_NAME = "net.one97.paytm"
    const val GOOGLE_ASSISTANT_PACKAGE_NAME = "com.google.android.googlequicksearchbox"

    // Directory Constants
    const val AUDIO_DIR = "${APP_ID}_audio"

    // Shared Preferences Constants
    const val DEFAULT_CHAT_ID_KEY = "default_chat_id"
    const val DEFAULT_ASSISTANT_ID_KEY = "default_assistant_id"

    // Notification Constants
    const val SILENT_NOTIFICATION_CHANNEL_ID = "${APP_ID}-silent"
    const val SILENT_NOTIFICATION_CHANNEL_NAME = "${APP_NAME}-Silent"
    const val ALERT_NOTIFICATION_CHANNEL_ID = "${APP_ID}-alert"
    const val ALERT_NOTIFICATION_CHANNEL_NAME = "${APP_NAME}-Alert"
    const val MAIN_NOTIFICATION_CHANNEL_ID = "${APP_ID}-main"
    const val MAIN_NOTIFICATION_CHANNEL_NAME = "${APP_NAME}-Main"
    const val MUSIC_NOTIFICATION_CHANNEL_ID = "${APP_ID}-music"
    const val MUSIC_NOTIFICATION_CHANNEL_NAME = "${APP_NAME}-Music"
    const val SPEECH_NOTIFICATION_CHANNEL_ID = "${APP_ID}-speech"
    const val SPEECH_NOTIFICATION_CHANNEL_NAME = "${APP_NAME}-Speech"


    enum class Entity {
        ASSISTANT,
        MESSAGE,
        CHAT,
    }
}