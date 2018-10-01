package hackernewslater.williamha.com.hackernewslater.ui

import android.app.Activity
import android.app.TimePickerDialog
import android.content.*
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.content.LocalBroadcastManager
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.ActionBar
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import android.widget.TimePicker
import com.amitshekhar.DebugDB
//import com.amitshekhar.DebugDB
import com.onesignal.OneSignal
import hackernewslater.williamha.com.hackernewslater.*
import hackernewslater.williamha.com.hackernewslater.exceptions.HNLException
import hackernewslater.williamha.com.hackernewslater.managers.HNLAccountManager
import hackernewslater.williamha.com.hackernewslater.managers.HNLStoreManager
import hackernewslater.williamha.com.hackernewslater.model.NewsItem
import hackernewslater.williamha.com.hackernewslater.model.TimeToSend
import hackernewslater.williamha.com.hackernewslater.notifications.OneSignalNotificationOpenedHandler
import hackernewslater.williamha.com.hackernewslater.repository.HackerNewsApiRepo
import hackernewslater.williamha.com.hackernewslater.service.HackerNewsApiService
import hackernewslater.williamha.com.hackernewslater.service.RssFeedService
import hackernewslater.williamha.com.hackernewslater.ui.fragments.LoginSignUpFragment
import hackernewslater.williamha.com.hackernewslater.ui.fragments.NewsItemListFragment
import hackernewslater.williamha.com.hackernewslater.ui.fragments.PodcastFragment
import hackernewslater.williamha.com.hackernewslater.ui.utilities.HNLDateTime
import hackernewslater.williamha.com.hackernewslater.ui.utilities.NetworkingUtilities
import hackernewslater.williamha.com.hackernewslater.ui.utilities.Toasty
import hackernewslater.williamha.com.hackernewslater.viewmodel.NewsItemViewModel
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener,
        NewsItemListFragment.OnNewsItemPulledListener,
        NavigationView.OnNavigationItemSelectedListener,
        LoginSignUpFragment.LoginSignUpFragmentListener,
        OneSignalNotificationOpenedHandler.OneSignalNotificationOpenHandlerListener {

    private lateinit var mDrawerLayout: DrawerLayout
    private var fragmentContainer: FrameLayout? = null
    private var podcastContainer: FrameLayout? = null
    private var newsItemListFragment = NewsItemListFragment.newInstance()
    private var podcastFragment = PodcastFragment.newInstance()
    private var accountManager = HNLAccountManager.getInstance(this)
    private val storeManager = HNLStoreManager.getInstance(this)
    private var broadcastReceiver: BroadcastReceiver? = null
    private lateinit var networkIntentFilter: IntentFilter
    private var isPostWelcome = false
    val PAYMENT_REQUEST_CODE = 1
    private var newsItemType = NewsItemViewModel.NewsItemType.TOP
    private var screenType = Screen.NewsItem

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(MainActivity::class.java.simpleName, DebugDB.getAddressLog())

        OneSignal.startInit(this)
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .setNotificationOpenedHandler(OneSignalNotificationOpenedHandler(this))
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init()

        val packageName = applicationContext.packageName
        Log.i("MainActivity", packageName)

        newsItemListFragment.offlineMode = isOffline()

        setContentView(R.layout.activity_main)
        fragmentContainer = findViewById(R.id.main_activity_fragment_container)
        podcastContainer = findViewById(R.id.main_activity_podcast_fragment_container)

        val isFirstLaunch = storeManager.getIsFirstLaunch()
        if (isFirstLaunch) {
            showWelcomeScreen()
            hideProgressBar()
        }

        val storeManager = HNLStoreManager.getInstance(this)
        if (isPostWelcome || !isFirstLaunch) {
            initiateFetch()
        }
        storeManager.setIsFirstLaunchOccurred()

        // Bottom Navigation
        bottomNavigation.setOnNavigationItemSelectedListener { item ->

            when(item.itemId) {
                R.id.topItems -> {
                    Log.d(MainActivity::class.java.simpleName, "Top items clicked")
                    newsItemType = NewsItemViewModel.NewsItemType.TOP
                    newsItemListFragment.fetchNews(NewsItemViewModel.NewsItemType.TOP)
                    fragmentContainer?.visibility = View.VISIBLE
                    podcastContainer?.visibility = View.INVISIBLE
                    screenType = Screen.NewsItem
                }

                R.id.queuedItems -> {
                    Log.d(MainActivity::class.java.simpleName, "Queued items clicked")
                    newsItemType = NewsItemViewModel.NewsItemType.QUEUED
                    newsItemListFragment.fetchNews(NewsItemViewModel.NewsItemType.QUEUED)
                    fragmentContainer?.visibility = View.VISIBLE
                    podcastContainer?.visibility = View.INVISIBLE
                    screenType = Screen.NewsItem

                }

                R.id.podcast -> {
                    Log.d(MainActivity::class.java.simpleName, "Podcast item clicked")
                    showPodcastNewsItemList()
                    fragmentContainer?.visibility = View.INVISIBLE
                    podcastContainer?.visibility = View.VISIBLE
                    screenType = Screen.Podcast
                }
            }
            return@setOnNavigationItemSelectedListener true
        }
    }

    override fun onStart() {
        super.onStart()

        registerForBroadcastReceiver()
    }

    override fun onStop() {
        super.onStop()

        broadcastReceiver?.let {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(it)
            unregisterReceiver(it)
        }
    }

    override fun onResume() {
        super.onResume()

        Log.i(MainActivity::class.java.simpleName, "Enter: On Resume")
        newsItemListFragment.offlineMode = isOffline()
    }

    private fun initiateFetch() {
        setupPostWelcomeStart()
        updateLoginButtonState()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                mDrawerLayout.openDrawer(GravityCompat.START)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun showProgressBar() {
        progressBar.visibility = View.VISIBLE
    }

    fun hideProgressBar() {
        progressBar.visibility = View.INVISIBLE
    }

    private fun setupPostWelcomeStart() {
        setSupportActionBar(toolbar)
        val actionBar: ActionBar? = supportActionBar
        actionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp)
        }

        setupNavigationDrawer()

        when (screenType) {
            Screen.NewsItem -> {
                showNewsItemList()
            }

            Screen.Podcast -> {
                showPodcastNewsItemList()
            }
        }

        if (isOffline()) {
            hideProgressBar()
            Toasty(this, getString(R.string.offline_mode_toast_string)).toast.show()
        } else {
            newsItemListFragment.offlineMode = false
        }
        swipeRefreshLayout.setOnRefreshListener(this)
    }

    private fun setupNavigationDrawer() {
        mDrawerLayout = drawerLayout
        val navView = findViewById<NavigationView>(R.id.nav_view)
        navView.setNavigationItemSelectedListener(this)
        updateLoginButtonState()
    }

    private fun updateLoginButtonState() {
        val navView = findViewById<NavigationView>(R.id.nav_view)
        val loginLogoutItem = navView.menu.findItem(R.id.logout)
        val maxItemsShown = navView.menu.findItem(R.id.showTopItemsSection)
        val isLoggedIn = accountManager.isLoggedIn()
        if (isLoggedIn && !isOffline()) {
            showAccountSettingsButtons()
            loginLogoutItem.setTitle(R.string.logout_string)
        } else {
            hideAccountSettingButtons()
            loginLogoutItem.setTitle(R.string.login_string)
        }

        when(!isOffline()) {
            true ->  {
                maxItemsShown.isVisible = true
                loginLogoutItem.isVisible = true
                navView.menu.setGroupVisible(R.id.group2, true)
                navView.menu.setGroupVisible(R.id.group4, false)
            }
            false -> {
                maxItemsShown.isVisible = false
                loginLogoutItem.isVisible = false
                navView.menu.setGroupVisible(R.id.group2, false)
                navView.menu.setGroupVisible(R.id.group4, true)
            }
        }
    }

    private fun hideAccountSettingButtons() {
        val navView = findViewById<NavigationView>(R.id.nav_view)
        val emailTimeSetter = navView.menu.findItem(R.id.setEmailTime)
        val maxEmailItemsSetter = navView.menu.findItem(R.id.setMaxitemsPerEmail)
        emailTimeSetter.isVisible = false
        maxEmailItemsSetter.isVisible = false
    }

    private fun showAccountSettingsButtons() {
        val navView = findViewById<NavigationView>(R.id.nav_view)
        val emailTimeSetter = navView.menu.findItem(R.id.setEmailTime)
        val maxEmailItemsSetter = navView.menu.findItem(R.id.setMaxitemsPerEmail)
        emailTimeSetter.isVisible = true
        maxEmailItemsSetter.isVisible = true
    }

    private fun showNewsItemList() {
        supportFragmentManager.beginTransaction().replace(R.id.main_activity_fragment_container, newsItemListFragment).commit()
    }

    private fun showPodcastNewsItemList() {
        supportFragmentManager.beginTransaction().replace(R.id.main_activity_podcast_fragment_container, podcastFragment).commit()
    }

    private fun showWelcomeScreen() {
        val welcomeIntent = Intent(this, WelcomeScreenActivity::class.java)
        startActivityForResult(welcomeIntent, WELCOME_REQUEST_CODE)
    }

    private fun closeDrawer() {
        mDrawerLayout.closeDrawer(Gravity.LEFT)
    }

    private fun registerForBroadcastReceiver() {
        broadcastReceiver = object: BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                intent?.let {

                    when(it.action) {
                        USER_AUTH_STATE_CHANGED -> {
                            isPostWelcome = true
                            updateLoginButtonState()
                        }

                        CONNECTIVITY_CHANGE -> {
                            newsItemListFragment.offlineMode = isOffline()
                            updateLoginButtonState()

                            if (!isOffline()) {
                                if (newsItemListFragment.isAdded) {
                                    newsItemListFragment.offlineMode = isOffline()
                                    newsItemListFragment.addItemsFavoritedButNotQueued()
                                    newsItemListFragment.removeItemsQueuedButUnsaved()
                                }
                            }
                        }
                    }
                }
            }
        }

        broadcastReceiver?.let {
            LocalBroadcastManager.getInstance(this).registerReceiver(it, IntentFilter(USER_AUTH_STATE_CHANGED))
        }

        networkIntentFilter = IntentFilter()
        networkIntentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)

        registerReceiver(broadcastReceiver, IntentFilter(networkIntentFilter))
    }


    // OnRefreshListener
    override fun onRefresh() {
        newsItemListFragment.fetchNews(newsItemType)
    }

    override fun onNewsItemPulledStopped() {
        hideProgressBar()
        swipeRefreshLayout.isRefreshing = false
    }

    override fun onNewsItemClicked(item: NewsItem, position: Int) {

        if (!accountManager.isLoggedIn()) {
            val alert = AlertDialog.Builder(this)
            alert.setMessage(R.string.log_in_for_immediate_flow)
            alert.setTitle("Login")
            alert.setPositiveButton("OK", { dialog, which ->
                dialog.dismiss()
                startLoginActivity(LOGIN_PATH)
            })
            alert.show()

        } else {
            item.url?.let { url ->
                showProgressBar()

                val apiRepo = HackerNewsApiRepo(this, HackerNewsApiService.instance)
                apiRepo.accountDetails { userDetails, exception ->

                    if (exception == null) {
                        userDetails?.let { user ->
                            if (user.numberOfImmediateReads > 0) {

                                val alertDialog = AlertDialog.Builder(this)
                                val message = "You have %d immediate reads remaining. Proceed?".format(user.numberOfImmediateReads)

                                alertDialog.setMessage(message)
                                alertDialog.setPositiveButton("Yes!", { dialog, _ ->
                                    dialog.dismiss()
                                    startImmediateRead(url)
                                })
                                alertDialog.setNegativeButton("Queue it instead!", { dialog, _ ->
                                    dialog.dismiss()
                                    hideProgressBar()
                                    newsItemListFragment.addNewsItem(item, position)
                                })
                                alertDialog.setOnCancelListener { hideProgressBar() }
                                alertDialog.show()

                            } else {
                                // Start Payment activity with note of the uri they wanted
                                val intent = Intent(this, PaymentActivity::class.java)
                                intent.putExtra(URL_CONSTANT, item.url)
                                startActivityForResult(intent, PAYMENT_REQUEST_CODE)
                            }
                        }
                    } else {
                        HNLException.handleHNLException(this, exception) {}
                        hideProgressBar()
                    }
                }
            }
        }
    }

    override fun onNewsItemCallStart() {
        showProgressBar()
    }

    private fun startImmediateRead(url: String) {
        val apiRepo = HackerNewsApiRepo(this, HackerNewsApiService.instance)
        apiRepo.decrementNumberOfReads(url) { success, exception ->
            hideProgressBar()

            if (exception == null) {
                val uri = Uri.parse(url)
                val intent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(intent)
            } else {
                HNLException.handleHNLException(this, exception) {}
            }
        }
    }

    private fun startLoginActivity(path: String) {
        val intent = Intent(this, LoginActivity::class.java)
        intent.putExtra(SELECTED_PATH, path)
        startActivity(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when {
            requestCode == PAYMENT_REQUEST_CODE && resultCode == Activity.RESULT_OK -> {
                data?.let { intent ->
                    val url = intent.getStringExtra(URL_CONSTANT)
                    val numberOfReads = intent.getIntExtra(NUMBER_OF_READS, 0)

                    if (numberOfReads > 0) {
                        startImmediateRead(url)
                    }
                }
            }

            requestCode == PAYMENT_REQUEST_CODE && resultCode == Activity.RESULT_CANCELED -> {
                hideProgressBar()
            }

            requestCode == WELCOME_REQUEST_CODE && resultCode == Activity.RESULT_OK -> {

                data?.let { intent ->
                    val toRegister = intent.getBooleanExtra(TO_REGISTER, false)
                    if (toRegister) {
                        startLoginActivity(REGISTER_PATH)
                    }
                }
                initiateFetch()
                newsItemListFragment.fetchNews(newsItemType)
            }
        }
    }

    // LoginSignUpFragmentListener
    override fun onUserDismissed() {
        setupPostWelcomeStart()
    }

    // OnNavigationItemSelectedListener
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val context = this
        closeDrawer()

        return when (item.itemId) {
            R.id.privacyPolicy -> {
                onUserViewWebPage(PRIVACY_POLICY_URL)
                true
            }

            R.id.termsAndConditions -> {
                onUserViewWebPage(TERMS_CONDITIONS_URL)
                true
            }

            R.id.logout -> {
                if (accountManager.isLoggedIn()) {
                    accountManager.logout()
                    updateLoginButtonState()
                    newsItemListFragment.fetchNews(NewsItemViewModel.NewsItemType.TOP)
                } else {
                    // Start the Login activity
                    startLoginActivity(LOGIN_PATH)
                }
                true
            }

            R.id.showTopItemsSection -> {
                val alertDialog = AlertDialog.Builder(this)
                alertDialog.setTitle("Set Max Items to Show in List")

                val maxItemsList = resources.getStringArray(R.array.max_news_items)
                val maxItemsListInteger = maxItemsList.map { it.toInt() }
                val maxItemsFromStore = accountManager.getMaxItemsToShow()

                val selectedIndex = maxItemsListInteger.indexOf(maxItemsFromStore)
                alertDialog.setSingleChoiceItems(R.array.max_news_items, selectedIndex, { dialog, which ->
                    val selectedInteger = maxItemsListInteger[which]
                    accountManager.setMaxItemsToShow(selectedInteger)
                    accountManager.updateUserSettings { _, exception ->
                        exception?.let {
                            HNLException.handleHNLException(this, exception) {}
                        }
                    }
                    dialog.dismiss()

                    // Refresh top news
                    newsItemListFragment.fetchNews(NewsItemViewModel.NewsItemType.TOP)
                })
                alertDialog.show()
                true
            }

            R.id.setEmailTime -> {
                val onTimeListener = object: TimePickerDialog.OnTimeSetListener {
                    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {

                        // Saving and sending the hour is always to GMT.
                        val hour = HNLDateTime.newInstance().convertHourToGMT(hourOfDay)
                        val timeToSend = TimeToSend(hour, minute)
                        accountManager.setTimeToSend(timeToSend)
                        accountManager.updateUserSettings { success, exception ->
                            if (success) {
                                val toast = Toasty(context, "Time updated!").toast
                                toast.setGravity(Gravity.CENTER, 0, 0)
                                toast.show()
                            }

                            exception?.let { HNLException.handleHNLException(context, exception) {} }
                        }
                    }
                }

                // Displaying the hour is always set to local time
                val timeToSendFromStore = accountManager.getTimeToSend()
                val hour = HNLDateTime.newInstance().convertGMTToLocalHour(timeToSendFromStore.hourToSend)
                val timePickerDialog = TimePickerDialog(this, onTimeListener, hour, timeToSendFromStore.minuteToSend, false)
                timePickerDialog.setTitle("Select Time to Deliver")
                timePickerDialog.show()
                true
            }

            R.id.setMaxitemsPerEmail -> {
                val alertDialog = AlertDialog.Builder(this)
                alertDialog.setTitle("Set Max Items to Send")

                val currentMaxItem = accountManager.getMaxItemsToSend()
                val maxItemStringArray = resources.getStringArray(R.array.max_items)
                val arrayInteger = maxItemStringArray.map { it.toInt() }
                val indexOfCurrentMaxItem = arrayInteger.indexOf(currentMaxItem)

                alertDialog.setSingleChoiceItems(R.array.max_items, indexOfCurrentMaxItem, { dialog, which ->
                    val selectedValue = arrayInteger[which]
                    accountManager.setMaxItemsToSend(selectedValue)
                    accountManager.updateUserSettings { success, exception ->

                        if (success) {
                            val toast = Toasty(context, "Max items updated!").toast
                            toast.setGravity(Gravity.CENTER, 0, 0)
                            toast.show()
                        } else {
                            HNLException.handleHNLException(this, exception) {}
                        }

                        dialog.dismiss()
                    }

                })
                alertDialog.show()
                true
            }

            else -> true
        }
    }

    // OneSignal Handler
    override fun onUserClickedNotificationWithUri(uri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(intent)
    }

    // Helpers

    private fun isOffline(): Boolean {
        return !NetworkingUtilities.getInstance(this).isConnectedToInternet()
    }

    fun onUserViewWebPage(url: String) {
        val uri = Uri.parse(url)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(intent)
    }

    enum class Screen {
        NewsItem, Podcast
    }
}
