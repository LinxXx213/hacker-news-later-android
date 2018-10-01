package hackernewslater.williamha.com.hackernewslater.ui.fragments.groupie

import android.view.LayoutInflater
import android.view.View
import com.xwray.groupie.ExpandableGroup
import com.xwray.groupie.ExpandableItem
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import hackernewslater.williamha.com.hackernewslater.R
import hackernewslater.williamha.com.hackernewslater.model.Comment
import kotlinx.android.synthetic.main.item_expandable_comment.*
import kotlinx.android.synthetic.main.item_expandable_comment.view.*

/**
 * Created by williamha on 8/26/18.
 */
open class ExpandableCommentItem constructor(private val comment: Comment,
                                             private val depth: Int): Item(), ExpandableItem {

    private lateinit var expandableGroup: ExpandableGroup

    override fun getLayout() = R.layout.item_expandable_comment

    override fun bind(viewHolder: ViewHolder, position: Int) {
        addDepthViews(viewHolder)

        viewHolder.body.text = comment.comment
        viewHolder.tv_user.text = comment.user

        comment.children?.let {
            if (it.count() > 0) {
                viewHolder.itemView.repliesButton.visibility = View.VISIBLE
            } else {
                viewHolder.itemView.repliesButton.visibility = View.GONE
            }
        }

        viewHolder.repliesButton.apply {
            setOnClickListener {
                expandableGroup.onToggleExpanded()

                if (expandableGroup.isExpanded) {
                    viewHolder.itemView.repliesButton.setText(R.string.hide_replies)
                } else {
                    viewHolder.itemView.repliesButton.setText(R.string.show_replies)
                }
            }
        }
    }

    private fun addDepthViews(viewHolder: ViewHolder) {
        viewHolder.itemView.separatorContainer.removeAllViews()

        if (depth > 0) {
            viewHolder.itemView.separatorContainer.visibility = View.VISIBLE
        } else {
            viewHolder.itemView.separatorContainer.visibility = View.GONE
        }

        // Loop through the "depth"
        for (i in 1..depth) {
            val v: View = LayoutInflater.from(viewHolder.itemView.context).inflate(R.layout.separator_view, viewHolder.itemView.separatorContainer, false)
            viewHolder.itemView.separatorContainer.addView(v)
        }
    }

    override fun setExpandableGroup(onToggleListener: ExpandableGroup) {
        this.expandableGroup = onToggleListener
    }

}