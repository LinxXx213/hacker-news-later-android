package hackernewslater.williamha.com.hackernewslater.viewmodel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import hackernewslater.williamha.com.hackernewslater.model.Episode
import hackernewslater.williamha.com.hackernewslater.model.Podcast
import hackernewslater.williamha.com.hackernewslater.repository.PodcastRepo
import hackernewslater.williamha.com.hackernewslater.service.FeedService
import hackernewslater.williamha.com.hackernewslater.service.RssFeedResponse
import hackernewslater.williamha.com.hackernewslater.ui.utilities.HNLDateTime
import java.util.*

/**
 * Created by williamha on 9/7/18.
 */
class PodcastViewModel(application: Application): AndroidViewModel(application) {

    var podcastRepo = PodcastRepo(FeedService.instance, application)

    fun returnPodcast(): LiveData<Podcast> {
        return podcastRepo.fetchPodcasts()
    }

    fun returnEpisodes(): LiveData<List<Episode>>? {
        return podcastRepo.fetchEpisodes()
    }

    fun networkFetchEpisodes() {
        podcastRepo.networkFetchEpisodes()
    }

    companion object {
        fun rssFeedResponseToPodcast(feedUrl: String, rssResponse: RssFeedResponse): Podcast? {
            val description = if (rssResponse.description == "") rssResponse.summary else rssResponse.description

            return Podcast(1, feedUrl, rssResponse.title, description, rssResponse.image, Date())
        }

        fun rssItemsToEpisodes(episodeResponses: List<RssFeedResponse.EpisodeResponse>): List<Episode> {
            return episodeResponses.map {
                Episode (it.guid ?: "",
                        1,
                        it.title ?: "",
                        it.description ?: "",
                        it.url ?: "",
                        it.type ?: "",
                        HNLDateTime.newInstance().xmlDateToDate(it.pubDate),
                        it.duration ?: "")
            }
        }
    }
}