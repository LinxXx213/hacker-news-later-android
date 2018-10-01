package hackernewslater.williamha.com.hackernewslater.ui.fragments

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.app.Fragment
import android.support.v4.content.LocalBroadcastManager
import android.support.v4.util.Pair
import android.support.v7.app.AlertDialog
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import hackernewslater.williamha.com.hackernewslater.*

import hackernewslater.williamha.com.hackernewslater.adapter.NewsItemListAdapter
import hackernewslater.williamha.com.hackernewslater.managers.HNLAccountManager
import hackernewslater.williamha.com.hackernewslater.model.NewsItem
import hackernewslater.williamha.com.hackernewslater.model.Resource
import hackernewslater.williamha.com.hackernewslater.repository.HackerNewsApiRepo
import hackernewslater.williamha.com.hackernewslater.repository.HackerNewsRepo
import hackernewslater.williamha.com.hackernewslater.service.HackerNewsApiService
import hackernewslater.williamha.com.hackernewslater.service.HackerNewsService
import hackernewslater.williamha.com.hackernewslater.ui.CommentActivity
import hackernewslater.williamha.com.hackernewslater.ui.LoginActivity
import hackernewslater.williamha.com.hackernewslater.ui.utilities.Toasty
import hackernewslater.williamha.com.hackernewslater.viewmodel.NewsItemViewModel
import kotlinx.android.synthetic.main.fragment_news_item_list.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch

class NewsItemListFragment : Fragment(), NewsItemListAdapter.NewsItemListAdapterListener {
    private lateinit var newsItemListAdapter: NewsItemListAdapter
    private var mListener: OnNewsItemPulledListener? = null
    var accountManager: HNLAccountManager? = null
    var offlineMode = false
    var newsItemType = NewsItemViewModel.NewsItemType.TOP

    private var broadcastReceiver: BroadcastReceiver? = null

    // Models
    private var newsItemViewModel: NewsItemViewModel? = null


    interface OnNewsItemPulledListener {
        fun onNewsItemPulledStopped()
        fun onNewsItemClicked(item: NewsItem, position: Int)
        fun onNewsItemCallStart()
    }

