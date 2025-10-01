package app.apktracer.ui.traceapks

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.apktracer.ui.common.components.ActionDivider
import app.apktracer.ui.common.components.FileList
import app.apktracer.ui.common.components.Header
import app.apktracer.ui.common.components.SectionHeader
import app.apktracer.ui.common.utils.alignHorizontalSpace
import io.github.composefluent.component.AccentButton
import io.github.composefluent.component.Button
import io.github.composefluent.component.ContentDialog
import io.github.composefluent.component.ContentDialogButton
import io.github.composefluent.component.DialogSize
import io.github.composefluent.component.Icon
import io.github.composefluent.component.ScrollbarContainer
import io.github.composefluent.component.Text
import io.github.composefluent.icons.Icons
import io.github.composefluent.icons.filled.Play
import io.github.composefluent.icons.regular.FolderOpen
import io.github.vinceglb.filekit.dialogs.compose.rememberDirectoryPickerLauncher

@Composable
fun TraceApksScreen(
    viewModel: TraceApksViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    TraceApksScreenContent(
        uiState = uiState,
        onSelectedFolderChange = viewModel::changeSelectedFolder,
        onApksTrace = viewModel::traceApks
    )
}

@Composable
private fun TraceApksScreenContent(
    uiState: TraceApksUiState,
    onSelectedFolderChange: (String?) -> Unit,
    onApksTrace: () -> Unit
) {
    val directoryPickerLauncher = rememberDirectoryPickerLauncher { directory ->
        onSelectedFolderChange(directory?.file?.absolutePath)
    }

    if (uiState.isTracing) {
        ContentDialog(
            title = "Tracing",
            visible = true,
            content = {},
            primaryButtonText = "Stop",
            onButtonClick = { button ->
                when (button) {
                    ContentDialogButton.Primary -> {
                    }

                    else -> {}
                }
            },
            size = DialogSize.Min
        )
    }

    Column(Modifier.fillMaxSize()) {
        val scrollState = rememberScrollState()

        Header(
            text = "Trace APKs",
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
                    onClick = onApksTrace,
                    disabled = uiState.apks.isEmpty()
                ) {
                    Icon(Icons.Filled.Play, "Start")
                    Text("Start")
                }
            }
        )
        ScrollbarContainer(
            adapter = rememberScrollbarAdapter(scrollState),
            modifier = Modifier.weight(1f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .verticalScroll(scrollState)
                    .alignHorizontalSpace()
                    .padding(bottom = 32.dp),
            ) {
                SectionHeader(uiState.selectedFolder ?: "No folder selected")
                FileList(
                    files = uiState.apks,
                    emptyMessage = "No APKs found"
                )
            }
        }
    }
}