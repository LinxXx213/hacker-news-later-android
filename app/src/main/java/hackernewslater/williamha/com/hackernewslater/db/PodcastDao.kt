package hackernewslater.williamha.com.hackernewslater.db

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy.REPLACE
import android.arch.persistence.room.Query
import hackernewslater.williamha.com.hackernewslater.model.Episode
import hackernewslater.williamha.com.hackernewslater.model.Podcast
import hackernewslater.williamha.com.hackernewslater.model.PodcastWithEpisodes

/**
 * Created by williamha on 9/5/18.
 */
@Dao
interface PodcastDao {

    @Insert(onConflict = REPLACE)
    fun insertPodcast(podcast: Podcast): Long

    @Query("SELECT * FROM Podcast")
    fun loadAll(): List<PodcastWithEpisodes>

    @Query("SELECT * FROM Podcast where id = :podcastId")
    fun loadPodcast(podcastId: Long): LiveData<Podcast>

    @Insert(onConflict = REPLACE)
    fun insertEpisode(episode: Episode): Long

    @Query("SELECT * FROM Episode where podcastId = :podcastId")
    fun loadEpisodesForPodcastId(podcastId: Long): List<Episode>

    @Query("SELECT * FROM Episode")
    fun loadAllEpisodes(): LiveData<List<Episode>>

    @Query("DELETE FROM Podcast")
    fun deleteAllItems(): Int
}