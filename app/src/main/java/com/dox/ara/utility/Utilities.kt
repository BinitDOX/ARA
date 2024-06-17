package com.truecrm.rat.utility

import android.annotation.SuppressLint
import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ListenableWorker
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.await
import com.dox.ara.utility.Constants.ENCRYPTION_ALGORITHM
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.nio.charset.StandardCharsets
import java.time.Duration
import java.util.Base64
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec


@SuppressLint("RestrictedApi")
inline fun <reified T : ListenableWorker> scheduleJob(context: Context, inputData: Data,
                                                      crossinline onSuccess: () -> Unit = {},
                                                      crossinline onError: () -> Unit = {}) {
    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    val workRequest = OneTimeWorkRequest.Builder(T::class.java)
        .setConstraints(constraints)
        .setInputData(inputData)
        .setBackoffCriteria(
            BackoffPolicy.EXPONENTIAL,
            30,
            TimeUnit.SECONDS
        )
        .build()

    val workManager = WorkManager.getInstance(context)
    workManager.enqueue(workRequest)

    CoroutineScope(Dispatchers.IO).launch {
        val maxChecks = 15
        var currentChecks = 0
        var delay = 10L
        while (true) {
            delay(Duration.ofSeconds(delay).toMillis())

            val listenableFuture = workManager.getWorkInfoById(workRequest.id)

            val workInfo = listenableFuture.await()

            when (workInfo.state) {
                WorkInfo.State.ENQUEUED -> {}
                WorkInfo.State.RUNNING -> {}
                WorkInfo.State.SUCCEEDED -> {
                    onSuccess.invoke()
                    return@launch
                }
                WorkInfo.State.FAILED -> {
                    onError.invoke()
                    return@launch
                }
                else -> {}
            }

            if(++currentChecks >= maxChecks){
                Timber.w("[scheduleJob] Worker callback timeout")
            }
            delay *= 2
        }
    }
}

fun generateDeviceId(): String? {
    return UUID.randomUUID().toString().replace("-", "").take(10)
}

fun encrypt(input: String, key: String): String {
    val cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM)
    val secretKeySpec = SecretKeySpec(key.toByteArray(StandardCharsets.UTF_8), ENCRYPTION_ALGORITHM)
    cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec)
    val encryptedBytes: ByteArray = cipher.doFinal(input.toByteArray(StandardCharsets.UTF_8))
    return Base64.getUrlEncoder().encodeToString(encryptedBytes)
}

fun getWorkerName(name: String): String{
    return "${name}_worker"
}

suspend fun waitForCondition(maxWaitTimeMillis: Long = 30000L, condition: () -> Boolean) {
    val startTimeMillis = System.currentTimeMillis()
    while (condition.invoke()) {
        if (System.currentTimeMillis() - startTimeMillis >= maxWaitTimeMillis) {
            Timber.w("[waitForCondition] Timeout reached (${maxWaitTimeMillis / 1000} seconds)")
            break
        }
        delay(1000)
    }
}

private fun levenshteinDistance(lhs: CharSequence, rhs: CharSequence): Int {
    val lhsLength = lhs.length
    val rhsLength = rhs.length

    var cost = Array(lhsLength + 1) { it }
    var newCost = Array(lhsLength + 1) { 0 }

    for (i in 1..rhsLength) {
        newCost[0] = i

        for (j in 1..lhsLength) {
            val match = if (lhs[j - 1] == rhs[i - 1]) 0 else 1

            val costReplace = cost[j - 1] + match
            val costInsert = cost[j] + 1
            val costDelete = newCost[j - 1] + 1

            newCost[j] = minOf(costInsert, costDelete, costReplace)
        }

        val swap = cost
        cost = newCost
        newCost = swap
    }

    return cost[lhsLength]
}

fun stringSimilarity(s1: String, s2: String): Double {
    val longer = if (s1.length > s2.length) s1 else s2
    val shorter = if (s1.length > s2.length) s2 else s1

    val longerLength = longer.length
    if (longerLength == 0) { return 1.0 }
    return (longerLength - levenshteinDistance(longer, shorter)) / longerLength.toDouble()
}