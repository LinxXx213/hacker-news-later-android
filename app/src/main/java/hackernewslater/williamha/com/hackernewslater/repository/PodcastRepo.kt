package hackernewslater.williamha.com.hackernewslater.repository

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import hackernewslater.williamha.com.hackernewslater.HN_PODCAST_URL
import hackernewslater.williamha.com.hackernewslater.db.NewsItemDatabase
import hackernewslater.williamha.com.hackernewslater.db.PodcastDao
import hackernewslater.williamha.com.hackernewslater.exceptions.HNLException
import hackernewslater.williamha.com.hackernewslater.model.Episode
import hackernewslater.williamha.com.hackernewslater.model.Podcast
import hackernewslater.williamha.com.hackernewslater.model.Resource
import hackernewslater.williamha.com.hackernewslater.service.FeedService
import hackernewslater.williamha.com.hackernewslater.ui.utilities.HNLDateTime
import hackernewslater.williamha.com.hackernewslater.viewmodel.PodcastViewModel.Companion.rssFeedResponseToPodcast
import hackernewslater.williamha.com.hackernewslater.viewmodel.PodcastViewModel.Companion.rssItemsToEpisodes
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch

/**
 * Created by williamha on 9/3/18.
 */
class PodcastRepo(private var feedService: FeedService, private val context: Context) {

    private val db: NewsItemDatabase = NewsItemDatabase.getInstance(context)
    private val podcastDao: PodcastDao = db.podcastDao()

    fun fetchPodcasts(): LiveData<Podcast> {
        networkFetchPodcast()
        return podcastDao.loadPodcast(1)
    }

    fun fetchEpisodes(): LiveData<List<Episode>> {
        return podcastDao.loadAllEpisodes()
    }

    fun networkFetchEpisodes() {
        feedService.getFeed(HN_PODCAST_URL, { rssFeedResponse ->
            rssFeedResponse?.let {
                it.episodes?.let {
                    val episodes = rssItemsToEpisodes(it)
                    for (episodeItem in episodes) {
                        insertEpisode(episodeItem)
                    }
                }
            }
        })
    }

    fun networkFetchPodcast() {
        feedService.getFeed(HN_PODCAST_URL, { rssFeedResponse ->

            if (rssFeedResponse != null) {
                val podcastResponse = rssFeedResponseToPodcast(HN_PODCAST_URL, rssFeedResponse)
                podcastResponse?.let {
                    launch(CommonPool) {
                        podcastDao.insertPodcast(it)
                    }
                }
            }
        })
    }

    fun insertEpisode(episode: Episode) {
        launch(CommonPool) {
            podcastDao.insertEpisode(episode)
        }
    }
}