package com.aliumujib.flashlightcontrol

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.aliumujib.flashlightcontrol.ui.theme.FlashLightControlTheme
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

class MainActivity : ComponentActivity() {

    companion object {
        private const val ON_OFF_DURATION = 500
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FlashLightControlTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val coroutineScope = rememberCoroutineScope()

                    var isOn by rememberSaveable { mutableStateOf(false) }

                    val offSetX = remember { Animatable(0f) }
                    val offSetY = remember { Animatable(0f) }
                    val contentAlpha = remember { Animatable(0.2f) }

                    LaunchedEffect(isOn) {
                        if (isOn) {
                            contentAlpha.animateTo(
                                targetValue = 1f,
                                animationSpec = tween(durationMillis = ON_OFF_DURATION)
                            )
                        } else {
                            contentAlpha.animateTo(
                                targetValue = 0.2f,
                                animationSpec = tween(durationMillis = ON_OFF_DURATION)
                            )
                        }
                    }


                    ConstraintLayout(
                        modifier = Modifier
                            .pointerInput(Unit) {
                                detectTapGestures(onTap = {
                                    isOn = !isOn

                                    coroutineScope.launch {
                                        if (isOn) {
                                            launch {
                                                offSetX.animateTo(
                                                    targetValue = 90f,
                                                    animationSpec = tween(durationMillis = ON_OFF_DURATION)
                                                )
                                            }
                                            launch {
                                                offSetY.animateTo(
                                                    targetValue = 150f,
                                                    animationSpec = tween(durationMillis = ON_OFF_DURATION)
                                                )
                                            }
                                        } else {
                                            launch {
                                                offSetX.animateTo(
                                                    targetValue = 0f,
                                                    animationSpec = tween(durationMillis = ON_OFF_DURATION)
                                                )
                                            }
                                            launch {
                                                offSetY.animateTo(
                                                    targetValue = 0f,
                                                    animationSpec = tween(durationMillis = ON_OFF_DURATION)
                                                )
                                            }
                                        }
                                    }
                                })
                            }
                            .pointerInput(Unit) {

                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    coroutineScope.launch {
                                        offSetX.snapTo(
                                            (offSetX.value + dragAmount.x).coerceIn(
                                                0f,
                                                180f
                                            )
                                        )
                                        offSetY.snapTo(
                                            (offSetY.value - dragAmount.y).coerceIn(
                                                0f,
                                                300f
                                            )
                                        )
                                    }
                                }
                            }
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        val (outerCurve, innerCurve, lightRay, lightBulb) = createRefs()

                        val maxDragDistance = 300f

                        val dragXProgress = (offSetX.value / maxDragDistance).coerceIn(0f, 1f)
                        val dragYProgress = (offSetY.value / maxDragDistance).coerceIn(0f, 1f)

                        isOn = dragYProgress > 0.1

                        val outerCurveWidthPercentage = 0.4f + if (isOn) {
                            (maxOf(dragXProgress, dragYProgress) * 0.5f)
                        } else {
                            0f
                        }
                        val outerCurveCurvature = (dragYProgress * 10)


                        val innerCurveWidthPercentage =
                            0.2f + (maxOf(dragXProgress, dragYProgress) * 0.6f)
                        val innerCurveBottomMargin = 50 + (dragYProgress * 200)
                        val innerCurveCurvature = (dragYProgress * 10)

                        val spread = (maxOf(dragXProgress, dragYProgress) * 1).coerceIn(0f, 1f)
                        val intensity = (dragYProgress * 1).coerceIn(0f, 1f)

                        if (isOn && dragYProgress in 0.1..0.8) {
                            CurvedBar(
                                modifier = Modifier
                                    .fillMaxWidth(outerCurveWidthPercentage)
                                    .height(10.dp)
                                    .constrainAs(outerCurve) {
                                        bottom.linkTo(lightBulb.top, margin = 250.dp)
                                        centerHorizontallyTo(parent)
                                    },
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = contentAlpha.value),
                                curvature = outerCurveCurvature,
                                strokeWidth = 2.dp,
                                isDotted = true
                            )
                        }

                        if (isOn) {
                            LightRay(
                                modifier = Modifier
                                    .fillMaxWidth(0.8f)
                                    .fillMaxHeight(0.20f)
                                    .constrainAs(lightRay) {
                                        bottom.linkTo(lightBulb.top, margin = 48.dp)
                                        centerHorizontallyTo(parent)
                                    },
                                intensity = intensity,
                                spread = spread
                            )
                        }

