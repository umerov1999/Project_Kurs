package dev.umerov.project.util

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import dev.umerov.project.Extra
import dev.umerov.project.R
import dev.umerov.project.activity.MainActivity
import dev.umerov.project.model.main.labs.ShoppingList
import dev.umerov.project.place.PlaceFactory
import dev.umerov.project.util.serializeble.msgpack.MsgPack

class NotificationHelper : BroadcastReceiver() {
    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context, intent: Intent) {
        val shoppingList = MsgPack.decodeFromByteArrayEx(
            ShoppingList.serializer(),
            intent.getByteArrayExtra(Extra.DATA) ?: return
        )
        val manager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
        if (Utils.hasOreo()) {
            manager?.createNotificationChannel(
                AppNotificationChannels.getSchedulePurchaseChannel(
                    context
                )
            )
        }
        val builder =
            NotificationCompat.Builder(context, AppNotificationChannels.schedulePurchaseChannelId)
                .setSmallIcon(R.drawable.client_round)
                .setContentTitle(shoppingList.title)
                .setContentText(shoppingList.description)
                .setStyle(NotificationCompat.BigTextStyle().bigText(shoppingList.description))
                .setAutoCancel(true)
        builder.priority = NotificationCompat.PRIORITY_HIGH
        val intentOpen = Intent(context, MainActivity::class.java)
        intentOpen.putExtra(Extra.PLACE, PlaceFactory.getShoppingProductPlace(shoppingList))
        intentOpen.action = MainActivity.ACTION_OPEN_PLACE
        intentOpen.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        val contentIntent = PendingIntent.getActivity(
            context,
            shoppingList.db_id.hashCode(),
            intentOpen,
            Utils.makeMutablePendingIntent(PendingIntent.FLAG_CANCEL_CURRENT)
        )
        builder.setContentIntent(contentIntent)
        val notification = builder.build()
        if (AppPerms.hasNotificationPermissionSimple(context)) {
            manager?.notify(
                "schedulePurchase" + shoppingList.db_id,
                NOTIFICATION_SCHEDULE_PURCHASE,
                notification
            )
        }
    }

    companion object {
        const val NOTIFICATION_SCHEDULE_PURCHASE = 10
    }
}