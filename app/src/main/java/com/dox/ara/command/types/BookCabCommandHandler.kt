package com.dox.ara.command.types

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.dox.ara.command.CommandHandler
import com.dox.ara.command.CommandHandlerFactory.CommandType.BOOK_CAB
import com.dox.ara.command.CommandResponse
import com.dox.ara.manager.PermissionManager
import com.dox.ara.utility.AppTools.Companion.isAppInstalled
import com.dox.ara.utility.Constants.UBER_PACKAGE_NAME
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber


class BookCabCommandHandler @AssistedInject constructor(
    @Assisted private val args : List<String>,
    @ApplicationContext private val context: Context,
    private val permissionManager: PermissionManager
) : CommandHandler(args) {
    override val numArgs = 4
    private lateinit var applicationName: CabApplication
    private lateinit var location: String

    enum class CabApplication {
        UBER,
        OLA,
    }

    override fun help(): String {
        val appNames = CabApplication.entries.joinToString("|") { it.name.lowercase() }

        return "[${BOOK_CAB.name.lowercase()}(<${appNames}>, place, city, country)]"
    }

    override fun parseArguments() {
        val applicationName = args[0].replace(" ", "_").uppercase()
            .replace("'", "").replace("\"", "")
        val location = args[1] + "," + args[2] + "," + args[3]

        this.location = location

        try {
            this.applicationName = CabApplication.valueOf(applicationName)
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException(
                "Invalid application: $applicationName, " +
                        "must be one of ${CabApplication.entries.joinToString { it.name }}",
            )
        }
    }

    private fun handleUber(): CommandResponse {
        return try {
            val uri = Uri.parse("uber://").buildUpon()
                .appendQueryParameter("action", "setPickup")  // Action
                .appendQueryParameter("pickup", "Payee my_location")  // Pick location
                .appendQueryParameter("dropoff[formatted_address]", location)  // Drop location
                .build()

            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = uri
                setPackage(UBER_PACKAGE_NAME)
            }

            return if (intent.resolveActivity(context.packageManager) != null) {
                if(!isAppInstalled(UBER_PACKAGE_NAME, context)){
                    return CommandResponse(false, "Uber app is not installed", true)
                }
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

                context.startActivity(intent)
                CommandResponse(true, "Uber ride initiated successfully", false)
            } else {
                CommandResponse(false, "Paytm app is not installed", true)
            }
        } catch (e: Exception) {
            Timber.e("[${::handleUber.name}] Error: $e")
            CommandResponse(false, "Error launching Uber: ${e.message}", true)
        }
    }

    private fun handleOla(): CommandResponse {
        return CommandResponse(
            true,
            "Not implemented yet",
            true
        )
    }

    override suspend fun execute(chatId: Long): CommandResponse {
       return when(applicationName){
           CabApplication.UBER -> handleUber()
           CabApplication.OLA -> handleOla()
       }
    }
}