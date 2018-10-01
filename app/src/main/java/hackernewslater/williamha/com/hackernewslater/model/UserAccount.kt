package hackernewslater.williamha.com.hackernewslater.model

/**
 * Created by williamha on 7/11/18.
 */
data class UserAccount(val savedItems: List<NewsItem>?, val enabled: Boolean, val timeToSend: TimeToSend,
                       val maxItemToSend: Int, val numberOfImmediateReads: Int)

data class EmailPassword(val email: String, val password: String)
data class TimeToSend(val hourToSend: Int, val minuteToSend: Int)