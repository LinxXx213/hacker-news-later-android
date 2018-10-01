package hackernewslater.williamha.com.hackernewslater.ui

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.ActionBar
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.text.Html
import android.view.MenuItem
import android.view.View
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import hackernewslater.williamha.com.hackernewslater.LOGIN_PATH
import hackernewslater.williamha.com.hackernewslater.R
import hackernewslater.williamha.com.hackernewslater.REGISTER_PATH
import hackernewslater.williamha.com.hackernewslater.SELECTED_PATH
import hackernewslater.williamha.com.hackernewslater.managers.HNLAccountManager
import hackernewslater.williamha.com.hackernewslater.model.Comment
import hackernewslater.williamha.com.hackernewslater.model.NewsItem
import hackernewslater.williamha.com.hackernewslater.repository.HackerNewsApiRepo
import hackernewslater.williamha.com.hackernewslater.repository.HackerNewsRepo
import hackernewslater.williamha.com.hackernewslater.service.HackerNewsApiService
import hackernewslater.williamha.com.hackernewslater.service.HackerNewsService
import hackernewslater.williamha.com.hackernewslater.ui.fragments.groupie.ExpandableCommentGroup
import hackernewslater.williamha.com.hackernewslater.ui.fragments.groupie.NoCommentsGroup
import hackernewslater.williamha.com.hackernewslater.ui.utilities.Toasty
import hackernewslater.williamha.com.hackernewslater.viewmodel.NewsItemViewModel
import kotlinx.android.synthetic.main.activity_comment.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch

/**
 * Created by williamha on 8/25/18.
 */


class CommentActivity: AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener {

    private lateinit var hackerNewsRepo: HackerNewsRepo
    private val hackerNewsApiRepo = HackerNewsApiRepo(this, HackerNewsApiService.instance)
    private val accountManager = HNLAccountManager.getInstance(this)

    private val groupAdapter = GroupAdapter<ViewHolder>()
    private var itemId: Long = 0
    private var position: Int = 0
    var newsItemFromStore: NewsItem? = null
    private var offlineMode = false

    // Models
    private var newsItemViewModel: NewsItemViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        hackerNewsRepo = HackerNewsRepo(this, HackerNewsService.instance)

        setContentView(R.layout.activity_comment)

        setupViewModels()