    companion object {
        fun newInstance(): NewsItemListFragment {
            return NewsItemListFragment()
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is OnNewsItemPulledListener) {
            mListener = context
        } else {
            throw RuntimeException(context!!.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        context?.let {
            accountManager = HNLAccountManager.getInstance(it)
        }
    }

    override fun onResume() {
        super.onResume()

        registerForBroadcastReceiver()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_news_item_list, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        setupViewModels()

        val layoutManager = LinearLayoutManager(context)

        val itemRecyclerView = view?.findViewById<RecyclerView>(R.id.itemRecyclerView)
        if (itemRecyclerView != null) {
            itemRecyclerView.layoutManager = layoutManager

            val dividerItemDecorator = DividerItemDecoration(itemRecyclerView.context, layoutManager.orientation)
            itemRecyclerView.addItemDecoration(dividerItemDecorator)

            newsItemListAdapter = NewsItemListAdapter(emptyList(), this)
            itemRecyclerView.adapter = newsItemListAdapter

            fetchNews(NewsItemViewModel.NewsItemType.TOP)
        }
    }

    override fun onPause() {
        super.onPause()
        context?.let {
            broadcastReceiver?.let { broadcastReceiver ->
                LocalBroadcastManager.getInstance(it).unregisterReceiver(broadcastReceiver)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    fun fetchNews(type: NewsItemViewModel.NewsItemType) {

        newsItemType = type

        if (newsItemViewModel == null) {
            setupViewModels()
        }

        mListener?.onNewsItemCallStart()

        newsItemViewModel?.returnNewsItems(type, offlineMode)?.observe(this, Observer<Resource<List<NewsItem>>> { resource ->
            if (resource != null) {
                when (resource.status) {
                    Resource.Status.SUCCESS -> {
                        resource.data?.let { newsItems ->
                            newsItemListAdapter.setNewsItemData(newsItems)
                            mListener?.onNewsItemPulledStopped()
                        }
                    }

                    Resource.Status.ERROR -> {
                        resource.exception?.let { exception ->
                            mListener?.onNewsItemPulledStopped()
                            exception.message?.let {
                                var toast = Toasty(activity as Context, exception.message).toast
                                toast.setGravity(Gravity.CENTER, 0, 0)
                                toast.show()
                            }
                        }
                    } else -> { }
                }
            }
        })
    }

    private fun setupViewModels() {
        if (!isAdded) return

        val service = HackerNewsService.instance

        newsItemViewModel = ViewModelProviders.of(this).get(NewsItemViewModel::class.java)
        context?.let {
            newsItemViewModel?.hackerNewsRepo = HackerNewsRepo(it, service)
        }
    }

    override fun onNewsItemClicked(item: NewsItem, position: Int) {
        context?.let { context ->
            val viewForPosition = itemRecyclerView.layoutManager.findViewByPosition(position)
            val itemTitle = viewForPosition.findViewById<View>(R.id.newsItemTitle)
            val queueButton = viewForPosition.findViewById<View>(R.id.addItemToQueueButton)

            val newsItemPair = Pair(itemTitle, "newsItem")
            val queueButtonPair = Pair(queueButton!!, "queueButton")

            val intent =  Intent(context, CommentActivity::class.java)
            intent.putExtra("title", item.title)
            intent.putExtra("itemId", item.hackerNewsItemId)
            intent.putExtra("position", position)
            intent.putExtra("network", offlineMode)

            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(context as Activity, newsItemPair, queueButtonPair)
            startActivityForResult(intent, 1000, options.toBundle())
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1000 && resultCode == Activity.RESULT_OK) {
            data?.let {
                val itemId = it.getLongExtra("itemId", 0)
                val position = it.getIntExtra("position", 0)

                if (itemId != 0L) {
                    launch(CommonPool) {
                        val newsItem = newsItemViewModel?.returnItemWithId(itemId)
                        newsItem?.let {
                            launch(UI) { newsItemListAdapter.updateListWithItem(it, position, NewsItemViewModel.NewsItemType.TOP) }
                        }
                    }
                }
            }
        }
    }

    override fun onNewsItemAdded(item: NewsItem, position: Int) {
        addNewsItem(item, position)
    }

    fun addNewsItem(item: NewsItem, position: Int) {
        launch(CommonPool) {

            toggleFavoriteUnFavoriteInDb(item.hackerNewsItemId) { newsItemWithNewStateReflected->
                if (offlineMode) {
                    launch(UI) {
                        if (newsItemWithNewStateReflected.isSaved) {
                            Toasty(activity as Context, "Item added in offline mode.").toast.show()
                        } else {
                            Toasty(activity as Context, "Item removed in offline mode.").toast.show()
                        }
                    }
                } else {
                    val apiRepo = returnHackerNewsApiRepo()
                    apiRepo?.let {
                        // Determine if the item was just favorited in the database:
                        var itemWasFavorited = newsItemWithNewStateReflected.isSaved

                        // Attempt to the call to queue the item on the server. Verification check is handled by the server.
                        if (isLoggedIn()) {
                            if (itemWasFavorited) {
                                addItems(listOf(newsItemWithNewStateReflected), apiRepo)
                            } else {
                                removeItems(listOf(newsItemWithNewStateReflected), apiRepo)
                            }
                        }
                    }
                }

                // Update the state of the item on the UI regardless of whether the call happened to the server
                launch(UI) {
                    updateListWithItem(newsItemWithNewStateReflected, position)
                    if (!isLoggedIn()) {
                        promptUserLogin()
                    }
                }
            }
        }
    }

    private fun toggleFavoriteUnFavoriteInDb(id: Long, callback:(updatedItem: NewsItem) -> Unit) {
        launch(CommonPool) {
            val newsItem = newsItemViewModel?.returnItemWithId(id)
            newsItem?.let {
                it.isSaved = !it.isSaved
                newsItemViewModel?.updateNewsItem(it)
                callback(it)
            }
        }
    }

    private fun addItems(newsItems: List<NewsItem>, apiRepo: HackerNewsApiRepo) {

        apiRepo.addItems(newsItems) { success, exception ->
            for (item in newsItems) {
                launch(CommonPool) {
                    item.addedToQueue = true
                    item.currentlyQueued = true
                    newsItemViewModel?.updateNewsItem(item)
                }
            }

            if (success) {
                launch(UI) {
                    var message = "Item added to queue!"
                    if (newsItems.size > 1) {
                        message = "%s total items have been added to your queue".format(newsItems.size)
                    }

                    launch(UI) {
                        Toasty(apiRepo.context, message).toast.show()
                    }
                }
            } else {
                exception?.message?.let {
                    launch(UI) {
                        Toasty(apiRepo.context, it).toast.show()
                    }
                }
            }
        }
    }

    private fun removeItems(newsItem: List<NewsItem>, apiRepo: HackerNewsApiRepo) {
        apiRepo.deleteNewsItems(newsItem) { success, exception ->
            if (success) {
                launch(CommonPool) {
                    newsItem.map {
                        it.addedToQueue = false
                        it.currentlyQueued = false
                        newsItemViewModel?.updateNewsItem(it)
                    }
                }
            }
        }
        var message = "Item removed"
        if (newsItem.size > 1) {
            message = "%s total items have been removed from your queue".format(newsItem.size)
        }

        launch(UI) {
            Toasty(apiRepo.context, message).toast.show()
        }
    }

    private fun isLoggedIn(): Boolean {
        accountManager?.let {
            when (it.isLoggedIn()) {
                true -> return true
                false -> {
                    return false
                }
            }
        }
        return false
    }

    private fun promptUserLogin() {
        if (offlineMode) return

        launch(CommonPool) {
            val allItems = newsItemViewModel?.returnAllSavedItems()
            launch(UI) {
                getContext()?.let {
                    val alertDialog = AlertDialog.Builder(it)
                    alertDialog.setMessage("You are not logged in. Sign up and login to " +
                            "ensure your %d saved stories are delivered to your inbox.".format(allItems?.count()))
                            .setPositiveButton("Sign up", { _, _ ->
                                val intent = Intent(it, LoginActivity::class.java)
                                intent.putExtra(SELECTED_PATH, REGISTER_PATH)
                                startActivity(intent)
                            })
                            .setNegativeButton("Log in", {_, _ ->
                                val intent = Intent(it, LoginActivity::class.java)
                                intent.putExtra(SELECTED_PATH, LOGIN_PATH)
                                startActivity(intent)
                            }).show()
                }
            }
        }
    }

    private fun registerForBroadcastReceiver() {
        broadcastReceiver = object: BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                context?.let {
                    intent?.let {
                        accountManager?.let { _ ->
                            if (it.action == USER_AUTH_STATE_CHANGED && isLoggedIn()) {
                                addItemsFavoritedButNotQueued()
                            }
                        }
                    }
                }
            }
        }

        context?.let { context ->
            broadcastReceiver?.let { broadcastReceiver ->
                LocalBroadcastManager.getInstance(context).registerReceiver(broadcastReceiver, IntentFilter(USER_AUTH_STATE_CHANGED))
            }
        }
    }

    fun addItemsFavoritedButNotQueued() {
        if (!isLoggedIn()) return

        context?.let { context ->
            launch(CommonPool) {
                val itemsToQueue = newsItemViewModel?.newsItemRepo?.returnSavedNewsItemsButNotQueued()
                itemsToQueue?.let {
                    if (it.isNotEmpty()) {
                        val apiRepo = HackerNewsApiRepo(context, HackerNewsApiService.instance)
                        addItems(itemsToQueue, apiRepo)
                    }
                }
            }
        }
    }

    fun removeItemsQueuedButUnsaved() {
        if (!isLoggedIn()) return

        context?.let { context ->
            launch(CommonPool) {
                val itemsToQueue = newsItemViewModel?.newsItemRepo?.returnItemsAddedToQueueButUnsaved()
                itemsToQueue?.let {
                    if (it.isNotEmpty()) {
                        val apiRepo = HackerNewsApiRepo(context, HackerNewsApiService.instance)
                        removeItems(itemsToQueue, apiRepo)
                    }
                }
            }
        }

    }

    private fun returnHackerNewsApiRepo(): HackerNewsApiRepo? {
        context?.let {
            return HackerNewsApiRepo(it, HackerNewsApiService.instance)
        }
        return null
    }

    fun updateListWithItem(item: NewsItem, position: Int) {
        newsItemListAdapter.updateListWithItem(item, position, newsItemType)
    }
}