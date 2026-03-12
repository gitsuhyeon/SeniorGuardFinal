package com.example.seniorguard.mediapipe

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.exp

class OneEuroFilter(
    private val minCutoff: Float = 1.0f, // 최소 차단 주파수 (떨림 제거 강도)
    private val beta: Float = 0.0f,      // 속도 계수 (반응 속도)
    private val dCutoff: Float = 1.0f    // 속도 차단 주파수
) {
    private var xPrev: Float? = null
    private var dxPrev: Float? = null
    private var tPrev: Long? = null

    fun filter(t: Long, x: Float): Float {
        if (tPrev == null) {
            tPrev = t
            xPrev = x
            dxPrev = 0f
            return x
        }

        // 시간 차이 계산 (단위: 초)
        // 안드로이드의 System.nanoTime() 등을 사용할 경우 1e9로 나누어야 함
        val tE = (t - tPrev!!) / 1_000_000_000.0

        if (tE <= 0.0) return xPrev!!

        // 1. 속도(dx) 계산 및 스무딩
        val alphaD = smoothingFactor(tE, dCutoff.toDouble())
        val dx = (x - xPrev!!) / tE
        val dxHat = exponentialSmoothing(alphaD, dx, dxPrev!!.toDouble())

        // 2. 현재 속도에 따른 Cutoff 조절
        val cutoff = minCutoff + beta * abs(dxHat)

        // 3. 위치(x) 스무딩
        val alpha = smoothingFactor(tE, cutoff)
        val xHat = exponentialSmoothing(alpha, x.toDouble(), xPrev!!.toDouble())

        // 상태 업데이트
        xPrev = xHat.toFloat()
        dxPrev = dxHat.toFloat()
        tPrev = t

        return xHat.toFloat()
    }

    private fun smoothingFactor(tE: Double, cutoff: Double): Double {
        val r = 2 * PI * cutoff * tE
        return r / (r + 1)
    }

    private fun exponentialSmoothing(a: Double, x: Double, xPrev: Double): Double {
        return a * x + (1 - a) * xPrev
    }
}