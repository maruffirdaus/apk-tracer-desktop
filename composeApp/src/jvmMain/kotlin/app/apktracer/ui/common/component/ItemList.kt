package app.apktracer.ui.common.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import io.github.composefluent.FluentTheme
import io.github.composefluent.component.Icon
import io.github.composefluent.component.Text

@Composable
fun ItemList(
    items: List<String>,
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    header: @Composable (() -> Unit)? = null,
    emptyMessage: String = "No items found",
    state: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues()
) {
    LazyColumn(
        state = state,
        contentPadding = contentPadding,
        modifier = modifier
    ) {
        if (header != null) {
            item {
                header()
            }
        }
        item {
            Container {
                Text(
                    text = title,
                    style = FluentTheme.typography.caption.copy(color = FluentTheme.colors.text.text.secondary)
                )
            }
        }
        if (items.isEmpty()) {
            item {
                Container(alternate = true) {
                    Text(emptyMessage)
                }
            }
        } else {
            itemsIndexed(items) { index, item ->
                Container(alternate = index % 2 == 0) {
                    Row {
                        Box(
                            modifier = Modifier.size(18.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = item
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(item)
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