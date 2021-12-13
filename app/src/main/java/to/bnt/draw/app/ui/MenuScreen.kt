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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import to.bnt.draw.app.R
import to.bnt.draw.app.theme.SuperLightGray

//TODO Move to Models
data class PreviewCardData(
    val boardName: String,
    val boardOwner: String,
    val boardOwnerImage: Painter?,
    val lastBoardUpdate: String,
)

@Composable
fun MenuScreen(navController: NavController) {
    Scaffold(topBar = { MenuTopBar() }) {
        val testList = mutableListOf(
            PreviewCardData(
                "Зачет по программированию", "Егор Спирин", painterResource(R.drawable.sample_avatar), "6:24"
            ),
            PreviewCardData(
                "Заметки по матанализу", "Ваша доска", painterResource(R.drawable.sample_avatar), "вчера в 21:30"
            ),
            PreviewCardData(
                "Наброски дизайна", "Саморожи", painterResource(R.drawable.sample_avatar), "27 октября в 23:40"
            ),
        )

        val scrollState = rememberLazyListState()
        LazyColumn(modifier = Modifier.fillMaxSize(), state = scrollState) {
            items(testList) { boardPreview ->
                BoardPreviewCard(
                    navController,
                    //TODO setBoardId
                    "5005",
                    boardPreview.boardName,
                    boardPreview.boardOwner,
                    boardPreview.boardOwnerImage,
                    boardPreview.lastBoardUpdate
                )
            }
        }
    }
}

@Composable
fun BoardPreviewCard(
    navController: NavController,
    boardID: String,
    boardName: String,
    boardOwner: String,
    boardOwnerImage: Painter?,
    lastBoardUpdate: String,
) {
    Column(
        modifier = Modifier.height(67.dp).fillMaxWidth().clickable {
            navController.navigate("board/${boardID}")
        },
        verticalArrangement = Arrangement.Center
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = boardName,
                modifier = Modifier.padding(start = 17.dp).weight(1f, fill = false),
                fontSize = 16.sp,
                overflow = TextOverflow.Ellipsis,
                softWrap = true,
                maxLines = 1
            )
            Text(
                text = lastBoardUpdate,
                modifier = Modifier.padding(top = 3.dp, end = 20.dp),
                fontSize = 12.sp,
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
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
                modifier = Modifier.padding(start = (if (boardOwnerImage == null) 16.dp else 6.dp), end = 19.dp),
                color = MaterialTheme.colors.onBackground.copy(alpha = 0.6f),
                fontSize = 14.sp,
                overflow = TextOverflow.Ellipsis,
                softWrap = true,
                maxLines = 1
            )
        }
    }
    Divider(color = SuperLightGray)
}

@Composable
fun MenuTopBar() {
    TopAppBar(title = {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Image(
                painter = painterResource(R.drawable.logo),
                contentDescription = "logo",
                modifier = Modifier.height(24.dp),
            )
        }
    }, navigationIcon = {
        IconButton(
            onClick = { print("Bebra") },
        ) {
            Icon(Icons.Default.Add, contentDescription = "plus sign")
        }
    }, actions = {
        IconButton(onClick = { print("Bebra") }) {
            Image(
                painterResource(R.drawable.sample_avatar),
                contentDescription = "sample avatar",
                modifier = Modifier.size(width = 32.dp, height = 32.dp).clip(
                    CircleShape
                )
            )
        }
    }, backgroundColor = MaterialTheme.colors.background
    )
}