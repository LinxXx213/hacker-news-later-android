package hackernewslater.williamha.com.hackernewslater

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import hackernewslater.williamha.com.hackernewslater.db.NewsItemDatabase
import hackernewslater.williamha.com.hackernewslater.model.NewsItem
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

/**
 * Created by williamha on 7/29/18.
 */

@RunWith(AndroidJUnit4::class)
class NewsItemTest {

    private var newsItemDatabase: NewsItemDatabase? = null

    @Before
    fun initDb() {
        NewsItemDatabase.TEST_MODE = true
        newsItemDatabase = NewsItemDatabase.getInstance(InstrumentationRegistry.getTargetContext())
    }

    @Test
    fun ensureItemsSavedNoLoggedInIsPreserved() {
        val item1 = NewsItem(1, "will", "title", "https://www.example.com", 0, 1234, true, false)
        val item2 = NewsItem(2, "will", "title", "https://www.example.com", 0, 1234, true, false)
        val item3 = NewsItem(3, "will", "title", "https://www.example.com", 0, 1234, true, false)

        newsItemDatabase?.newsItemDao()?.insertNewsItem(item1)
        newsItemDatabase?.newsItemDao()?.insertNewsItem(item2)
        newsItemDatabase?.newsItemDao()?.insertNewsItem(item3)


        var item4 = NewsItem(4, "will", "title", "https://www.example.com", 0, 12345, true, true)
        newsItemDatabase?.newsItemDao()?.insertNewsItem(item4)

        val items = newsItemDatabase?.newsItemDao()?.loadNewsItemsSavedButNotYetAddedToQueue()
        val expectedItems = listOf(item1, item2, item3)

        assertEquals(items, expectedItems)
    }
}