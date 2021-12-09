package to.bnt.draw.app.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import to.bnt.draw.app.R

data class PreviewCardData(
    val boardName: String,
    val boardOwner: String,
    val boardOwnerImage: Painter?,
    val lastBoardUpdate: String,
)

@Composable
fun MenuScreen() {
    MenuTopBar()
    val testList = mutableListOf(
        PreviewCardData(
            "Зачет по программированию",
            "Егор Спирин", painterResource(R.drawable.sample_avatar), "6:24"
        ),PreviewCardData(
            "Заметки по матанализу",
            "Ваша доска", painterResource(R.drawable.sample_avatar), "вчера в 21:30"
        ),
        PreviewCardData(
            "Наброски дизайна",
            "Саморожи", painterResource(R.drawable.sample_avatar), "27 октября в 23:40"
        ),
    )

    val scrollState = rememberLazyListState()
    LazyColumn(modifier = Modifier.fillMaxSize(), state = scrollState)
    {
        items(testList) { boardPreview ->
            BoardPreviewCard(
                boardPreview.boardName,
                boardPreview.boardOwner,
                boardPreview.boardOwnerImage,
                boardPreview.lastBoardUpdate
            )
        }
    }
}

@Composable
fun BoardPreviewCard(
    boardName: String,
    boardOwner: String,
    boardOwnerImage: Painter?,
    lastBoardUpdate: String,
) {
    Column(
        modifier = Modifier.height(67.dp).fillMaxWidth().clickable { print("bebra") },
        verticalArrangement = Arrangement.Center
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = boardName,
                modifier = Modifier.padding(start = 17.dp).align(Alignment.CenterStart),
                fontSize = 16.sp,
            )
            Text(
                text = lastBoardUpdate,
                modifier = Modifier.padding(top = 3.dp, end = 20.dp).align(Alignment.CenterEnd),
                fontSize = 12.sp
            )
        }
        Spacer(modifier = Modifier.padding(top = 4.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            boardOwnerImage?.let {
                Image(
                    it,
                    "Boarder owner avatar",
                    modifier = Modifier.padding(start = 17.dp).size(width = 20.dp, height = 20.dp).clip(CircleShape)
                )
            }
            Text(
                boardOwner,
                modifier = Modifier.padding(start = (if (boardOwnerImage == null) 16.dp else 6.dp)),
                color = MaterialTheme.colors.onBackground.copy(alpha = 0.6f),
                fontSize = 14.sp,
            )
        }
    }
    Divider(color = Color(0.132f, 0.132f, 0.132f, 0.08f))
}

@Composable
fun MenuTopBar() {
    Box(
        modifier = Modifier.fillMaxWidth().height(56.dp),
    ) {
        IconButton(
            onClick = { print("Bebra") },
        )
        {
            Icon(
                Icons.Default.Add,
                contentDescription = "plus sign",
                modifier = Modifier.align(Alignment.Center).padding(start = 4.dp)
            )
        }
        Image(
            painter = painterResource(R.drawable.logo),
            contentDescription = "logo",
            modifier = Modifier.height(24.dp).align(Alignment.Center),
        )
        IconButton(
            onClick = { print("Bebra") },
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 9.dp)
        )
        {
            Image(
                painterResource(R.drawable.sample_avatar),
                contentDescription = "sample avatar",
                modifier = Modifier.size(width = 32.dp, height = 32.dp).clip(
                    CircleShape
                )
            )
        }
    }
}