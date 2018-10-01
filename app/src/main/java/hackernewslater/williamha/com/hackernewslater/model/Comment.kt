package hackernewslater.williamha.com.hackernewslater.model

/**
 * Created by williamha on 8/26/18.
 */
class Comment constructor(val user: String?, val comment: String?, val children:List<Comment>? = emptyList())