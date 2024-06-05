package com.dox.ara.command.types

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import android.provider.Settings
import com.dox.ara.command.CommandHandler
import com.dox.ara.command.CommandResponse
import com.dox.ara.manager.PermissionManager
import com.dox.ara.service.EventListenerService.Companion.startRoutine
import com.dox.ara.service.event.SettingCommandEvent.Companion.MOBILE_DATA_SUB_ROUTINE
import com.dox.ara.service.event.SettingCommandEvent.Companion.QUICK_SETTINGS_ROUTINE
import com.dox.ara.service.event.SettingCommandEvent.Companion.WIFI_SUB_ROUTINE
import com.dox.ara.service.event.SettingCommandEvent.Companion.cancelAllSubRoutines
import com.dox.ara.utility.AppTools
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CompletableDeferred
import timber.log.Timber


class SettingCommandHandler @AssistedInject constructor(
    @Assisted private val args : List<String>,
    @ApplicationContext private val context: Context,
    private val permissionManager: PermissionManager
) : CommandHandler(args) {
    override val numArgs = 2
    private lateinit var settingType: SettingType
    private lateinit var settingValue: SettingValue

    enum class SettingType {
        WIFI,
        BLUETOOTH,
        MOBILE_DATA,
        TORCH
    }

    enum class SettingValue {
        ON,
        OFF,
    }

    override fun help(): String {
        val settingTypes = SettingType.entries.joinToString("|") { it.name }
        val settingValues = SettingValue.entries.joinToString("|") { it.name }
        return "Usage: setting(<$settingTypes>,<$settingValues>)"
    }

    override fun parseArguments() {
        val settingType = args[0].replace(" ", "_").uppercase().replace("'", "")
        val settingValue = args[1].uppercase().replace("'", "")

        try {
            this.settingType = SettingType.valueOf(settingType)
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException(
                "Invalid setting type: '$settingType', " +
                        "must be one of ${SettingType.entries.joinToString { it.name }}",
            )
        }

        try {
            this.settingValue = SettingValue.valueOf(settingValue)
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Invalid setting value: '$settingValue', " +
                    "must be one of ${SettingValue.entries.joinToString { it.name }}")
        }
    }

    private suspend fun handleWifiSetting(state: Boolean): CommandResponse{
        if(!permissionManager.checkAccessWifiState()){
            return CommandResponse(
                isSuccess = false,
                message = "Access wifi state permission is not granted",
                getResponse = true
            )
        }

        if(AppTools.isWifiEnabled(context) == state){
            return CommandResponse(
                isSuccess = true,
                message = "Wifi is already ${if (state) "on" else "off"}",
                getResponse = true
            )
        }

        if(!permissionManager.isAccessibilityPermissionGranted(context)){
            return CommandResponse(
                isSuccess = false,
                message = "Accessibility permission is not granted",
                getResponse = true
            )
        }

        val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

        WIFI_SUB_ROUTINE.active = true
        val response = startRoutine(context, QUICK_SETTINGS_ROUTINE, intent)
        cancelAllSubRoutines()
        return response
    }

    private fun handleTorchSetting(state: Boolean): CommandResponse{
        val cameraManager = context.getSystemService(CameraManager::class.java)
        val flashAvailable = cameraManager
            .getCameraCharacteristics("0")
            .get(CameraCharacteristics.FLASH_INFO_AVAILABLE)

        if (flashAvailable != true) {
            return CommandResponse(
                isSuccess = false,
                message = "Device does not support torch",
                getResponse = true
            )
        }

        try {
            val cameraId = cameraManager.cameraIdList[0]
            cameraManager.setTorchMode(cameraId, state)
        } catch (ex: Exception){
            Timber.e("[${::handleTorchSetting.name}] Error: $ex")
            return CommandResponse(
                isSuccess = false,
                message = "Error while turning torch ${if (state) "on" else "off"}: ${ex.message}",
                getResponse = true
            )
        }

        return CommandResponse(
            isSuccess = true,
            message = "Torch was turned ${if (state) "on" else "off"}",
            getResponse = false
        )
    }

    @SuppressLint("MissingPermission")
    private suspend fun handleBluetoothSetting(state: Boolean): CommandResponse{
        val bluetoothManager: BluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter

        return if (bluetoothAdapter == null) {
            CommandResponse(
                isSuccess = true,
                message = "Device does not support Bluetooth",
                getResponse = true
            )
        } else {
            if(state == bluetoothAdapter.isEnabled){
                return CommandResponse(
                    isSuccess = true,
                    message = "Bluetooth is already ${if (state) "on" else "off"}",
                    getResponse = true
                )
            }

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !permissionManager.checkBluetoothConnect()) {
                val permissionDenied = CompletableDeferred<Boolean>()
                permissionManager.requestSinglePermission(
                    permission = Manifest.permission.BLUETOOTH_CONNECT,
                    onPermissionGranted = { permissionDenied.complete(false) },
                    onPermissionDenied = { permissionDenied.complete(true) }
                )

                if(permissionDenied.await()){
                    return CommandResponse(
                        isSuccess = false,
                        message = "Bluetooth permission was denied by user",
                        getResponse = true
                    )
                }
            }

            return if (state) {
                val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)

                CommandResponse(
                    isSuccess = true,
                    message = "Bluetooth was turned on",
                    getResponse = false
                )
            } else {
                val intent = Intent("android.bluetooth.adapter.action.REQUEST_DISABLE")
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)

                CommandResponse(
                    isSuccess = true,
                    message = "Bluetooth was turned off",
                    getResponse = false
                )
            }
        }
    }

    private suspend fun handleMobileDataSetting(state: Boolean): CommandResponse{
        if(AppTools.isMobileDataEnabled(context) == state){
            return CommandResponse(
                isSuccess = true,
                message = "Mobile data is already ${if (state) "on" else "off"}",
                getResponse = true
            )
        }

        if(!permissionManager.isAccessibilityPermissionGranted(context)){
            return CommandResponse(
                isSuccess = false,
                message = "Accessibility permission is not granted",
                getResponse = true
            )
        }

        val intent = Intent(Settings.ACTION_DATA_USAGE_SETTINGS)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

        MOBILE_DATA_SUB_ROUTINE.active = true
        val response = startRoutine(context, QUICK_SETTINGS_ROUTINE, intent)
        cancelAllSubRoutines()
        return response
    }

    override suspend fun execute(): CommandResponse {
       return when(settingType){
           SettingType.WIFI -> handleWifiSetting(settingValue == SettingValue.ON)
           SettingType.BLUETOOTH -> handleBluetoothSetting(settingValue == SettingValue.ON)
           SettingType.MOBILE_DATA -> handleMobileDataSetting(settingValue == SettingValue.ON)
           SettingType.TORCH -> handleTorchSetting(settingValue == SettingValue.ON)
       }
    }
}