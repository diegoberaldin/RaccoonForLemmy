package com.github.diegoberaldin.raccoonforlemmy.core.commonui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource

@Composable
fun PostCardImage(
    modifier: Modifier = Modifier,
    post: PostModel,
    blurNsfw: Boolean,
) {
    val imageUrl = post.thumbnailUrl.orEmpty()
    if (imageUrl.isNotEmpty()) {
        val painterResource = asyncPainterResource(data = imageUrl)
        KamelImage(
            modifier = modifier.fillMaxWidth()
                .heightIn(min = 200.dp)
                .blur(radius = if (post.nsfw && blurNsfw) 60.dp else 0.dp),
            resource = painterResource,
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
        )
    }
}
