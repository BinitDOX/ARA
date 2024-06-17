package com.dox.ara.repository

import android.content.Context
import com.dox.ara.BuildConfig
import com.dox.ara.api.AuthenticationAPI
import com.dox.ara.manager.SharedPreferencesManager
import com.dox.ara.utility.Constants
import com.dox.ara.utility.Constants.AUTH_TOKEN_KEY
import com.dox.ara.utility.Constants.DEVICE_ID_KEY
import com.truecrm.rat.utility.encrypt
import com.truecrm.rat.utility.generateDeviceId
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.util.Calendar
import javax.inject.Inject


class AuthenticationRepository @Inject constructor(@ApplicationContext private val context: Context,
                                                   private val authenticationAPI: AuthenticationAPI,
                                                   private val sharedPrefsManager: SharedPreferencesManager
) {

    suspend fun getAuthToken(): String? {
        Timber.i("[${::getAuthToken.name}] Requesting new token")

        val deviceId = sharedPrefsManager.get(DEVICE_ID_KEY) ?:generateDeviceId()
        if(deviceId == null){
            Timber.e("[${::getAuthToken.name}] Device ID is null")
            return null
        }
        val authKey = deviceId + Constants.DELIMITER_DATA_DATE + Calendar.getInstance().timeInMillis
        val encKey = BuildConfig.ENCRYPTION_KEY

        val authKeyEncrypted = encrypt(authKey, encKey)

        val response = authenticationAPI.getAuthToken(authKeyEncrypted)
        return if(response.isSuccessful && response.body()?.isSuccess == true && response.body() != null) {
            Timber.d("[${::getAuthToken.name}] Response: " + response.body()?.payload)
            sharedPrefsManager.save(AUTH_TOKEN_KEY, response.body()!!.payload.toString())
            sharedPrefsManager.save(DEVICE_ID_KEY, deviceId)
            response.body()!!.payload.toString()
        } else {
            Timber.e("[${::getAuthToken.name}] Response: " +
                    if (!response.isSuccessful) response.errorBody().toString() else response.body())
            null
        }
    }
}