package com.android.string.plugin.util

import com.android.string.plugin.IString
import com.android.string.plugin.data.Constant
import com.android.string.plugin.demo_files.*
import com.android.string.plugin.mode.Mode
import com.android.string.plugin.task.BaseFile
import com.android.string.plugin.task.build.*

/**
 * @author chancey
 * @date   2025/6/29
 **/
object ModeUtils {

    fun resolveModes(modes: List<Mode>): List<Mode> {
        return modes.ifEmpty { listOf(Mode.DEFAULT) }.distinct()
    }

    fun getEncodeImplClassName(mode: Mode): String {
        return when (mode) {
            Mode.DEFAULT -> Constant.DEFAULT_IMPL_CLASS_NAME
            Mode.XOR -> Constant.XOR_IMPL_CLASS_NAME
            Mode.REVERSE -> Constant.REVERSE_IMPL_CLASS_NAME
            Mode.SHIFT -> Constant.SHIFT_IMPL_CLASS_NAME
            Mode.XOR_SHIFT -> Constant.XOR_SHIFT_IMPL_CLASS_NAME
            Mode.XOR_SIMD -> Constant.XOR_SIMD_IMPL_CLASS_NAME
            Mode.FAST_ROT -> Constant.FAST_ROT_IMPL_CLASS_NAME
        }
    }

    fun getEncodeImplClassFilePath(mode: Mode, applicationId: String): String {
        return when (mode) {
            Mode.DEFAULT -> Constant.DEFAULT_IMPL_CLASS_FILE_PATH
            Mode.XOR -> Constant.XOR_IMPL_CLASS_FILE_PATH
            Mode.REVERSE -> Constant.REVERSE_IMPL_CLASS_FILE_PATH
            Mode.SHIFT -> Constant.SHIFT_IMPL_CLASS_FILE_PATH
            Mode.XOR_SHIFT -> Constant.XOR_SHIFT_IMPL_CLASS_FILE_PATH
            Mode.XOR_SIMD -> Constant.XOR_SIMD_IMPL_CLASS_FILE_PATH
            Mode.FAST_ROT -> Constant.FAST_ROT_IMPL_CLASS_FILE_PATH
        }.format(applicationId)
    }

    fun getEncodeImpl(mode: Mode): IString {
        return when (mode) {
            Mode.DEFAULT -> DefaultEncodeImpl()
            Mode.XOR -> XorEncodeImpl()
            Mode.REVERSE -> ReverseEncodeImpl()
            Mode.SHIFT -> ShiftEncodeImpl()
            Mode.XOR_SHIFT -> XorShiftEncodeImpl()
            Mode.XOR_SIMD -> XorSimdEncodeImpl()
            Mode.FAST_ROT -> FastRotEncodeImpl()
        }
    }

    fun getEncodeImpl(modeName: String): IString {
        return getEncodeImpl(Mode.valueOf(modeName))
    }

    fun getEncodeImplFile(mode: Mode): BaseFile {
        return when (mode) {
            Mode.DEFAULT -> DefaultEncodeImplFile()
            Mode.XOR -> XorEncodeImplFile()
            Mode.REVERSE -> ReverseEncodeImplFile()
            Mode.SHIFT -> ShiftEncodeImplFile()
            Mode.XOR_SHIFT -> XorShiftEncodeImplFile()
            Mode.XOR_SIMD -> XorSimdEncodeImplFile() // 使用专用SIMD文件生成器
            Mode.FAST_ROT -> FastRotEncodeImplFile()   // 使用专用旋转文件生成器
        }
    }
}