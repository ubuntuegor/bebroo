package to.bnt.draw.app.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import to.bnt.draw.app.R
import to.bnt.draw.app.controller.BebrooController
import to.bnt.draw.app.theme.Coral
import to.bnt.draw.app.theme.SuperLightGray
import to.bnt.draw.shared.apiClient.exceptions.ApiException

//TODO Move to Models
data class PreviewCardData(
    val boardName: String,
    val boardOwner: String,
    val boardOwnerImage: Painter?,
    val lastBoardUpdate: String,
)

@Composable
fun MenuScreen(navController: NavController) {
    Scaffold(topBar = { MenuTopBar(navController) }) {
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
        }, verticalArrangement = Arrangement.Center
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
                modifier = Modifier.padding(top = 3.dp, end = 17.dp),
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
fun MenuTopBar(navController: NavController) {
    var isCreateButtonClicked by remember { mutableStateOf(false) }
    var createError by remember { mutableStateOf<String?>(null) }
    var newBoardName by remember { mutableStateOf("") }
    if (isCreateButtonClicked) {
        Dialog(onDismissRequest = { isCreateButtonClicked = !isCreateButtonClicked }) {
            Card(
                elevation = 7.dp, shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.new_board),
                        modifier = Modifier.padding(top = 15.dp).align(Alignment.CenterHorizontally),
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Medium,
                    )
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth().padding(top = 15.dp).padding(horizontal = 12.dp)
                            .align(Alignment.CenterHorizontally),
                        value = newBoardName,
                        onValueChange = {
                            if (it.length < 200) {
                                newBoardName = it
                            }
                        },
                        label = { Text(stringResource(R.string.board_name)) },
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                    )
                    var isBoardCreateButtonClicked by remember { mutableStateOf(false) }
                    Button(
                        onClick = {
                            MainScope().launch {
                                try {
                                    isBoardCreateButtonClicked = true
                                    val boardID = BebrooController.client.createBoard(newBoardName)
                                    navController.navigate("board/${boardID}")
                                } catch (e: ApiException) {
                                    isBoardCreateButtonClicked = false
                                    createError = e.message
                                }
                            }
                        },
                        modifier = Modifier.padding(top = 20.dp).padding(horizontal = 12.dp)
                            .align(Alignment.CenterHorizontally).height(37.dp).fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (isBoardCreateButtonClicked) {
                            CircularProgressIndicator(modifier = Modifier.size(25.dp), color = Color.White)
                        } else {
                            Text(text = stringResource(R.string.create))
                        }
                    }
                    Row(
                        modifier = Modifier.height(24.dp).fillMaxWidth().padding(bottom = 2.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        createError?.let {
                            Text(
                                text = it,
                                color = Coral,
                                fontSize = 16.sp,
                            )
                        }
                    }
                }
            }
        }
    }
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
            onClick = { isCreateButtonClicked = !isCreateButtonClicked },
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
    }, backgroundColor = MaterialTheme.colors.background, elevation = 0.dp
    )
}