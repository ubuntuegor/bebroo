package to.bnt.draw.app.controller

import to.bnt.draw.app.data.Config
import to.bnt.draw.shared.apiClient.ApiClient

object BebrooController {
    val client = ApiClient(Config.url)

    fun isTokenExist() = client.token != null
}