package app.apktracer.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.apktracer.ui.common.components.Header
import app.apktracer.ui.common.components.SectionHeader
import app.apktracer.ui.common.utils.alignHorizontalSpace
import io.github.composefluent.component.Button
import io.github.composefluent.component.CardExpanderItem
import io.github.composefluent.component.ComboBox
import io.github.composefluent.component.Icon
import io.github.composefluent.component.ScrollbarContainer
import io.github.composefluent.component.Text
import io.github.composefluent.icons.Icons
import io.github.composefluent.icons.regular.Folder
import io.github.composefluent.icons.regular.FolderOpen
import io.github.composefluent.icons.regular.Phone
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.absolutePath
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberDirectoryPickerLauncher
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.filesDir

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    SettingsScreenContent(
        uiState = uiState,
        onSettingsLoad = viewModel::loadSettings,
        onOutputDirSave = viewModel::saveOutputDir,
        onLdPlayerSelectedSave = viewModel::saveLdPlayerSelected,
        onAvdIniSave = viewModel::saveAvdIni
    )
}

@Composable
private fun SettingsScreenContent(
    uiState: SettingsUiState,
    onSettingsLoad: () -> Unit,
    onOutputDirSave: (String?) -> Unit,
    onLdPlayerSelectedSave: (Boolean) -> Unit,
    onAvdIniSave: (String?) -> Unit
) {
    val outputDirPickerLauncher = rememberDirectoryPickerLauncher { outputDir ->
        onOutputDirSave(outputDir?.file?.absolutePath)
    }

    val avdIniPickerLauncher = rememberFilePickerLauncher(
        type = FileKitType.File("ini")
    ) { avdIni ->
        onAvdIniSave(avdIni?.file?.absolutePath)
    }

    LaunchedEffect(Unit) {
        onSettingsLoad()
    }

    Column(Modifier.fillMaxSize()) {
        val scrollState = rememberScrollState()

        Header("Settings")
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
                SectionHeader("General")
                CardExpanderItem(
                    heading = {
                        Text("Output folder")
                    },
                    icon = {
                        Icon(Icons.Regular.Folder, "Output folder")
                    },
                    caption = {
                        Text(
                            uiState.settings.outputDir ?: PlatformFile(
                                FileKit.filesDir,
                                "output"
                            ).absolutePath()
                        )
                    },
                    trailing = {
                        Button(
                            onClick = {
                                outputDirPickerLauncher.launch()
                            },
                        ) {
                            Icon(Icons.Regular.FolderOpen, "Browse")
                            Text("Browse")
                        }
                    }
                )
                Spacer(Modifier.height(4.dp))
                CardExpanderItem(
                    heading = {
                        Text("Emulator")
                    },
                    icon = {
                        Icon(Icons.Regular.Phone, "Emulator")
                    },
                    trailing = {
                        ComboBox(
                            items = listOf(
                                "AVD",
                                "LDPlayer"
                            ),
                            selected = if (uiState.settings.ldPlayerSelected) 1 else 0,
                            onSelectionChange = { i, s ->
                                onLdPlayerSelectedSave(i == 1)
                            }
                        )
                    }
                )
                if (!uiState.settings.ldPlayerSelected) {
                    Spacer(Modifier.height(4.dp))
                    CardExpanderItem(
                        heading = {
                            Text("Android Virtual Device (AVD)")
                        },
                        icon = {
                            Icon(Icons.Regular.Phone, "Android Virtual Device (AVD)")
                        },
                        caption = {
                            Text(uiState.settings.avdIni ?: "No AVD selected")
                        },
                        trailing = {
                            Button(
                                onClick = {
                                    avdIniPickerLauncher.launch()
                                },
                            ) {
                                Icon(Icons.Regular.FolderOpen, "Browse")
                                Text("Browse")
                            }
                        }
                    )
                }
            }
        }
    }
}