package app.apktracer.ui.common.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.composefluent.FluentTheme
import io.github.composefluent.component.Text

@Composable
fun SectionHeader(text: String) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        style = FluentTheme.typography.bodyStrong
    )
}