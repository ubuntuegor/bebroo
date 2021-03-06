package to.bnt.draw.shared.apiClient.exceptions

open class ApiException(message: String) : Throwable(message)
class InvalidTokenException : ApiException("Некорректный токен")
class ForbiddenException : ApiException("Недостаточно прав")
class UnexpectedServerErrorException : ApiException("Ошибка сервера")
class RequestErrorException : ApiException("Ошибка запроса")