                        CurvedBar(
                            modifier = Modifier
                                .fillMaxWidth(innerCurveWidthPercentage)
                                .height(10.dp)
                                .constrainAs(innerCurve) {
                                    bottom.linkTo(lightBulb.top, margin = innerCurveBottomMargin.dp)
                                    centerHorizontallyTo(parent)
                                },
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = contentAlpha.value),
                            curvature = innerCurveCurvature,
                            strokeWidth = 2.dp,
                        )

                        Image(
                            modifier = Modifier
                                .pointerInput(Unit) {
                                    detectTapGestures(onTap = {
                                        isOn = !isOn

                                        coroutineScope.launch {
                                            if (isOn) {
                                                launch {
                                                    offSetX.animateTo(
                                                        targetValue = 90f,
                                                        animationSpec = tween(durationMillis = ON_OFF_DURATION)
                                                    )
                                                }
                                                launch {
                                                    offSetY.animateTo(
                                                        targetValue = 150f,
                                                        animationSpec = tween(durationMillis = ON_OFF_DURATION)
                                                    )
                                                }
                                            } else {
                                                launch {
                                                    offSetX.animateTo(
                                                        targetValue = 0f,
                                                        animationSpec = tween(durationMillis = ON_OFF_DURATION)
                                                    )
                                                }
                                                launch {
                                                    offSetY.animateTo(
                                                        targetValue = 0f,
                                                        animationSpec = tween(durationMillis = ON_OFF_DURATION)
                                                    )
                                                }
                                            }
                                        }
                                    })
                                }
                                .fillMaxSize(fraction = 0.2f)
                                .constrainAs(lightBulb) {
                                    bottom.linkTo(parent.bottom, margin = 32.dp)
                                    start.linkTo(parent.start)
                                    end.linkTo(parent.end)
                                },
                            painter = painterResource(id = R.drawable.flash_light),
                            contentScale = ContentScale.FillHeight,
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(
                                color = MaterialTheme.colorScheme.onBackground.copy(
                                    alpha = contentAlpha.value
                                )
                            )
                        )
                    }
                }
            }
        }
    }

}

@Composable
fun LightRay(
    modifier: Modifier = Modifier,
    intensity: Float = 0.5f,
    angle: Float = 0f,
    spread: Float = 0.5f
) {
    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val centerX = canvasWidth / 2
        val centerY = canvasHeight

        val path = Path().apply {
            val spreadAngle = 40f * spread
            val leftAngle = Math.toRadians((angle - spreadAngle).toDouble())
            val rightAngle = Math.toRadians((angle + spreadAngle).toDouble())

            moveTo(centerX, centerY)

            val leftEndX = centerX + canvasHeight * sin(leftAngle).toFloat()
            val leftEndY = centerY - canvasHeight * cos(leftAngle).toFloat()
            lineTo(leftEndX, leftEndY)

            val radius = canvasHeight / cos(spreadAngle * Math.PI / 180).toFloat()
            arcTo(
                Rect(
                    centerX - radius,
                    centerY - radius,
                    centerX + radius,
                    centerY + radius
                ),
                270f - angle - spreadAngle,
                spreadAngle * 2,
                false
            )

            val rightEndX = centerX + canvasHeight * sin(rightAngle).toFloat()
            val rightEndY = centerY - canvasHeight * cos(rightAngle).toFloat()
            lineTo(rightEndX, rightEndY)

            close()
        }

        val gradient = Brush.radialGradient(
            colors = listOf(
                Color.White.copy(alpha = intensity),
                Color.White.copy(alpha = intensity * 0.7f),
                Color.White.copy(alpha = intensity * 0.3f),
                Color.Transparent
            ),
            center = Offset(centerX, centerY),
            radius = canvasHeight * 1.5f
        )

        drawPath(
            path = path,
            brush = gradient,
            blendMode = BlendMode.Plus
        )
    }
}

@Composable
fun CurvedBar(
    modifier: Modifier = Modifier,
    color: Color = Color.Gray,
    strokeWidth: Dp = 10.dp,
    curvature: Float = 0.0f,
    isDotted: Boolean = false,
    dotSpacing: Dp = 10.dp
) {
    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        val path = Path().apply {
            moveTo(0f, canvasHeight / 2)

            val controlPointY = canvasHeight / 2 - (canvasHeight * curvature)
            quadraticBezierTo(
                canvasWidth / 2, controlPointY,
                canvasWidth, canvasHeight / 2
            )
        }

        val pathEffect = if (isDotted) {
            PathEffect.dashPathEffect(
                floatArrayOf(strokeWidth.toPx(), dotSpacing.toPx()),
                0f
            )
        } else {
            null
        }

        drawPath(
            path = path,
            color = color,
            style = Stroke(
                width = strokeWidth.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
                pathEffect = pathEffect
            )
        )
    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    FlashLightControlTheme {

    }
}