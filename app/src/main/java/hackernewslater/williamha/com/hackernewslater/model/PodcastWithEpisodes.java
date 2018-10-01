package hackernewslater.williamha.com.hackernewslater.model;

import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Relation;

import java.util.List;

/**
 * Created by williamha on 9/6/18.
 */

public class PodcastWithEpisodes {

    @Embedded public Podcast podcast;

    @Relation(parentColumn = "id", entityColumn = "podcastId") public List<Episode> episodes;
}
