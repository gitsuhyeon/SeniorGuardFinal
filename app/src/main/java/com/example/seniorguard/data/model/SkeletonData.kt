package com.example.seniorguard.data.model

import kotlinx.serialization.Serializable
/**
 * 앱에서 사용하는 골격 데이터 모델
 * - joints: 관절 좌표 리스트
 *
 */
@Serializable
data class SkeletonData(
    val joints: List<Joint>
) {

    @Serializable
    data class Joint(
        val x: Float,
        val y: Float,
        val z: Float,
        val visibility: Float? //andmark.visibility().orElse(null)로 처리했기에 맞춰줌.
    )

    companion object {
        fun empty() = SkeletonData(joints = emptyList())
    }
}
