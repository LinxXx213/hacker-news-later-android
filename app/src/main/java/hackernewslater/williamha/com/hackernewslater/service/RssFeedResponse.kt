package hackernewslater.williamha.com.hackernewslater.service

import java.util.*

/**
 * Created by williamha on 9/3/18.
 */
data class RssFeedResponse(var title: String = "", var description: String = "",
                           var summary: String = "", var image: String = "",
                           var lastUpdated: Date = Date(),
                           var episodes: MutableList<EpisodeResponse>? = null
                            ) {

    data class EpisodeResponse(var title: String? = null, var link: String? = null,
                               var description: String? = null, var guid: String? = null,
                               var pubDate: String? = null, var duration: String? = null,
                               var url: String? = null, var type: String? = null)
}