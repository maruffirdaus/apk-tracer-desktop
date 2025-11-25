package app.apktracer.ui.convertapks

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.apktracer.ui.common.component.ActionDivider
import app.apktracer.ui.common.component.Header
import app.apktracer.ui.common.component.ItemList
import app.apktracer.ui.common.component.SectionHeader
import app.apktracer.ui.common.extension.alignHorizontalSpace
import io.github.composefluent.component.AccentButton
import io.github.composefluent.component.Button
import io.github.composefluent.component.Icon
import io.github.composefluent.component.ProgressRing
import io.github.composefluent.component.ScrollbarContainer
import io.github.composefluent.component.Text
import io.github.composefluent.icons.Icons
import io.github.composefluent.icons.filled.Play
import io.github.composefluent.icons.regular.Document
import io.github.composefluent.icons.regular.FolderOpen
import io.github.vinceglb.filekit.dialogs.compose.rememberDirectoryPickerLauncher
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ConvertApksScreen(
    viewModel: ConvertApksViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    ConvertApksScreenContent(
        uiState = uiState,
        onSelectedFolderChange = viewModel::changeSelectedFolder,
        onApksConvert = viewModel::convertApks
    )
}

@Composable
private fun ConvertApksScreenContent(
    uiState: ConvertApksUiState,
    onSelectedFolderChange: (String?) -> Unit,
    onApksConvert: () -> Unit
) {
    val directoryPickerLauncher = rememberDirectoryPickerLauncher { directory ->
        onSelectedFolderChange(directory?.file?.absolutePath)
    }

    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            ProgressRing()
        }
    } else {
        Column(Modifier.fillMaxSize()) {
            val lazyListState = rememberLazyListState()

            Header(
                text = "Convert to APKs",
                action = {
                    Button(
                        onClick = {
                            directoryPickerLauncher.launch()
                        }
                    ) {
                        Icon(Icons.Regular.FolderOpen, "Select folder")
                        Text("Select folder")
                    }
                    ActionDivider()
                    AccentButton(
                        onClick = onApksConvert,
                        disabled = uiState.files.isEmpty()
                    ) {
                        Icon(Icons.Filled.Play, "Start")
                        Text("Start")
                    }
                }
            )
            ScrollbarContainer(
                adapter = rememberScrollbarAdapter(lazyListState),
                modifier = Modifier.weight(1f)
            ) {
                ItemList(
                    items = uiState.files.map { it.name },
                    title = "Name",
                    icon = Icons.Regular.Document,
                    modifier = Modifier
                        .fillMaxHeight()
                        .alignHorizontalSpace(),
                    header = {
                        SectionHeader(uiState.selectedFolder ?: "No folder selected")
                    },
                    emptyMessage = "No files found",
                    state = lazyListState,
                    contentPadding = PaddingValues(bottom = 32.dp)
                )
            }
        }
    }
}