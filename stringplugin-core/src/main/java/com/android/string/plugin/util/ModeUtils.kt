package com.android.string.plugin.util

import com.android.string.plugin.IString
import com.android.string.plugin.data.Constant
import com.android.string.plugin.demo_files.DefaultEncodeImpl
import com.android.string.plugin.demo_files.XorEncodeImpl
import com.android.string.plugin.mode.Mode
import com.android.string.plugin.task.BaseFile
import com.android.string.plugin.task.build.DefaultEncodeImplFile
import com.android.string.plugin.task.build.XorEncodeImplFile

/**
 * @author chancey
 * @date   2025/6/29
 **/
object ModeUtils {

    fun getEncodeImplClassName(mode: Mode): String {
        return if (mode == Mode.XOR) {
            Constant.XOR_IMPL_CLASS_NAME
        } else {
            Constant.DEFAULT_IMPL_CLASS_NAME
        }
    }

    fun getEncodeImplClassFilePath(mode: Mode, applicationId: String): String {
        return if (mode == Mode.XOR) {
            Constant.XOR_IMPL_CLASS_FILE_PATH
        } else {
            Constant.DEFAULT_IMPL_CLASS_FILE_PATH
        }.format(applicationId)
    }

    fun getEncodeImpl(mode: Mode): IString {
        return if (mode == Mode.XOR) {
            XorEncodeImpl()
        } else {
            DefaultEncodeImpl()
        }
    }

    fun getEncodeImplFile(mode: Mode): BaseFile {
        return if (mode == Mode.XOR) {
            XorEncodeImplFile()
        } else {
            DefaultEncodeImplFile()
        }
    }
}