        setSupportActionBar(commentToolbar)
        val actionBar: ActionBar? = supportActionBar
        actionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
        }

        commentSwipeRefreshLayout.setOnRefreshListener(this)

        val title = intent.getStringExtra("title")
        itemId = intent.getLongExtra("itemId", 0)
        position = intent.getIntExtra("position", 0)
        offlineMode = intent.getBooleanExtra("network", false)

        commentNewsItemTitle.text = title

        commentRecyclerView.apply {
            adapter = groupAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }

        newsItemViewModel?.returnLiveNewsItem(itemId)?.observe(this, Observer<NewsItem> { newsItem ->
            if (newsItem != null) {
                if (newsItem.isSaved) {
                    addItemToQueueButton.setImageResource(R.drawable.ic_delete_item)
                } else {
                    addItemToQueueButton.setImageResource(R.drawable.ic_add_news_item)
                }
            }
        })

        addItemToQueueButton.setOnClickListener {
            val context = this
            launch(CommonPool) {

                newsItemFromStore = newsItemViewModel?.returnItemWithId(itemId)
                newsItemFromStore?.let {

                    // Update the db to save
                    it.isSaved = !it.isSaved
                    newsItemViewModel?.updateNewsItem(it)

                    if (isLoggedIn()) {
                        // Call server to add the item.
                        if (it.addedToQueue) {
                            // Remove
                            hackerNewsApiRepo.deleteNewsItem(it) { success, exception ->
                                if (success) {
                                    it.addedToQueue = !it.addedToQueue
                                    launch(CommonPool) { newsItemViewModel?.updateNewsItem(it) }
                                    launch(UI) {
                                        Toasty(context, "Item removed").toast.show()
                                    }
                                } else {
                                    launch(UI) {
                                        Toasty(context, "An error occurred removing your item from the server at the moment; " +
                                                "when a connection can be made, it will be removed.").toast.show()
                                    }
                                }
                            }
                        } else {
                            // Add
                            hackerNewsApiRepo.addItem(it) { success, exception ->
                                if (success) {
                                    it.addedToQueue = !it.addedToQueue
                                    launch(CommonPool) { newsItemViewModel?.updateNewsItem(it) }
                                    launch(UI) {
                                        Toasty(context, "Item added").toast.show()
                                    }
                                } else {
                                    launch(UI) {
                                        Toasty(context, "An error occurred adding your item from the server at the moment; " +
                                                "when a connection can be made, it will be added.").toast.show()
                                    }
                                }
                            }
                        }
                    } else {
                        promptUserLogin()
                    }
                }
            }
        }

        fetch {}
    }


    private fun setupViewModels() {
        val service = HackerNewsService.instance
        newsItemViewModel = ViewModelProviders.of(this).get(NewsItemViewModel::class.java)
        newsItemViewModel?.hackerNewsRepo = HackerNewsRepo(this, service)
    }

    private fun fetch(callback:() -> Unit) {
        kickoffCommentsFetch(itemId) { fetchedComments ->
            for (comment in fetchedComments) {
                groupAdapter.add(ExpandableCommentGroup(comment))
            }

            if (fetchedComments.count() == 0) {
                groupAdapter.add(NoCommentsGroup())
            }
            commentLoadingProgressBar.visibility = View.GONE
            callback()
        }
    }

    private fun kickoffCommentsFetch(itemId: Long, callback:(comments: List<Comment>) -> Unit) {
        var comments = emptyList<Comment>()
        val isNetworkDown = offlineMode
        hackerNewsRepo.getNewsItemDetails(itemId, { newsItemResponse ->

            if (newsItemResponse.kids == null) {
                callback(emptyList())
            } else {
                newsItemResponse.kids?.let { descendants ->
                    fetchComments(descendants) { fetchedComments ->
                        comments = fetchedComments
                        callback(comments)
                    }

                    if (descendants.count() == 0) {
                        callback(comments)
                    }
                }
            }
        }, !isNetworkDown)
    }

    fun fetchComments(items: List<Long>, callback:(commentsList: List<Comment>) -> Unit) {

        val itemsToReturn = mutableListOf<Comment>()
        for (item in items) {
            fetchComment(item) { fetchedComment ->
                itemsToReturn.add(fetchedComment)

                if (itemsToReturn.count() == items.count()) {
                    callback(itemsToReturn)
                }
            }
        }
    }

    fun fetchComment(itemId: Long, callback:(comment: Comment) -> Unit) {
        val isNetworkDown = offlineMode
        hackerNewsRepo.getNewsItemDetails(itemId, { fetchedComment ->
            var comment = fetchedComment.text ?: ""
            comment = Html.fromHtml(comment).toString()

            if (fetchedComment.kids == null) {
                callback(Comment(fetchedComment.by, comment))
            } else {
                fetchedComment.kids?.let { descendants ->

                    if (descendants.count() == 0) {
                        callback(Comment(fetchedComment.by, comment, emptyList()))
                    } else {
                        fetchComments(descendants) { listOfComments ->
                            callback(Comment(fetchedComment.by, comment, listOfComments))
                        }
                    }
                }
            }
        }, !isNetworkDown)
    }

    // SwipeRefreshLayout.OnRefreshListener
    override fun onRefresh() {
        fetch { commentSwipeRefreshLayout.isRefreshing = false }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                finishActivityWithResult()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishActivityWithResult()
    }

    // helpers

    private fun finishActivityWithResult() {
        val returnIntent = Intent()
        returnIntent.putExtra("itemId", itemId)
        returnIntent.putExtra("position", position)
        setResult(Activity.RESULT_OK, returnIntent)
        finish()
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
        val context = this

        launch(CommonPool) {
            val allItems = newsItemViewModel?.returnAllSavedItems()
            launch(UI) {
                val alertDialog = AlertDialog.Builder(context)
                alertDialog.setMessage("You are not logged in. Sign up and login to " +
                        "ensure your %d saved stories are delivered to your inbox.".format(allItems?.count()))
                        .setPositiveButton("Sign up", { _, _ ->
                            val intent = Intent(context, LoginActivity::class.java)
                            intent.putExtra(SELECTED_PATH, REGISTER_PATH)
                            startActivity(intent)
                        })
                        .setNegativeButton("Log in", { _, _ ->
                            val intent = Intent(context, LoginActivity::class.java)
                            intent.putExtra(SELECTED_PATH, LOGIN_PATH)
                            startActivity(intent)
                        }).show()
            }
        }
    }
}
