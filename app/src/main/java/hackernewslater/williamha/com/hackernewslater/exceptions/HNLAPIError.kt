package hackernewslater.williamha.com.hackernewslater.exceptions

/**
 * Created by williamha on 7/24/18.
 */
class HNLAPIError constructor(errorCode: String, errorMessage: String) {
    var errorCode: String = ""
    var errorMessage: String = ""

    init {
        this.errorCode = errorCode
        this.errorMessage = errorMessage
    }
}