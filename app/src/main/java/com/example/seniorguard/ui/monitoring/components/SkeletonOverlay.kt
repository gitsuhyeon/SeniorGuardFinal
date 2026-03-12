package com.example.seniorguard.ui.monitoring.components


import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PaintingStyle
//import androidx.compose.ui.graphics.drawscope.drawCircle
//import androidx.compose.ui.graphics.drawscope.drawLine
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import com.google.mediapipe.tasks.vision.core.RunningMode
import kotlin.math.max
import androidx.compose.ui.graphics.Paint
import com.example.seniorguard.data.model.SkeletonData


@Composable
fun SkeletonOverlay(
    skeleton: SkeletonData,
    modifier: Modifier = Modifier
) {

    Canvas(modifier = modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        skeleton.joints.forEach { joint ->
            joint.visibility?.let { if (it < 0.5f) return@forEach }

            val x = joint.x * canvasWidth
            val y = joint.y * canvasHeight

            drawCircle(
                color = Color.Red,
                radius = 8f,
                center = Offset(x, y)
            )

        /*
        val scaleFactor = when (runningMode) {
            RunningMode.LIVE_STREAM -> max(canvasWidth / imageWidth, canvasHeight / imageHeight)
            else -> 1f


        }

        val pointPaint = Paint().apply {
            color = Color.Yellow
            strokeWidth = LANDMARK_STROKE_WIDTH
            style = PaintingStyle.Fill
        }

        val linePaint = Paint().apply {
            color = Color(0xFF6200EE) // mp_color_primary 대체
            strokeWidth = LANDMARK_STROKE_WIDTH
            style = PaintingStyle.Stroke
        }

        result.landmarks().firstOrNull()?.let { landmarks ->
            // Draw points
            landmarks.forEach { landmark ->
                drawCircle(
                    color = Color.Yellow,
                    radius = LANDMARK_STROKE_WIDTH,
                    center = Offset(
                        landmark.x() * imageWidth * scaleFactor,
                        landmark.y() * imageHeight * scaleFactor
                    )
                )
            }

            // Draw lines
            PoseLandmarker.POSE_LANDMARKS.forEach {
                val start = landmarks[it!!.start()]
                val end = landmarks[it.end()]
                drawLine(
                    color = Color(0xFF6200EE),
                    start = Offset(start.x() * imageWidth * scaleFactor, start.y() * imageHeight * scaleFactor),
                    end = Offset(end.x() * imageWidth * scaleFactor, end.y() * imageHeight * scaleFactor),
                    strokeWidth = LANDMARK_STROKE_WIDTH
                )

         */
            }
        }
    }


//private const val LANDMARK_STROKE_WIDTH = 12f
