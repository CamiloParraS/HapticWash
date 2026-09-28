package com.hapticwash.inference

import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.roundToInt

/**
 * `model_meta.json` (SPEC 8.2). Everything the pipeline needs about the model is read from
 * here at load time; nothing is hardcoded (SPEC 10 rule 4). [parse] throws on anything it
 * does not recognise, and the caller falls back to timer-only mode (R6).
 */
class ModelMeta(
    val modelVersion: String,
    val sampleRateHz: Int,
    /** Samples per window. */
    val windowSize: Int,
    /** Samples between window starts. */
    val windowStride: Int,
    val bandLowHz: Double,
    val bandHighHz: Double,
    val bandOrder: Int,
    val gravityAlign: Boolean,
    val gravityLowpassHz: Double,
    val normMean: FloatArray,
    val normStd: FloatArray,
    val labels: List<String>,
    val movingAverageK: Int,
    val inputShape: IntArray,
    val outputShape: IntArray,
) {
    companion object {
        const val SPEC_VERSION = "1.0"

        /** [com.hapticwash.sensing.MotionSample] carries exactly these, in this order. */
        val CHANNELS = listOf("ax", "ay", "az", "gx", "gy", "gz")

        fun parse(json: String): ModelMeta {
            val o = JSONObject(json)
            val spec = o.getString("spec_version")
            require(spec == SPEC_VERSION) { "unrecognised spec_version $spec" }
            require(o.getJSONArray("channels").strings() == CHANNELS) { "unexpected channels" }
            val fs = o.getInt("sample_rate_hz")
            val pre = o.getJSONObject("preprocessing")
            require(pre.getString("normalization") == "zscore_per_channel") { "unknown normalization" }
            val band = pre.getJSONObject("bandpass")
            val meta = ModelMeta(
                modelVersion = o.getString("model_version"),
                sampleRateHz = fs,
                windowSize = (o.getDouble("window_size_s") * fs).roundToInt(),
                windowStride = (o.getDouble("window_stride_s") * fs).roundToInt(),
                bandLowHz = band.getDouble("low_hz"),
                bandHighHz = band.getDouble("high_hz"),
                bandOrder = band.getInt("order"),
                gravityAlign = pre.getBoolean("gravity_align"),
                gravityLowpassHz = pre.getDouble("gravity_lowpass_hz"),
                normMean = pre.getJSONArray("norm_mean").floats(),
                normStd = pre.getJSONArray("norm_std").floats(),
                labels = o.getJSONArray("labels").strings(),
                movingAverageK = o.getJSONObject("postprocessing").getInt("moving_average_k"),
                inputShape = o.getJSONObject("input").getJSONArray("shape").ints(),
                outputShape = o.getJSONObject("output").getJSONArray("shape").ints(),
            )
            val c = CHANNELS.size
            require(meta.normMean.size == c && meta.normStd.size == c) { "norm vectors need $c entries" }
            require(meta.windowStride > 0) { "window stride must be positive" }
            require(meta.inputShape.contentEquals(intArrayOf(1, meta.windowSize, c))) { "input shape mismatch" }
            require(meta.outputShape.contentEquals(intArrayOf(1, meta.labels.size))) { "output shape mismatch" }
            return meta
        }

        private fun JSONArray.strings() = List(length()) { getString(it) }
        private fun JSONArray.ints() = IntArray(length()) { getInt(it) }
        private fun JSONArray.floats() = FloatArray(length()) { getDouble(it).toFloat() }
    }
}
