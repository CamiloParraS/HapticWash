package com.hapticwash.inference

import android.content.Context
import android.util.Log
import java.io.Closeable
import java.nio.ByteBuffer
import java.nio.ByteOrder
import org.tensorflow.lite.Interpreter

/** The LiteRT step classifier plus the meta it was exported with (SPEC 8.2, 8.3). CPU, 1 thread. */
class StepModel private constructor(val meta: ModelMeta, private val interpreter: Interpreter) : Closeable {
    private val input = ByteBuffer.allocateDirect(4 * meta.windowSize * ModelMeta.CHANNELS.size)
        .order(ByteOrder.nativeOrder())
    private val output = Array(1) { FloatArray(meta.labels.size) }

    /** Softmax over [ModelMeta.labels] for one z-scored window from the preprocessor. */
    fun predict(window: FloatArray): FloatArray {
        input.rewind()
        input.asFloatBuffer().put(window)
        interpreter.run(input, output)
        return output[0].copyOf()
    }

    override fun close() = interpreter.close()

    companion object {
        private const val TAG = "HapticWash"
        private var bundled: StepModel? = null
        private var bundledTried = false

        /**
         * The model shipped in `assets/model/`, loaded once. Null means timer-only mode (R6):
         * any failure (missing file, corrupt model, unknown meta) is logged, never thrown.
         */
        @Synchronized
        fun bundled(context: Context): StepModel? {
            if (!bundledTried) {
                bundledTried = true
                bundled = runCatching {
                    val a = context.applicationContext.assets
                    val bytes = a.open("model/model.tflite").use { it.readBytes() }
                    val json = a.open("model/model_meta.json").use { it.readBytes().decodeToString() }
                    load(bytes, json)
                }.getOrNull()
            }
            return bundled
        }

        /** Null (timer-only, R6) if [modelBytes] or [metaJson] can't be used for any reason. */
        fun load(modelBytes: ByteArray, metaJson: String): StepModel? = runCatching {
            val meta = ModelMeta.parse(metaJson)
            val buf = ByteBuffer.allocateDirect(modelBytes.size).order(ByteOrder.nativeOrder())
            buf.put(modelBytes)
            val interp = Interpreter(buf, Interpreter.Options().setNumThreads(1))
            try {
                check(interp.getInputTensor(0).shape().contentEquals(meta.inputShape)) { "model input != meta" }
                check(interp.getOutputTensor(0).shape().contentEquals(meta.outputShape)) { "model output != meta" }
            } catch (e: Throwable) {
                interp.close()
                throw e
            }
            StepModel(meta, interp)
        }.onSuccess {
            Log.i(TAG, "Model loaded: step-classifier ${it.meta.modelVersion}")
        }.onFailure {
            Log.w(TAG, "Model unavailable, timer-only mode", it)
        }.getOrNull()
    }
}
