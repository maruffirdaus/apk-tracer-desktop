package app.apktracer.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.apktracer.common.model.ApkSource
import app.apktracer.common.model.CsvDelimiter
import app.apktracer.common.model.Emulator
import app.apktracer.common.model.EmulatorLaunchWaitTime
import app.apktracer.common.model.TraceTimeout
import app.apktracer.ui.common.component.Header
import app.apktracer.ui.common.component.SectionHeader
import app.apktracer.ui.common.extension.alignHorizontalSpace
import io.github.composefluent.component.Button
import io.github.composefluent.component.CardExpanderItem
import io.github.composefluent.component.ComboBox
import io.github.composefluent.component.Icon
import io.github.composefluent.component.ScrollbarContainer
import io.github.composefluent.component.Text
import io.github.composefluent.component.TextField
import io.github.composefluent.icons.Icons
import io.github.composefluent.icons.regular.AppGeneric
import io.github.composefluent.icons.regular.Box
import io.github.composefluent.icons.regular.DocumentText
import io.github.composefluent.icons.regular.Folder
import io.github.composefluent.icons.regular.FolderOpen
import io.github.composefluent.icons.regular.Key
import io.github.composefluent.icons.regular.NumberSymbol
import io.github.composefluent.icons.regular.Phone
import io.github.composefluent.icons.regular.Timer
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberDirectoryPickerLauncher
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    SettingsScreenContent(
        uiState = uiState,
        onOutputDirSave = viewModel::saveOutputDir,
        onTraceTimeoutSave = viewModel::saveTraceTimeout,
        onApkSourceSave = viewModel::saveApkSource,
        onAndroZooApiKeySave = viewModel::saveAndroZooApiKey,
        onEmulatorSave = viewModel::saveEmulator,
        onAvdIniSave = viewModel::saveAvdIni,
        onLdConsoleBinarySave = viewModel::saveLdConsoleBinary,
        onEmulatorLaunchWaitTimeSave = viewModel::saveEmulatorLaunchWaitTime,
        onCsvDelimiterSave = viewModel::saveCsvDelimiter
    )
}

