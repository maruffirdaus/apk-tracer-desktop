package app.apktracer.ui.common.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.composefluent.FluentTheme

@Composable
fun ActionDivider() {
    Spacer(
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .padding(2.dp)
            .height(16.dp)
            .width(1.dp)
            .background(FluentTheme.colors.stroke.divider.default)
    )
}