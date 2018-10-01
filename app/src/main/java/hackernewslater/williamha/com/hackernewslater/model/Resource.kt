package hackernewslater.williamha.com.hackernewslater.model

import hackernewslater.williamha.com.hackernewslater.exceptions.HNLException

/**
 * Created by williamha on 8/19/18.
 */

class Resource<T> private constructor(val status: Resource.Status, val data: T?, val exception: HNLException?) {
    enum class Status {
        SUCCESS, ERROR, LOADING
    }
    companion object {
        fun <T> success(data: T?): Resource<T> {
            return Resource(Status.SUCCESS, data, null)
        }
        fun <T> error(exception: HNLException?): Resource<T> {
            return Resource(Status.ERROR, null, exception)
        }
        fun <T> loading(data: T?): Resource<T> {
            return Resource(Status.LOADING, data, null)
        }
    }
}