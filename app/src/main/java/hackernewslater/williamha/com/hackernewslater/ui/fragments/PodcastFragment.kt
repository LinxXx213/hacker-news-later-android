package hackernewslater.williamha.com.hackernewslater.ui.fragments

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.ComponentName
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.squareup.picasso.Picasso
import hackernewslater.williamha.com.hackernewslater.R
import hackernewslater.williamha.com.hackernewslater.adapter.EpisodesItemListAdapter
import hackernewslater.williamha.com.hackernewslater.model.Episode
import hackernewslater.williamha.com.hackernewslater.model.Podcast
import hackernewslater.williamha.com.hackernewslater.service.HNLMediaService
import hackernewslater.williamha.com.hackernewslater.viewmodel.PodcastViewModel
import kotlinx.android.synthetic.main.fragment_podcast.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch

/**
 * Created by williamha on 9/7/18.
 */
class PodcastFragment: Fragment(), EpisodesItemListAdapter.EpisodesItemListAdapterListener {

    private var podcastViewModel: PodcastViewModel? = null
    private lateinit var episodeListItemAdapter: EpisodesItemListAdapter

    // Media

    private lateinit var mediaBrowser: MediaBrowserCompat
    private var mediaControllerCallback: MediaControllerCallback? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_podcast, container, false)

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        setupViewModels()

        val layoutManager = LinearLayoutManager(context)
        episodeListItemAdapter = EpisodesItemListAdapter(Podcast(), mutableListOf(),this)

        val dividerItemDecorator = DividerItemDecoration(episodeRecyclerView.context, layoutManager.orientation)
        episodeRecyclerView.addItemDecoration(dividerItemDecorator)

        episodeRecyclerView.layoutManager = layoutManager
        episodeRecyclerView.adapter = episodeListItemAdapter

        podcastViewModel?.returnPodcast()?.observe(this, Observer {
            it?.let {

                Picasso.get().load(it.imageUrl).into(podcastImageView)

                podcastTitle.text = it.title

                episodeListItemAdapter.setPodcastData(it)
            }
        })

        podcastViewModel?.returnEpisodes()?.observe(this, Observer {
            if (it == null || it.isEmpty()) {
                podcastViewModel?.networkFetchEpisodes()
            } else {
                episodeListItemAdapter.setEpisodeData(it)
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initMediaBrowser()
    }

    override fun onStart() {
        super.onStart()

        if (mediaBrowser.isConnected) {
            if (MediaControllerCompat.getMediaController(activity as Activity) == null) {
                registerMediaController(mediaBrowser.sessionToken)
            }
        } else {
            mediaBrowser.connect()
        }
    }

    override fun onStop() {
        super.onStop()

        if (MediaControllerCompat.getMediaController(activity as Activity) != null) {
            mediaControllerCallback?.let {
                MediaControllerCompat.getMediaController(activity as Activity)
                        .unregisterCallback(it)
            }
        }
    }

    private fun setupViewModels() {
        podcastViewModel = ViewModelProviders.of(this).get(PodcastViewModel::class.java)
    }

    // EpisodesItemListAdapterListener

    override fun onEpisodeItemClicked(episode: Episode, position: Int) {

        var controller = MediaControllerCompat.getMediaController(activity as Activity)

        if (controller.playbackState != null) {
            if (controller.playbackState.state == PlaybackStateCompat.STATE_PLAYING) {
                controller.transportControls.pause()
            } else {
                startPlaying(episode)
            }
        } else {
            startPlaying(episode)
        }
    }

    private fun startPlaying(episodeViewData: Episode) {
        launch(CommonPool) {
            val controller = MediaControllerCompat.getMediaController(activity as Activity)
            controller.transportControls.playFromUri(Uri.parse(episodeViewData.mediaUrl), null)
        }
    }

    private fun initMediaBrowser() {
        mediaBrowser = MediaBrowserCompat(activity, ComponentName(activity, HNLMediaService::class.java),
                MediaBrowserCallBacks(), null)
    }

    private fun registerMediaController(token: MediaSessionCompat.Token) {

        // This is separate from the MediaController class in Android widget
        val mediaController = MediaControllerCompat(activity, token)

        MediaControllerCompat.setMediaController(activity as Activity, mediaController)

        mediaControllerCallback = MediaControllerCallback()
        mediaController.registerCallback(mediaControllerCallback!!)
    }

    inner class MediaBrowserCallBacks: MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            super.onConnected()

            registerMediaController(mediaBrowser.sessionToken)
            println("onConnected")
        }

        override fun onConnectionSuspended() {
            super.onConnectionSuspended()

            println("onConnectionSuspended")
            // Disable transport controls
        }

        override fun onConnectionFailed() {
            super.onConnectionFailed()
            println("onConnectionFailed")
            // Fatal error handling
        }
    }

    inner class MediaControllerCallback: MediaControllerCompat.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            super.onMetadataChanged(metadata)

            println("metadata changed to ${metadata?.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI)}")
        }

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            super.onPlaybackStateChanged(state)
            println("state changed to $state")
        }
    }

    companion object {
        fun newInstance(): PodcastFragment {
            return PodcastFragment()
        }
    }
}