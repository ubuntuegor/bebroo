package to.bnt.draw.shared.apiClient.exceptions

open class ApiException(message: String) : Throwable(message)
class InvalidTokenException : ApiException("Некорректный токен")
class UnexpectedServerErrorException : ApiException("Неизвестная ошибка")
