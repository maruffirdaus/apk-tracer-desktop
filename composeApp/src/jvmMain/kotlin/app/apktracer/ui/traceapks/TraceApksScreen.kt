package app.apktracer.ui.traceapks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.apktracer.common.model.ApkSource
import app.apktracer.ui.common.component.ActionDivider
import app.apktracer.ui.common.component.Header
import app.apktracer.ui.common.component.ItemList
import app.apktracer.ui.common.component.SectionHeader
import app.apktracer.ui.common.extension.alignHorizontalSpace
import io.github.composefluent.FluentTheme
import io.github.composefluent.component.AccentButton
import io.github.composefluent.component.Button
import io.github.composefluent.component.ContentDialog
import io.github.composefluent.component.ContentDialogButton
import io.github.composefluent.component.DialogSize
import io.github.composefluent.component.Icon
import io.github.composefluent.component.ProgressBar
import io.github.composefluent.component.ProgressRing
import io.github.composefluent.component.ScrollbarContainer
import io.github.composefluent.component.Text
import io.github.composefluent.icons.Icons
import io.github.composefluent.icons.filled.Play
import io.github.composefluent.icons.regular.Document
import io.github.composefluent.icons.regular.FolderOpen
import io.github.composefluent.icons.regular.Tag
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberDirectoryPickerLauncher
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TraceApksScreen(
    viewModel: TraceApksViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    TraceApksScreenContent(
        uiState = uiState,
        onSelectedFolderChange = viewModel::changeSelectedFolder,
        onSelectedCsvChange = viewModel::changeSelectedCsv,
        onTraceStart = viewModel::startTrace,
        onTraceStop = viewModel::stopTrace,
        onErrorMessageClear = viewModel::clearErrorMessage
    )
}

@Composable
private fun TraceApksScreenContent(
    uiState: TraceApksUiState,
    onSelectedFolderChange: (String?) -> Unit,
    onSelectedCsvChange: (String?) -> Unit,
    onTraceStart: () -> Unit,
    onTraceStop: () -> Unit,
    onErrorMessageClear: () -> Unit
) {
    val directoryPickerLauncher = rememberDirectoryPickerLauncher { directory ->
        onSelectedFolderChange(directory?.file?.absolutePath)
    }
    val filePickerLauncher = rememberFilePickerLauncher(
        type = FileKitType.File("csv")
    ) { file ->
        onSelectedCsvChange(file?.file?.absolutePath)
    }

    if (uiState.isTracing) {
        ContentDialog(
            title = if (uiState.isStoppingTrace) {
                "Stopping trace"
            } else {
                "Tracing"
            },
            visible = true,
            content = {
                if (uiState.isStoppingTrace) {
                    ProgressBar(
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    val progress =
                        (uiState.completedTraceCount.toFloat() + uiState.failedTraceCount) / (uiState.totalTraceCount.toFloat()
                            .takeIf { it > 0f } ?: 1f)

                    if (uiState.traceMessage != null) {
                        Text(
                            text = uiState.traceMessage,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = FluentTheme.typography.caption
                        )
                        Spacer(Modifier.height(4.dp))
                    }
                    ProgressBar(
                        progress = progress,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = buildString {
                            append(uiState.completedTraceCount + uiState.failedTraceCount)
                            append(" of ")
                            append(uiState.totalTraceCount)
                            append(" (")
                            append(uiState.failedTraceCount)
                            append(" failed)")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End,
                        style = FluentTheme.typography.caption
                    )
                }
            },
            primaryButtonText = "Stop",
            onButtonClick = { button ->
                when (button) {
                    ContentDialogButton.Primary -> {
                        if (!uiState.isStoppingTrace) {
                            onTraceStop()
                        }
                    }

                    else -> {}
                }
            },
            size = DialogSize.Min
        )
    }

    if (uiState.errorMessage != null) {
        ContentDialog(
            title = "Error",
            visible = true,
            content = {
                Text(uiState.errorMessage)
            },
            primaryButtonText = "Close",
            onButtonClick = { button ->
                when (button) {
                    ContentDialogButton.Primary -> {
                        onErrorMessageClear()
                    }

                    else -> {}
                }
            },
            size = DialogSize.Min
        )
    }

    if (uiState.isLoading) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ProgressRing()
            if (uiState.loadingMessage != null) {
                Spacer(Modifier.height(16.dp))
                Text(uiState.loadingMessage)
            }
        }
    } else {
        Column(Modifier.fillMaxSize()) {
            val lazyListState = rememberLazyListState()

            Header(
                text = "Trace APKs",
                action = {
                    Button(
                        onClick = {
                            if (uiState.apkSource == ApkSource.LOCAL) {
                                directoryPickerLauncher.launch()
                            } else {
                                filePickerLauncher.launch()
                            }
                        }
                    ) {
                        val label = if (uiState.apkSource == ApkSource.LOCAL) {
                            "Select folder"
                        } else {
                            "Select CSV"
                        }
                        Icon(Icons.Regular.FolderOpen, label)
                        Text(label)
                    }
                    ActionDivider()
                    AccentButton(
                        onClick = onTraceStart,
                        disabled = if (uiState.apkSource == ApkSource.LOCAL) {
                            uiState.apks.isEmpty()
                        } else {
                            uiState.apkIdentifiers.isEmpty()
                        }
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
                    items = if (uiState.apkSource == ApkSource.LOCAL) {
                        uiState.apks.map { it.name }
                    } else {
                        uiState.apkIdentifiers
                    },
                    title = if (uiState.apkSource == ApkSource.LOCAL) {
                        "Name"
                    } else {
                        "Identifier"
                    },
                    icon = if (uiState.apkSource == ApkSource.LOCAL) {
                        Icons.Regular.Document
                    } else {
                        Icons.Regular.Tag
                    },
                    modifier = Modifier
                        .fillMaxHeight()
                        .alignHorizontalSpace(),
                    header = {
                        SectionHeader(
                            text = buildString {
                                if (uiState.apkSource == ApkSource.LOCAL) {
                                    append(uiState.selectedFolder ?: "No folder selected")
                                } else {
                                    append(uiState.selectedCsv ?: "No CSV selected")
                                }
                            }
                        )
                    },
                    emptyMessage = if (uiState.apkSource == ApkSource.LOCAL) {
                        "No APKs found"
                    } else {
                        "No APK identifiers found"
                    },
                    state = lazyListState,
                    contentPadding = PaddingValues(bottom = 32.dp)
                )
            }
        }
    }
}