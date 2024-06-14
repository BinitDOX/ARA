package com.dox.ara.manager

import android.Manifest
import android.annotation.SuppressLint
import android.app.AppOpsManager
import android.content.ComponentName
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.annotation.RequiresApi
import com.dox.ara.service.EventListenerService
import com.dox.ara.utility.Constants
import com.dox.ara.utility.Constants.SCREEN_CAPTURE_ACTION
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.PermissionListener
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject


class PermissionManager @Inject constructor(@ApplicationContext private val context: Context) {

    fun getPermissions(onPermissionsChecked: () -> Unit, onPermissionsDenied: () -> Unit) {
        val permissionsToRequest = mutableListOf<String>()

        permissionsToRequest.addAll(
            listOf(
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.FOREGROUND_SERVICE,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.READ_CALL_LOG,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.ANSWER_PHONE_CALLS,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        )

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            permissionsToRequest.add(Manifest.permission.BLUETOOTH_CONNECT)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(Manifest.permission.READ_MEDIA_AUDIO)
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE){
            permissionsToRequest.add(Manifest.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK)
        }

        Dexter
            .withContext(context)
            .withPermissions(*permissionsToRequest.toTypedArray())
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(multiplePermissionsReport: MultiplePermissionsReport) {
                    if (multiplePermissionsReport.areAllPermissionsGranted()) {
                        onPermissionsChecked()
                    }
                    if (multiplePermissionsReport.isAnyPermissionPermanentlyDenied) {
                        onPermissionsDenied()
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    token?.continuePermissionRequest()
                }
            }).check()
    }

    fun requestSinglePermission(permission: String, onPermissionGranted: () -> Unit, onPermissionDenied: () -> Unit){
        Dexter.withContext(context)
            .withPermission(permission)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(permisssionGranted: PermissionGrantedResponse?) {
                    onPermissionGranted()
                }

                override fun onPermissionDenied(permissionDenied: PermissionDeniedResponse?) {
                    onPermissionDenied()
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissionRequest: PermissionRequest?,
                    permissionToken: PermissionToken?
                ) {}
            }
        ).check()
    }

    // Runtime permissions
    fun checkAccessNetworkState(): Boolean {
        val granted = PackageManager.PERMISSION_GRANTED
        val requiredPermission = Manifest.permission.ACCESS_NETWORK_STATE
        val checkPrem: Int = context.checkCallingOrSelfPermission(requiredPermission)
        return checkPrem == granted
    }

    fun checkAccessWifiState(): Boolean {
        val granted = PackageManager.PERMISSION_GRANTED
        val requiredPermission = Manifest.permission.ACCESS_WIFI_STATE
        val checkPrem: Int = context.checkCallingOrSelfPermission(requiredPermission)
        return checkPrem == granted
    }

    fun checkAnswerPhoneCalls(): Boolean {
        val granted = PackageManager.PERMISSION_GRANTED
        val requiredPermission = Manifest.permission.ANSWER_PHONE_CALLS
        val checkPrem: Int = context.checkCallingOrSelfPermission(requiredPermission)
        return checkPrem == granted
    }

    fun checkReadExternalStorage(): Boolean {
        val granted = PackageManager.PERMISSION_GRANTED
        val requiredPermission = Manifest.permission.READ_EXTERNAL_STORAGE
        val checkPrem: Int = context.checkCallingOrSelfPermission(requiredPermission)
        return checkPrem == granted
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun checkReadMediaAudio(): Boolean {
        val granted = PackageManager.PERMISSION_GRANTED
        val requiredPermission = Manifest.permission.READ_MEDIA_AUDIO
        val checkPrem: Int = context.checkCallingOrSelfPermission(requiredPermission)
        return checkPrem == granted
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun checkReadMediaImages(): Boolean {
        val granted = PackageManager.PERMISSION_GRANTED
        val requiredPermission = Manifest.permission.READ_MEDIA_IMAGES
        val checkPrem: Int = context.checkCallingOrSelfPermission(requiredPermission)
        return checkPrem == granted
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun checkReadMediaVideo(): Boolean {
        val granted = PackageManager.PERMISSION_GRANTED
        val requiredPermission = Manifest.permission.READ_MEDIA_VIDEO
        val checkPrem: Int = context.checkCallingOrSelfPermission(requiredPermission)
        return checkPrem == granted
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun checkPostNotifications(): Boolean {
        val granted = PackageManager.PERMISSION_GRANTED
        val requiredPermission = Manifest.permission.POST_NOTIFICATIONS
        val checkPrem: Int = context.checkCallingOrSelfPermission(requiredPermission)
        return checkPrem == granted
    }

    @RequiresApi(Build.VERSION_CODES.S)
    fun checkBluetoothConnect(): Boolean {
        val granted = PackageManager.PERMISSION_GRANTED
        val requiredPermission = Manifest.permission.BLUETOOTH_CONNECT
        val checkPrem: Int = context.checkCallingOrSelfPermission(requiredPermission)
        return checkPrem == granted
    }

    fun checkWriteExternalStorage(): Boolean {
        val granted = PackageManager.PERMISSION_GRANTED
        val requiredPermission = Manifest.permission.WRITE_EXTERNAL_STORAGE
        val checkPrem: Int = context.checkCallingOrSelfPermission(requiredPermission)
        return checkPrem == granted
    }

    fun checkReadSms(): Boolean {
        val granted = PackageManager.PERMISSION_GRANTED
        val requiredPermission = Manifest.permission.READ_SMS
        val checkPrem: Int = context.checkCallingOrSelfPermission(requiredPermission)
        return checkPrem == granted
    }

    fun checkReadPhoneState(): Boolean {
        val granted = PackageManager.PERMISSION_GRANTED
        val requiredPermission = Manifest.permission.READ_PHONE_STATE
        val checkPrem: Int = context.checkCallingOrSelfPermission(requiredPermission)
        return checkPrem == granted
    }

    fun checkReadPhoneNumbers(): Boolean {
        val granted = PackageManager.PERMISSION_GRANTED
        val requiredPermission = Manifest.permission.READ_PHONE_NUMBERS
        val checkPrem: Int = context.checkCallingOrSelfPermission(requiredPermission)
        return checkPrem == granted
    }


    fun checkReceiveSms(): Boolean {
        val granted = PackageManager.PERMISSION_GRANTED
        val requiredPermission = Manifest.permission.RECEIVE_SMS
        val checkPrem: Int = context.checkCallingOrSelfPermission(requiredPermission)
        return checkPrem == granted
    }

    fun checkSendSms(): Boolean {
        val granted = PackageManager.PERMISSION_GRANTED
        val requiredPermission3 = Manifest.permission.SEND_SMS
        val checkPrem3: Int = context.checkCallingOrSelfPermission(requiredPermission3)
        return checkPrem3 == granted
    }

    fun checkReadCallLog(): Boolean {
        val granted = PackageManager.PERMISSION_GRANTED
        val requiredPermission = Manifest.permission.READ_CALL_LOG
        val checkPrem: Int = context.checkCallingOrSelfPermission(requiredPermission)
        return checkPrem == granted
    }

    fun checkReadContacts(): Boolean {
        val granted = PackageManager.PERMISSION_GRANTED
        val requiredPermission = Manifest.permission.READ_CONTACTS
        val checkPrem: Int = context.checkCallingOrSelfPermission(requiredPermission)
        return checkPrem == granted
    }

    fun checkWriteContacts(): Boolean {
        val granted = PackageManager.PERMISSION_GRANTED
        val requiredPermission = Manifest.permission.WRITE_CONTACTS
        val checkPrem: Int = context.checkCallingOrSelfPermission(requiredPermission)
        return checkPrem == granted
    }

    fun checkCaptureMic(): Boolean {
        val granted = PackageManager.PERMISSION_GRANTED
        val requiredPermission = Manifest.permission.RECORD_AUDIO
        val checkPrem: Int = context.checkCallingOrSelfPermission(requiredPermission)
        return checkPrem == granted
    }
    fun checkCaptureCam(): Boolean {
        val granted = PackageManager.PERMISSION_GRANTED
        val requiredPermission = Manifest.permission.CAMERA
        val checkPrem: Int = context.checkCallingOrSelfPermission(requiredPermission)
        return checkPrem == granted
    }
    fun checkGetLocation(): Boolean {
        val granted = PackageManager.PERMISSION_GRANTED
        val requiredPermission = Manifest.permission.ACCESS_FINE_LOCATION
        val checkPrem: Int = context.checkCallingOrSelfPermission(requiredPermission)
        return checkPrem == granted
    }

    // Special permissions
    fun isNotificationPermissionGranted(context: Context): Boolean {
        val contentResolver: ContentResolver = context.contentResolver
        val enabledNotificationListeners: String =
            Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        val packageName: String = context.packageName
        return enabledNotificationListeners.contains(
            packageName
        )
    }

    fun isOverlayPermissionGranted(context: Context): Boolean {
        return Settings.canDrawOverlays(context)
    }

    fun isScreenCapturePermissionGranted(): Boolean {
        return false //screenCaptureAccessToken != null
    }

    fun isUsageStatsPermissionGranted(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val packageManager = context.packageManager
        val applicationInfo = packageManager.getApplicationInfo(context.packageName, 0)
        val mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, applicationInfo.uid, applicationInfo.packageName)
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun isAccessibilityPermissionGranted(context: Context, accessibilityService: Class<*> = EventListenerService::class.java): Boolean {
        val expectedComponentName = ComponentName(context, accessibilityService)
        val enabledServicesSetting = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
        return enabledServicesSetting?.contains(expectedComponentName.flattenToString()) == true
    }


    @SuppressLint("NewApi")
    fun isManageExternalStoragePermissionGranted(): Boolean {
        return Environment.isExternalStorageManager()
    }

    fun isSpecialPermissionGranted(context: Context, specialPermission: Constants.SpecialPermission): Boolean {
        return when (specialPermission) {
            Constants.SpecialPermission.NOTIFICATION -> isNotificationPermissionGranted(context)
            Constants.SpecialPermission.OVERLAY -> isOverlayPermissionGranted(context)
            Constants.SpecialPermission.SCREEN_CAPTURE -> isScreenCapturePermissionGranted()
            Constants.SpecialPermission.APP_USAGE -> isUsageStatsPermissionGranted(context)
            Constants.SpecialPermission.ACCESSIBILITY -> isAccessibilityPermissionGranted(context)
            Constants.SpecialPermission.STORAGE -> isManageExternalStoragePermissionGranted()
        }
    }

    fun startSpecialPermissionActivity(context: Context, specialPermission: Constants.SpecialPermission) {
        val action = when (specialPermission) {
            Constants.SpecialPermission.NOTIFICATION -> Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS
            Constants.SpecialPermission.OVERLAY -> Settings.ACTION_MANAGE_OVERLAY_PERMISSION
            Constants.SpecialPermission.SCREEN_CAPTURE -> SCREEN_CAPTURE_ACTION
            Constants.SpecialPermission.APP_USAGE -> Settings.ACTION_USAGE_ACCESS_SETTINGS
            Constants.SpecialPermission.ACCESSIBILITY -> Settings.ACTION_ACCESSIBILITY_SETTINGS
            Constants.SpecialPermission.STORAGE -> Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
        }
        context.startActivity(Intent(action))
    }
}