package pe.com.smart.core.ui

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import medicalapp.shared.generated.resources.Res
import medicalapp.shared.generated.resources.medical_location
import org.jetbrains.compose.resources.painterResource

@Composable
fun AppLogo(
    modifier: Modifier = Modifier,
    size: Dp = 52.dp
) {
    Image(
        painter = painterResource(Res.drawable.medical_location),
        contentDescription = "Medical App",
        contentScale = ContentScale.Crop,
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.24f))
    )
}
