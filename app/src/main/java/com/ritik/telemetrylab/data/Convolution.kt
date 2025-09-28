package com.ritik.telemetrylab.data

import javax.inject.Inject
import kotlin.random.Random

class Convolution @Inject constructor() {

    private val kernel = arrayOf(
        floatArrayOf(0f, 1f, 0f),
        floatArrayOf(1f, -4f, 1f),
        floatArrayOf(0f, 1f, 0f)
    )

    fun runCompute(times: Int) {
        val size = 256
        val matrix = Array(size) { FloatArray(size) { Random.nextFloat() } }

        repeat(times) {
            val result = Array(size) { FloatArray(size) }
            for (i in 1 until size - 1) {
                for (j in 1 until size - 1) {
                    var sum = 0f
                    for (ki in -1..1) {
                        for (kj in -1..1) {
                            sum += matrix[i + ki][j + kj] * kernel[ki + 1][kj + 1]
                        }
                    }
                    result[i][j] = sum
                }
            }
        }
    }
}
