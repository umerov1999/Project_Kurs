package dev.umerov.project.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import dev.umerov.project.R

object AppNotificationChannels {
    val schedulePurchaseChannelId: String
        get() = "schedule_purchase_channel"

    @RequiresApi(api = Build.VERSION_CODES.O)
    fun getSchedulePurchaseChannel(context: Context): NotificationChannel {
        val channelName = context.getString(R.string.purchase)
        val channel =
            NotificationChannel(
                schedulePurchaseChannelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            )
        channel.enableLights(true)
        channel.enableVibration(true)
        return channel
    }
}