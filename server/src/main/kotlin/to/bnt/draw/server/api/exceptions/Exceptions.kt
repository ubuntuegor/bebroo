package to.bnt.draw.server.api.exceptions

import io.ktor.http.*

open class ApiException(override val message: String, val errorCode: HttpStatusCode = HttpStatusCode.BadRequest) : Exception(message)
class MissingParameterException(parameterName: String) :
    ApiException("Ошибка запроса: отсутствует параметр $parameterName")
class ForbiddenException : ApiException("Недостаточно прав", HttpStatusCode.Forbidden)
