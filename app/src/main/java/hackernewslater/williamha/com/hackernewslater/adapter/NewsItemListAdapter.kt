package hackernewslater.williamha.com.hackernewslater.adapter

import android.content.ContentValues.TAG
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import hackernewslater.williamha.com.hackernewslater.R
import hackernewslater.williamha.com.hackernewslater.model.NewsItem
import hackernewslater.williamha.com.hackernewslater.viewmodel.NewsItemViewModel

/**
 * Created by williamha on 6/13/18.
 */
class NewsItemListAdapter(private var newsItemList: List<NewsItem>?,
                          private val newsItemListAdapterListener: NewsItemListAdapterListener): RecyclerView.Adapter<NewsItemListAdapter.ViewHolder>() {

    interface NewsItemListAdapterListener {
        fun onNewsItemClicked(item: NewsItem, position: Int)
        fun onNewsItemAdded(item:  NewsItem, position: Int)
    }

    inner class ViewHolder(v: View): RecyclerView.ViewHolder(v) {
        val newsItemTitle = v.findViewById<TextView>(R.id.newsItemTitle)
        var newsItem: NewsItem? = null
        var newsItemButtonImage = v.findViewById<ImageButton>(R.id.addItemToQueueButton)

        init {
            v.setOnClickListener {
                newsItem?.let {
                    newsItemListAdapterListener.onNewsItemClicked(it, layoutPosition)
                }
            }

            val addItemButton = v.findViewById<ImageButton>(R.id.addItemToQueueButton)
            addItemButton.setOnClickListener {
                newsItem?.let {
                    newsItemListAdapterListener.onNewsItemAdded(it, layoutPosition)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.news_item, parent, false))
    }

    override fun getItemCount() = newsItemList?.size ?: 0

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val newsItemList = newsItemList ?: return
        val newsItem = newsItemList[position]
        holder.newsItemTitle.text = newsItem.title
        holder.newsItem = newsItem

        if (newsItem.isSaved) {
            holder.newsItemButtonImage.setImageResource(R.drawable.ic_delete_item)
        } else {
            holder.newsItemButtonImage.setImageResource(R.drawable.ic_add_news_item)
        }

    }

    fun setNewsItemData(newsItemData: List<NewsItem>) {
        Log.d("NewsItemListAdapter", "News Item Data Set")
        newsItemList = newsItemData
        this.notifyDataSetChanged()
    }

    fun updateListWithItem(item: NewsItem, position: Int, type: NewsItemViewModel.NewsItemType) {
        var list = newsItemList?.toMutableList()
        list ?: return

        when (type) {
            NewsItemViewModel.NewsItemType.TOP -> {
                list[position] = item
                newsItemList = list
                this.notifyItemChanged(position)
            }

            NewsItemViewModel.NewsItemType.QUEUED -> {
                list.removeAt(position)
                newsItemList = list
                this.notifyDataSetChanged()
            }
        }
    }
}