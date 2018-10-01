package hackernewslater.williamha.com.hackernewslater.ui.fragments.groupie


import com.xwray.groupie.ExpandableGroup
import hackernewslater.williamha.com.hackernewslater.model.Comment

/**
 * Created by williamha on 8/26/18.
 */

class ExpandableCommentGroup constructor(private val comment: Comment,
                                         depth: Int = 0): ExpandableGroup(ExpandableCommentItem(comment, depth)) {
    init {
        comment.children?.let {
            for (comment in it) {
                val newDepth = depth + 1
                add(ExpandableCommentGroup(comment, newDepth))
            }
        }
    }
}