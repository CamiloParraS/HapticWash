package com.hapticwash

import java.nio.ByteBuffer
import java.nio.ByteOrder

/** Minimal reader for the little-endian float32 `.npy` golden files (SPEC 9.2). */
class Npy(val shape: IntArray, val data: FloatArray) {
    companion object {
        fun read(bytes: ByteArray): Npy {
            val buf = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
            check(bytes[0] == 0x93.toByte() && String(bytes, 1, 5) == "NUMPY") { "not an .npy file" }
            val major = bytes[6].toInt()
            val headerLen = if (major == 1) buf.getShort(8).toInt() and 0xffff else buf.getInt(8)
            val start = if (major == 1) 10 else 12
            val header = String(bytes, start, headerLen)
            check("'<f4'" in header) { "expected <f4, got $header" }
            val shape = Regex("""'shape': \(([^)]*)\)""").find(header)!!.groupValues[1]
                .split(',').filter { it.isNotBlank() }.map { it.trim().toInt() }.toIntArray()
            val flat = FloatArray(shape.fold(1) { a, b -> a * b })
            buf.position(start + headerLen)
            buf.asFloatBuffer().get(flat)
            // raw.npy is written Fortran-ordered; hand back C order for 2-D arrays.
            if ("'fortran_order': True" in header) {
                check(shape.size == 2) { "fortran order only supported for 2-D" }
                val (r, c) = shape[0] to shape[1]
                return Npy(shape, FloatArray(flat.size) { flat[(it % c) * r + it / c] })
            }
            return Npy(shape, flat)
        }
    }
}
