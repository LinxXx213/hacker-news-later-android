package hackernewslater.williamha.com.hackernewslater.adapter

import android.support.v7.widget.RecyclerView
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import hackernewslater.williamha.com.hackernewslater.R
import hackernewslater.williamha.com.hackernewslater.model.Episode
import hackernewslater.williamha.com.hackernewslater.model.Podcast
import java.text.SimpleDateFormat

/**
 * Created by williamha on 9/8/18.
 */
class EpisodesItemListAdapter(private var podcast: Podcast?,
                              private var episodes: List<Episode>?,
                              private val episodesItemListAdapterListener: EpisodesItemListAdapterListener): RecyclerView.Adapter<EpisodesItemListAdapter.ViewHolder>() {

    interface EpisodesItemListAdapterListener {
        fun onEpisodeItemClicked(episode: Episode, position: Int)
    }

    inner class ViewHolder(v: View): RecyclerView.ViewHolder(v) {

        val title = v.findViewById<TextView>(R.id.episodeTitle)
        val length = v.findViewById<TextView>(R.id.episodeLength)
        val date = v.findViewById<TextView>(R.id.episodeDate)
        val description = v.findViewById<TextView>(R.id.episodeDescription)

        init {
            v.setOnClickListener {
                episodes?.let {
                    episodesItemListAdapterListener.onEpisodeItemClicked(it[layoutPosition], layoutPosition)
                }
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val episodeList = episodes ?: return

        val episode = episodeList[position]
        holder.title.text = episode.title
        holder.length.text = episode.duration

        val spf = SimpleDateFormat("MMM dd, yyyy")
        val dateString = spf.format(episode.releaseDate)

        holder.date.text = dateString
        holder.description.text = Html.fromHtml(episode.description).toString()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.episode_item, parent, false))
    }

    override fun getItemCount(): Int = episodes?.size ?: 0

    fun setPodcastData(podcast: Podcast) {
        this.podcast = podcast
        this.notifyDataSetChanged()
    }

    fun setEpisodeData(episodes: List<Episode>) {
        this.episodes = episodes
        this.notifyDataSetChanged()
    }
}