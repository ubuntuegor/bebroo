package to.bnt.draw.shared.apiClient

import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.w3c.dom.MessageEvent
import org.w3c.dom.events.Event
import to.bnt.draw.shared.apiClient.exceptions.ApiException
import kotlin.js.Promise

data class OAuthResponse(
    val type: String?,
    val token: String?,
    val errorText: String?
)

fun ApiClient.googleOAuthPopup(): Promise<String> {
    return Promise { resolve, reject ->
        val popup = window.open("$apiUrl$authEndpoint/googleAuthorize", "Google", "popup")
        MainScope().launch {
            while (true) {
                if (popup?.closed == true) {
                    reject(ApiException("Вход отменён пользователем"))
                    break
                }
                delay(1000)
            }
        }
        val messageHandler: (Event) -> Unit = {
            it as MessageEvent
            val data = it.data.unsafeCast<OAuthResponse>()
            if (data.type == "token") {
                resolve(data.token!!)
                popup?.close()
            }
            if (data.type == "error") {
                reject(ApiException(data.errorText!!))
                popup?.close()
            }
        }
        window.addEventListener("message", messageHandler)
    }
}
