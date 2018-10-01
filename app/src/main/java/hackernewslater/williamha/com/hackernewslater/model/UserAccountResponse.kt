package hackernewslater.williamha.com.hackernewslater.model

/**
 * Created by williamha on 7/31/18.
 */
data class UserAccountResponse(val enabled: Boolean, val hasItemsInList: Boolean, val maxItemsToSend: Int,
           val hourToSendItems: Int, val minuteToSendItems: Int, val savedNewsItemsList: List<NewsItem>?, val numberOfImmediateReads: Int) {


    fun userAccountResponseToUserAccount(): UserAccount {
        return UserAccount(savedNewsItemsList, enabled, TimeToSend(hourToSendItems, minuteToSendItems), maxItemsToSend, numberOfImmediateReads)
    }
}

data class UserAccountQueuedItemsResponse(val items: List<Long>)

