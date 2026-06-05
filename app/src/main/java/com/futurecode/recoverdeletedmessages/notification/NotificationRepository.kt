package com.futurecode.recoverdeletedmessages.notification

import com.futurecode.recoverdeletedmessages.utils.getNotificationListFromPrefs

object NotificationRepository {
    val notifications = getNotificationListFromPrefs()

}