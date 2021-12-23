package to.bnt.draw.shared.drawing

import io.ktor.client.features.websocket.*
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import to.bnt.draw.shared.apiClient.ApiClient
import to.bnt.draw.shared.apiClient.exceptions.ApiException
import to.bnt.draw.shared.drawing.drawing_structures.AddLineResult
import to.bnt.draw.shared.drawing.drawing_structures.Line
import to.bnt.draw.shared.drawing.drawing_structures.Point
import to.bnt.draw.shared.structures.*
import kotlin.math.round

private suspend fun DefaultClientWebSocketSession.sendAction(action: Action) {
    send(Frame.Text(action.toJson()))
}

class DrawingBoardWebSocket(
    canvas: SharedCanvas,
    private val client: ApiClient,
    private val uuid: String,
    private val userId: Int?
) :
    DrawingBoard(canvas) {
    private var connection: DefaultClientWebSocketSession? = null
    private var job: Job? = null
    private val lineToFigureId = mutableMapOf<Long, Long>()
    private val figureToLineId = mutableMapOf<Long, Long>()

    var onConnected: () -> Unit = {}
    var onApiException: (ApiException) -> Unit = {}
    var onConnectionClosed: () -> Unit = {}
    var onConnectedUsers: (List<User>) -> Unit = {}
    var onDisconnectedUser: (Int) -> Unit = {}

    private fun handleAction(action: Action) {
        when (action) {
            is AddFigure -> {
                addFigureFromServer(action.figure)
                redrawLines()
            }
            is FigureAck -> {
                lineToFigureId[action.localId] = action.id
                figureToLineId[action.id] = action.localId
            }
            is RemoveFigure -> {
                removeFigureFromServer(action.figureId)
                redrawLines()
            }
            is ConnectedUsers -> {
                onConnectedUsers(action.users)
            }
            is UserConnected -> {
                onConnectedUsers(listOf(action.user))
            }
            is UserDisconnected -> {
                onDisconnectedUser(action.userId)
            }
        }
    }

    private fun addFigureFromServer(figure: Figure) {
        val addLineResult = conversionStorage.addWorldLine(figure.toLine())
        addLineResult?.let {
            lineToFigureId[it.id] = figure.id!!
            figureToLineId[figure.id!!] = it.id
        }
    }

    private fun removeFigureFromServer(figureId: Long) {
        figureToLineId[figureId]?.let {
            conversionStorage.removeLine(it)
            lineToFigureId.remove(it)
        }
        figureToLineId.remove(figureId)
    }

    fun connect() {
        job = CoroutineScope(Dispatchers.Default).launch {
            try {
                val figures = client.getBoard(uuid, showFigures = true).figures!!
                figures.forEach {
                    addFigureFromServer(it)
                }
                redrawLines()
                client.boardWebSocket(
                    uuid,
                    figureId = figures.lastOrNull()?.id,
                    { connection = it; onConnected() }) { handleAction(it) }
                connection = null
                onConnectionClosed()
            } catch (e: ApiException) {
                onApiException(e)
            }
        }
    }

    override fun onMouseDown(point: Point) {
        if (connection == null || userId == null) return
        super.onMouseDown(point)
    }

    override fun stopDrawing(): AddLineResult? {
        val addLineResult = super.stopDrawing()
        addLineResult?.let {
            CoroutineScope(Dispatchers.Default).launch {
                connection?.sendAction(AddFigure(it.id, it.worldSimplifiedLine.toFigure()))
            }
        }
        return addLineResult
    }

    override fun clearLineAtPoint(point: Point): Long? {
        val lineId = conversionStorage.getLineAtPoint(point)
        val figureId = lineToFigureId[lineId]
        figureId?.let {
            super.clearLineAtPoint(point)
            CoroutineScope(Dispatchers.Default).launch {
                connection?.sendAction(RemoveFigure(it))
            }
        }
        return lineId
    }

    private suspend fun close() {
        connection?.close()
    }

    override fun cleanup() {
        CoroutineScope(Dispatchers.Default).launch {
            close()
        }
        super.cleanup()
    }

    companion object {
        private fun List<Point>.toDrawingData(): String {
            return this.joinToString("|") { "${it.x},${it.y}" }
        }

        private fun parseDrawingData(drawingData: String): List<Point> {
            return drawingData.split("|").map {
                val coordinates = it.split(",")
                Point(coordinates[0].toDouble(), coordinates[1].toDouble())
            }
        }

        private fun Figure.toLine(): Line {
            return Line(parseDrawingData(this.drawingData), this.strokeWidth.toDouble(), this.color)
        }

        private fun Line.toFigure(): Figure {
            return Figure(null, this.points.toDrawingData(), this.strokeColor, round(this.strokeWidth).toInt())
        }
    }
}
