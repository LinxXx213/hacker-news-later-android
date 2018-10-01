package hackernewslater.williamha.com.hackernewslater

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import hackernewslater.williamha.com.hackernewslater.db.NewsItemDatabase
import hackernewslater.williamha.com.hackernewslater.model.Episode
import hackernewslater.williamha.com.hackernewslater.model.Podcast
import junit.framework.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

/**
 * Created by williamha on 9/5/18.
 */
@RunWith(AndroidJUnit4::class)

class PodcastTest {

    private var podcastDatabase: NewsItemDatabase? = null

    @Before
    fun initDb() {
        NewsItemDatabase.TEST_MODE = true
        podcastDatabase = NewsItemDatabase.getInstance(InstrumentationRegistry.getTargetContext())
    }

    @Test
    fun testPodcastInsert() {

        val podcast = Podcast(null, "test podcast url", "test podcast title",
                "test podcast description", "test podcast image url",
                Date())

        val podcastId = podcastDatabase?.podcastDao()?.insertPodcast(podcast)

        val first = Episode("1", podcastId, "test title", "test description",
                "test url", "test mime", Date(), "1")

        val second = Episode("2", podcastId, "test title", "test description",
                "test url", "test mime", Date(), "1")

        val third = Episode("3", podcastId, "test title", "test description",
                "test url", "test mime", Date(), "1")

        podcastDatabase?.podcastDao()?.insertEpisode(first)
        podcastDatabase?.podcastDao()?.insertEpisode(second)
        podcastDatabase?.podcastDao()?.insertEpisode(third)

        podcast.episodes = listOf(first, second, third)


        var fetchedPodcast = podcastDatabase?.podcastDao()?.loadAll()

        fetchedPodcast?.let {
            it[0].episodes.sortBy { it.guid }
        }

        assertTrue(fetchedPodcast!![0].episodes.hasSameContents(listOf(first, second, third)))
    }

    fun <T> Collection<T>.hasSameContents(collection: Collection<T>): Boolean {

        if ((collection.size == this.size) && this.containsAll(collection)) {
            return true
        }

        return false

    }
}