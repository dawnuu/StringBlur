package com.android.string.plugin.util

import com.android.string.plugin.IString
import com.android.string.plugin.data.Constant
import com.android.string.plugin.demo_files.*
import com.android.string.plugin.mode.Mode
import com.android.string.plugin.task.EncodeImplFile

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
            Mode.XOR_SHIFT -> Constant.XOR_SHIFT_IMPL_CLASS_NAME
            Mode.FAST_ROT -> Constant.FAST_ROT_IMPL_CLASS_NAME
            Mode.XOR_CHAIN -> Constant.XOR_CHAIN_IMPL_CLASS_NAME
            Mode.LCG_STREAM -> Constant.LCG_STREAM_IMPL_CLASS_NAME
        }
    }

    fun getEncodeImplClassFilePath(mode: Mode, applicationId: String): String {
        return when (mode) {
            Mode.DEFAULT -> Constant.DEFAULT_IMPL_CLASS_FILE_PATH
            Mode.XOR -> Constant.XOR_IMPL_CLASS_FILE_PATH
            Mode.XOR_SHIFT -> Constant.XOR_SHIFT_IMPL_CLASS_FILE_PATH
            Mode.FAST_ROT -> Constant.FAST_ROT_IMPL_CLASS_FILE_PATH
            Mode.XOR_CHAIN -> Constant.XOR_CHAIN_IMPL_CLASS_FILE_PATH
            Mode.LCG_STREAM -> Constant.LCG_STREAM_IMPL_CLASS_FILE_PATH
        }.format(applicationId)
    }

    fun getEncodeImpl(mode: Mode): IString {
        return when (mode) {
            Mode.DEFAULT -> DefaultEncodeImpl()
            Mode.XOR -> XorEncodeImpl()
            Mode.XOR_SHIFT -> XorShiftEncodeImpl()
            Mode.FAST_ROT -> FastRotEncodeImpl()
            Mode.XOR_CHAIN -> XorChainEncodeImpl()
            Mode.LCG_STREAM -> LcgStreamEncodeImpl()
        }
    }

    fun getEncodeImpl(modeName: String): IString {
        return getEncodeImpl(Mode.valueOf(modeName))
    }

    fun getEncodeImplFile(mode: Mode): EncodeImplFile = EncodeImplFile(mode)
}
