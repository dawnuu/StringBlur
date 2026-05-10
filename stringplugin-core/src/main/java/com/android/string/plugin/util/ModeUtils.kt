package com.android.string.plugin.util

import com.android.string.plugin.IString
import com.android.string.plugin.data.Constant
import com.android.string.plugin.demo_files.DefaultEncodeImpl
import com.android.string.plugin.demo_files.ReverseEncodeImpl
import com.android.string.plugin.demo_files.ShiftEncodeImpl
import com.android.string.plugin.demo_files.XorShiftEncodeImpl
import com.android.string.plugin.demo_files.XorEncodeImpl
import com.android.string.plugin.mode.Mode
import com.android.string.plugin.task.BaseFile
import com.android.string.plugin.task.build.DefaultEncodeImplFile
import com.android.string.plugin.task.build.ReverseEncodeImplFile
import com.android.string.plugin.task.build.ShiftEncodeImplFile
import com.android.string.plugin.task.build.XorShiftEncodeImplFile
import com.android.string.plugin.task.build.XorEncodeImplFile

/**
 * @author chancey
 * @date   2025/6/29
 **/
object ModeUtils {

    fun getEncodeImplClassName(mode: Mode): String {
        return when (mode) {
            Mode.DEFAULT -> Constant.DEFAULT_IMPL_CLASS_NAME
            Mode.XOR -> Constant.XOR_IMPL_CLASS_NAME
            Mode.REVERSE -> Constant.REVERSE_IMPL_CLASS_NAME
            Mode.SHIFT -> Constant.SHIFT_IMPL_CLASS_NAME
            Mode.XOR_SHIFT -> Constant.XOR_SHIFT_IMPL_CLASS_NAME
        }
    }

    fun getEncodeImplClassFilePath(mode: Mode, applicationId: String): String {
        return when (mode) {
            Mode.DEFAULT -> Constant.DEFAULT_IMPL_CLASS_FILE_PATH
            Mode.XOR -> Constant.XOR_IMPL_CLASS_FILE_PATH
            Mode.REVERSE -> Constant.REVERSE_IMPL_CLASS_FILE_PATH
            Mode.SHIFT -> Constant.SHIFT_IMPL_CLASS_FILE_PATH
            Mode.XOR_SHIFT -> Constant.XOR_SHIFT_IMPL_CLASS_FILE_PATH
        }.format(applicationId)
    }

    fun getEncodeImpl(mode: Mode): IString {
        return when (mode) {
            Mode.DEFAULT -> DefaultEncodeImpl()
            Mode.XOR -> XorEncodeImpl()
            Mode.REVERSE -> ReverseEncodeImpl()
            Mode.SHIFT -> ShiftEncodeImpl()
            Mode.XOR_SHIFT -> XorShiftEncodeImpl()
        }
    }

    fun getEncodeImplFile(mode: Mode): BaseFile {
        return when (mode) {
            Mode.DEFAULT -> DefaultEncodeImplFile()
            Mode.XOR -> XorEncodeImplFile()
            Mode.REVERSE -> ReverseEncodeImplFile()
            Mode.SHIFT -> ShiftEncodeImplFile()
            Mode.XOR_SHIFT -> XorShiftEncodeImplFile()
        }
    }
}