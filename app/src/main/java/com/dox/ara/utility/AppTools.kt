package com.dox.ara.utility

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.KeyguardManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.location.LocationManager
import android.media.AudioManager
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.PowerManager
import android.provider.Settings
import android.telephony.TelephonyManager
import androidx.core.app.ActivityCompat
import com.dox.ara.utility.Constants.APP_ID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.time.delay
import timber.log.Timber
import java.io.File
import java.time.Duration
import java.time.temporal.ChronoUnit
import java.util.Locale


@SuppressLint("Range")
class AppTools {
    companion object {

        fun getScreenBrightness(context: Context): Int {
            return Settings.System.getInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS
            );
        }

        fun isWifiEnabled(context: Context): Boolean {
            val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
            return wifiManager.isWifiEnabled
        }

        fun isMobileDataEnabled(context: Context): Boolean {
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            if (ActivityCompat.checkSelfPermission (
                    context,
                    Manifest.permission.ACCESS_NETWORK_STATE
                ) == PackageManager.PERMISSION_GRANTED
            ) { return telephonyManager.isDataEnabled }
            return false
        }

        fun isGpsEnabled(context: Context): Boolean {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        }

        fun getDeviceName(): String {
            fun capitalize(string: String?): String {
                if (string.isNullOrEmpty()) {
                    return ""
                }
                val first = string[0]
                return if (Character.isUpperCase(first)) {
                    string
                } else {
                    Character.toUpperCase(first).toString() + string.substring(1)
                }
            }

            val manufacturer = Build.MANUFACTURER
            val model = Build.MODEL
            return if (model.lowercase(Locale.getDefault())
                    .startsWith(manufacturer.lowercase(Locale.getDefault()))
            ) {
                capitalize(model)
            } else {
                capitalize(manufacturer) + " " + model
            }
        }

        fun getBatteryPercentage(context: Context): Int {
            val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            return bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        }

        fun getVolumeLevels(context: Context): Map<String, Int> {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val volumeLevels = mutableMapOf<String, Int>()
            volumeLevels["Call"] = audioManager.getStreamVolume(
                AudioManager.STREAM_VOICE_CALL)
            volumeLevels["Media"] = audioManager.getStreamVolume(
                AudioManager.STREAM_MUSIC)
            volumeLevels["Ringtone"] = audioManager.getStreamVolume(
                AudioManager.STREAM_RING)
            volumeLevels["Notification"] = audioManager.getStreamVolume(
                AudioManager.STREAM_NOTIFICATION)
            volumeLevels["Alarm"] = audioManager.getStreamVolume(
                AudioManager.STREAM_ALARM)
            return volumeLevels
        }

        fun getPublicDirectory(directoryName: String, type: String): File {
            val directory = File(
                Environment.getExternalStoragePublicDirectory(type),
                directoryName
            )

            if (!directory.exists()) {
                directory.mkdirs()
            }

            return directory
        }

        fun isDeviceKeyguardLocked(context: Context): Boolean {
            val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            return keyguardManager.isKeyguardLocked
        }

        fun isDeviceLocked(context: Context): Boolean {
            val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            return keyguardManager.isDeviceLocked
        }

        fun isScreenOff(context: Context): Boolean {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            return !powerManager.isInteractive
        }

        fun wakeUpDevice(context: Context) {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            val wakeLock = powerManager.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.FULL_WAKE_LOCK,
                "${APP_ID}:wake-lock")
            wakeLock.acquire(3000)
        }

        fun getKeyGuard(context: Context): KeyguardManager.KeyguardLock {
            val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            return keyguardManager.newKeyguardLock("${APP_ID}:keyguard-lock")
        }

        private fun killProcess(context: Activity) {
            context.finish()
            android.os.Process.killProcess( android.os.Process.myPid())
        }

        fun isAppInstalled(packageName: String, context: Context): Boolean {
            val packageManager = context.packageManager
            return try {
                packageManager.getPackageInfo(packageName, 0)
                true
            } catch (e: PackageManager.NameNotFoundException) {
                false
            }
        }

        fun getForegroundAppPackageName(context: Context): String? {
            val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val time = System.currentTimeMillis()
            val usageStatsList = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 1000, time)

            if (usageStatsList.isEmpty()) {
                return null
            }

            val foregroundUsageStat = usageStatsList.maxByOrNull { it.lastTimeUsed }
            return foregroundUsageStat?.packageName
        }

        fun isConnectedToInternet(): Boolean {
            val command = "ping -c 1 google.com"
            return Runtime.getRuntime().exec(command).waitFor() == 0
        }

        @SuppressLint("QueryPermissionsNeeded")
        fun isAppUpiReady(context: Context, packageName: String): Boolean {
            var appUpiReady = false
            val upiIntent = Intent(Intent.ACTION_VIEW, Uri.parse("upi://pay"))
            val packageManager = context.packageManager
            val upiActivities: List<ResolveInfo> = packageManager.queryIntentActivities(upiIntent, 0)
            for (a in upiActivities){
                if (a.activityInfo.packageName == packageName) appUpiReady = true
            }
            return appUpiReady
        }

        fun getAudioMode(context: Context): Int {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val mode = when (audioManager.mode) {
                AudioManager.MODE_NORMAL -> "NORMAL"
                AudioManager.MODE_RINGTONE -> "RINGTONE"
                AudioManager.MODE_IN_CALL -> "IN_CALL"
                AudioManager.MODE_IN_COMMUNICATION -> "IN_COMMUNICATION"
                else -> "UNKNOWN"
            }
            Timber.d("[${::getAudioMode.name}] Current mode: $mode")
            return audioManager.mode
        }

        fun decreaseRingVolumeTemporarily(context: Context, volumePercent: Int, forDurationInSec: Long){
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_RING)
            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING)
            val desiredVolume = (maxVolume * (volumePercent.toDouble() / 100.0)).toInt()  // 30% volume
            Timber.d("[${::decreaseRingVolumeTemporarily.name}] Setting ring volume to ${volumePercent}%")
            audioManager.setStreamVolume(AudioManager.STREAM_RING, desiredVolume, 0)

            CoroutineScope(Dispatchers.IO).launch {
                delay(Duration.of(forDurationInSec, ChronoUnit.SECONDS))
                Timber.d("[${::decreaseRingVolumeTemporarily.name}] Restoring ring volume")
                audioManager.setStreamVolume(AudioManager.STREAM_RING, currentVolume, 0)
            }
        }
    }
}