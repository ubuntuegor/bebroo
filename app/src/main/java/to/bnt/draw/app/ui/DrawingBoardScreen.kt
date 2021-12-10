package to.bnt.draw.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import to.bnt.draw.app.theme.DarkGray

@Composable
fun DrawingBoardScreen(boardName: String) {
    DrawingBoardTopBar(boardName)
    DrawingBoardBottomBar()
    Desk()
}

@Composable
fun DrawingBoardTopBar(
    boardName: String
) {
    Row(
        modifier = Modifier.fillMaxWidth().height(56.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(
            onClick = { print("Bebra") },
            modifier = Modifier.align(Alignment.CenterVertically)
        )
        {
            Icon(
                Icons.Default.ArrowBack,
                contentDescription = "arrow back",
            )
        }
        Text(text = boardName, modifier = Modifier.align(Alignment.CenterVertically), fontSize = 20.sp)

        Row(modifier = Modifier.align(Alignment.CenterVertically)) {
            IconButton(onClick = { print("Bebra") })
            {
                Icon(
                    Icons.Default.Share,
                    contentDescription = "share sign",
                )
            }
            IconButton(onClick = { print("Bebra") })
            {
                Icon(
                    Icons.Default.Menu,
                    contentDescription = "triple sign",
                )
            }
        }
    }
}

@Composable
fun DrawingBoardBottomBar() {
    Column(modifier = Modifier.height(112.dp))
    {
        Box(modifier = Modifier.height(67.dp)) {
            Box(modifier = Modifier.padding(start = 53.dp, end = 47.dp).fillMaxWidth()) {
                Box(
                    modifier = Modifier.clip(CircleShape).background(color = DarkGray).size(8.dp)
                        .align(Alignment.CenterStart)
                )
                Box(
                    modifier = Modifier.clip(CircleShape).background(color = DarkGray).size(20.dp)
                        .align(Alignment.CenterEnd)
                )
            }

            Row(modifier = Modifier.padding(start = 57.dp, end = 57.dp).padding(top = 11.dp)) {
                var sliderPosition by remember { mutableStateOf(0f) }
                Slider(
                    value = sliderPosition,
                    onValueChange = { sliderPosition = it },
                    colors = SliderDefaults.colors(
                        thumbColor = DarkGray,
                        activeTrackColor = DarkGray,
                        inactiveTrackColor = Color.LightGray,
                        activeTickColor = DarkGray,
                        inactiveTickColor = Color.LightGray,
                    ),
                    steps = 4,
                    valueRange = 1f..6f
                )
                Text(sliderPosition.toString())
            }
        }
        Row(modifier = Modifier.fillMaxSize()) {

        }
    }
}