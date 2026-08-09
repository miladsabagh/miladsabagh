package com.zarin.gold.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zarin.gold.ui.theme.ZarinColors

@Composable
fun AtmosphereBackground(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val transition = rememberInfiniteTransition(label = "sheen")
    val shift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(9000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shift"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ZarinColors.Atmosphere)
            .drawBehind {
                val cx = size.width * (0.2f + shift * 0.6f)
                val cy = size.height * 0.18f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0x28D4AF37), Color.Transparent),
                        center = Offset(cx, cy),
                        radius = size.minDimension * 0.55f
                    ),
                    radius = size.minDimension * 0.55f,
                    center = Offset(cx, cy)
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0x18C48B7A), Color.Transparent),
                        center = Offset(size.width * 0.85f, size.height * 0.75f),
                        radius = size.minDimension * 0.45f
                    ),
                    radius = size.minDimension * 0.45f,
                    center = Offset(size.width * 0.85f, size.height * 0.75f)
                )
            }
    ) {
        content()
    }
}

@Composable
fun BrandMark(modifier: Modifier = Modifier, compact: Boolean = false) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "زرین",
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            fontSize = if (compact) 28.sp else 48.sp,
            color = ZarinColors.AmberSoft,
            letterSpacing = 2.sp
        )
        if (!compact) {
            Text(
                text = "گالری طلا و جواهر",
                color = ZarinColors.IvoryMuted,
                fontSize = 14.sp,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
fun GoldButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(52.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = ZarinColors.Amber,
            contentColor = ZarinColors.Night,
            disabledContainerColor = ZarinColors.AmberDeep.copy(alpha = 0.4f),
            disabledContentColor = ZarinColors.Night.copy(alpha = 0.5f)
        ),
        contentPadding = PaddingValues(horizontal = 20.dp)
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(text = text, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun GhostButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextButton(
        onClick = onClick,
        modifier = modifier
            .height(52.dp)
            .border(1.dp, ZarinColors.Amber.copy(alpha = 0.55f), RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp)
    ) {
        Text(text = text, color = ZarinColors.AmberSoft)
    }
}

@Composable
fun ZarinField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        singleLine = singleLine,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = ZarinColors.Amber,
            unfocusedBorderColor = ZarinColors.Line,
            focusedLabelColor = ZarinColors.AmberSoft,
            unfocusedLabelColor = ZarinColors.IvoryMuted,
            cursorColor = ZarinColors.Amber,
            focusedTextColor = ZarinColors.Ivory,
            unfocusedTextColor = ZarinColors.Ivory,
            focusedContainerColor = ZarinColors.NightElevated.copy(alpha = 0.7f),
            unfocusedContainerColor = ZarinColors.NightElevated.copy(alpha = 0.45f)
        )
    )
}

@Composable
fun SectionTitle(title: String, subtitle: String? = null) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.SemiBold,
            fontSize = 22.sp,
            color = ZarinColors.Ivory,
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth()
        )
        if (subtitle != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                color = ZarinColors.IvoryMuted,
                fontSize = 13.sp,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun QuickAction(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(ZarinColors.NightElevated.copy(alpha = 0.85f))
            .border(1.dp, ZarinColors.Line, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 18.dp, horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(
                    brush = Brush.linearGradient(
                        listOf(ZarinColors.AmberDeep, ZarinColors.Amber)
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = ZarinColors.Night, modifier = Modifier.size(22.dp))
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(title, color = ZarinColors.Ivory, fontSize = 13.sp, textAlign = TextAlign.Center)
    }
}

@Composable
fun PriceRibbon(label: String, value: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFF2A2216), Color(0xFF1A1612), Color(0xFF2A2216))
                )
            )
            .border(1.dp, ZarinColors.Amber.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
            .padding(horizontal = 20.dp, vertical = 18.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(label, color = ZarinColors.IvoryMuted, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                color = ZarinColors.AmberSoft,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        }
        Box(
            modifier = Modifier
                .offset(x = 4.dp)
                .size(56.dp)
                .background(ZarinColors.CardGlow, CircleShape)
                .border(1.dp, ZarinColors.Amber.copy(alpha = 0.4f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("۱۸", color = ZarinColors.Amber, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
    }
}