@Composable
private fun SettingsScreenContent(
    uiState: SettingsUiState,
    onOutputDirSave: (String?) -> Unit,
    onTraceTimeoutSave: (Int) -> Unit,
    onApkSourceSave: (Int) -> Unit,
    onAndroZooApiKeySave: (String?) -> Unit,
    onEmulatorSave: (Int) -> Unit,
    onAvdIniSave: (String) -> Unit,
    onLdConsoleBinarySave: (String) -> Unit,
    onEmulatorLaunchWaitTimeSave: (Int) -> Unit,
    onCsvDelimiterSave: (Int) -> Unit
) {
    val outputDirPickerLauncher = rememberDirectoryPickerLauncher { outputDir ->
        onOutputDirSave(outputDir?.file?.absolutePath)
    }

    val avdIniPickerLauncher = rememberFilePickerLauncher(
        type = FileKitType.File("ini")
    ) { avdIni ->
        avdIni?.file?.absolutePath?.let {
            onAvdIniSave(it)
        }
    }

    val ldConsoleBinaryPickerLauncher = rememberFilePickerLauncher(
        type = FileKitType.File("exe")
    ) { ldConsoleBinary ->
        ldConsoleBinary?.file?.absolutePath?.let {
            onLdConsoleBinarySave(it)
        }
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
                SectionHeader("Trace")
                CardExpanderItem(
                    heading = {
                        Text("Output folder")
                    },
                    icon = {
                        Icon(Icons.Regular.Folder, "Output folder")
                    },
                    caption = {
                        Text(uiState.outputDir)
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
                        Text("Trace timeout")
                    },
                    icon = {
                        Icon(Icons.Regular.Timer, "Trace timeout")
                    },
                    trailing = {
                        ComboBox(
                            items = TraceTimeout.entries.map { it.label },
                            selected = uiState.traceTimeout.ordinal,
                            onSelectionChange = { ordinal, _ ->
                                onTraceTimeoutSave(ordinal)
                            }
                        )
                    }
                )
                Spacer(Modifier.height(32.dp))
                SectionHeader("APK source")
                CardExpanderItem(
                    heading = {
                        Text("APK source")
                    },
                    icon = {
                        Icon(Icons.Regular.Box, "APK Source")
                    },
                    trailing = {
                        ComboBox(
                            items = ApkSource.entries.map { it.label },
                            selected = uiState.apkSource.ordinal,
                            onSelectionChange = { ordinal, _ ->
                                onApkSourceSave(ordinal)
                            }
                        )
                    }
                )
                if (uiState.apkSource == ApkSource.ANDRO_ZOO) {
                    Spacer(Modifier.height(4.dp))
                    CardExpanderItem(
                        heading = {
                            Text("AndroZoo API key")
                        },
                        icon = {
                            Icon(Icons.Regular.Key, "AndroZoo API key")
                        },
                        trailing = {
                            TextField(
                                value = uiState.androZooApiKey ?: "",
                                onValueChange = { value ->
                                    onAndroZooApiKeySave(value.trim())
                                },
                                singleLine = true,
                                modifier = Modifier.width(280.dp)
                            )
                        }
                    )
                }
                Spacer(Modifier.height(32.dp))
                SectionHeader("Emulator")
                CardExpanderItem(
                    heading = {
                        Text("Emulator")
                    },
                    icon = {
                        Icon(Icons.Regular.Phone, "Emulator")
                    },
                    trailing = {
                        ComboBox(
                            items = Emulator.entries.map { it.label },
                            selected = uiState.emulator.ordinal,
                            onSelectionChange = { ordinal, _ ->
                                onEmulatorSave(ordinal)
                            }
                        )
                    }
                )
                if (uiState.emulator == Emulator.AVD) {
                    Spacer(Modifier.height(4.dp))
                    CardExpanderItem(
                        heading = {
                            Text("Android Virtual Device (AVD) INI")
                        },
                        icon = {
                            Icon(Icons.Regular.DocumentText, "Android Virtual Device (AVD) INI")
                        },
                        caption = {
                            Text(uiState.avdIni ?: "No AVD INI selected")
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
                if (uiState.emulator == Emulator.LD_PLAYER) {
                    Spacer(Modifier.height(4.dp))
                    CardExpanderItem(
                        heading = {
                            Text("LDConsole binary")
                        },
                        icon = {
                            Icon(Icons.Regular.AppGeneric, "LDConsole binary")
                        },
                        caption = {
                            Text(uiState.ldConsoleBinary)
                        },
                        trailing = {
                            Button(
                                onClick = {
                                    ldConsoleBinaryPickerLauncher.launch()
                                },
                            ) {
                                Icon(Icons.Regular.FolderOpen, "Browse")
                                Text("Browse")
                            }
                        }
                    )
                }
                Spacer(Modifier.height(4.dp))
                CardExpanderItem(
                    heading = {
                        Text("Emulator launch wait time")
                    },
                    icon = {
                        Icon(Icons.Regular.Timer, "Emulator launch wait time")
                    },
                    trailing = {
                        ComboBox(
                            items = EmulatorLaunchWaitTime.entries.map { it.label },
                            selected = uiState.emulatorLaunchWaitTime.ordinal,
                            onSelectionChange = { ordinal, _ ->
                                onEmulatorLaunchWaitTimeSave(ordinal)
                            }
                        )
                    }
                )
                Spacer(Modifier.height(32.dp))
                SectionHeader("Other")
                CardExpanderItem(
                    heading = {
                        Text("CSV delimiter")
                    },
                    icon = {
                        Icon(Icons.Regular.NumberSymbol, "CSV delimiter")
                    },
                    trailing = {
                        ComboBox(
                            items = CsvDelimiter.entries.map { it.label },
                            selected = uiState.csvDelimiter.ordinal,
                            onSelectionChange = { ordinal, _ ->
                                onCsvDelimiterSave(ordinal)
                            }
                        )
                    }
                )
            }
        }
    }
}