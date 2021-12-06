package to.bnt.draw.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import to.bnt.draw.app.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    Greeting("Bebroid")
                    DrawDesk()
                }

            }
        }
    }
}

enum class TouchType {
    Tap, Drag // https://github.com/androidx/androidx/blob/androidx-main/compose/docs/compose-api-guidelines.md#do
}

const val defaultPointerId = -1L //Как оформить?

//После прочтения Android State рассмотреть хранение listOfPointsData, чтобы не хранить коорд по отдельности
@Composable
fun DrawDesk() {
    var coordinateX by remember { mutableStateOf(0f) }
    var coordinateY by remember { mutableStateOf(0f) }
    var previousPointerId by remember { mutableStateOf(defaultPointerId) }
    var isCurrentPointer by remember { mutableStateOf(false) }
    var touchType by remember { mutableStateOf(TouchType.Tap) }

    data class PointData(val offset: Offset, val isCurrentPointer: Boolean = false, val touchType: TouchType)

    val listOfPointsData by remember { mutableStateOf(mutableListOf<PointData>()) }
    Canvas(modifier = Modifier.fillMaxSize().pointerInput(Unit) {
        detectTapGestures { offset ->
            touchType = TouchType.Tap
            coordinateX = offset.x
            coordinateY = offset.y
            isCurrentPointer = false
            previousPointerId = 0
        }
    }.pointerInput(Unit) {
        detectDragGestures { change, _ ->
            change.consumeAllChanges()
            println(change)
            touchType = TouchType.Drag
            coordinateX = change.position.x
            coordinateY = change.position.y
            isCurrentPointer = change.id.value == previousPointerId
            previousPointerId = change.id.value
        }
    }) {
        if (previousPointerId != -1L) {
            listOfPointsData.add(
                PointData(Offset(coordinateX, coordinateY), isCurrentPointer, touchType)
            )
        }

        for (point in 0 until listOfPointsData.size) {
            when (listOfPointsData[point].touchType) {
                TouchType.Drag -> {
                    if (listOfPointsData[point].isCurrentPointer && point >= 2) {
                        drawLine(
                            start = listOfPointsData[point - 1].offset,
                            end = listOfPointsData[point].offset,
                            color = Color.Red,
                            strokeWidth = 10f
                        )
                    }
                }
                TouchType.Tap -> {
                    drawCircle(
                        center = listOfPointsData[point].offset,
                        color = Color.Red,
                        radius = 10f
                    )
                }
            }
        }

        /*for (point in 2 until listOfPointsData.size) {
                  if (listOfPointsData[point].isCurrentPointer) {
                      drawLine(
                          start = listOfPointsData[point - 1].offset,
                          end = listOfPointsData[point].offset,
                          color = Color.Red,
                          strokeWidth = 10f
                      )
                  }
              }*/
    }
}

@Composable
fun Greeting(name: String) {
    Column(modifier = Modifier.padding(50.dp)) {
        Text(text = "Hello $name!")
        Text(text = "Hello!")
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MyApplicationTheme {
        Greeting("Bedroid")
    }
}

@Preview(showBackground = true)
@Composable
fun CanvasPreview() {
    MyApplicationTheme {
        DrawDesk()
    }
}
