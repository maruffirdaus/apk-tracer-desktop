package app.apktracer.ui.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import io.github.composefluent.FluentTheme
import io.github.composefluent.component.Icon
import io.github.composefluent.component.Text
import io.github.composefluent.icons.Icons
import io.github.composefluent.icons.regular.Document
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.name

@Composable
fun FileList(
    files: List<PlatformFile>,
    emptyMessage: String = "No files found"
) {
    Column {
        Container {
            Text(
                text = "Name",
                style = FluentTheme.typography.caption.copy(color = FluentTheme.colors.text.text.secondary)
            )
        }
        if (files.isEmpty()) {
            Container(alternate = true) {
                Text(emptyMessage)
            }
        } else {
            files.forEachIndexed { index, file ->
                Container(alternate = index % 2 == 0) {
                    Row {
                        Box(
                            modifier = Modifier.size(18.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Regular.Document,
                                contentDescription = file.name
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(file.name)
                    }
                }
            }
        }
    }
}

@Composable
private fun Container(
    alternate: Boolean = false,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(68.dp)
            .clip(RoundedCornerShape(4.dp))
            .let {
                if (alternate) {
                    it.background(
                        color = FluentTheme.colors.background.card.default,
                        shape = RoundedCornerShape(4.dp)
                    )
                } else {
                    it
                }
            }
            .padding(16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        content()
    }
}