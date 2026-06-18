package com.android.string.plugin.trasform

import com.android.string.plugin.data.Constant
import com.android.string.plugin.field.StringFiled
import com.android.string.plugin.mode.BytesMode
import com.android.string.plugin.mode.Mode
import com.android.string.plugin.report.StringBlurReport
import com.android.string.plugin.trasform.visitor.ClinitMethodVisitor
import com.android.string.plugin.trasform.visitor.InitMethodVisitor
import com.android.string.plugin.trasform.visitor.NormalMethodVisitor
import com.android.string.plugin.util.AsmWriter
import com.android.string.plugin.util.ModeUtils
import com.android.string.plugin.util.SmartAlgorithmSelector
import com.android.string.plugin.mode.SelectionStrategy
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import kotlin.random.Random

/**
 * @author chancey
 * @date   2023/9/6   22:54
 **/
class ClassVisitorController(
    applicationId: String,
    private val key: String,
    private val bytesMode: BytesMode,
    private val modes: List<Mode>,
    private val reportPath: String,
    private val minLength: Int,
    private val selectionStrategy: SelectionStrategy = SelectionStrategy.RANDOM,
    private val performanceWeight: Double = 0.5,
    private val securityWeight: Double = 0.5
) {
    private val smartSelector = SmartAlgorithmSelector()
    private val stringBlurClassName =
        Constant.PLUGIN_CLASS_FILE_PATH.format(applicationId).replace(".", "/")
    var currentClassName: String? = null
    val staticFinalFields = mutableListOf<StringFiled>()
    val staticFields = mutableListOf<StringFiled>()
    val finalFields = mutableListOf<StringFiled>()
    private val fields = mutableListOf<StringFiled>()
    private var isClInitExists = false
    private val random = Random(key.hashCode())
    fun visitField(access: Int, name: String?, desc: String?, value: String?) {
        if (name.isNullOrBlank() || desc != StringFiled.DESC) {
            return
        }
        val isStatic = (access and Opcodes.ACC_STATIC) != 0
        val isFinal = (access and Opcodes.ACC_FINAL) != 0
        val field = StringFiled(name, value)
        when {
            // static final, in this condition, the value is null or not null.
            isStatic && isFinal -> staticFinalFields += field
            // static, in this condition, the value is null.
            isStatic && !isFinal -> staticFields += field
            // final, in this condition, the value is null or not null.
            !isStatic && isFinal -> finalFields += field
            // normal, in this condition, the value is null.
            else -> fields += field
        }
    }

    fun visitEnd(mv: MethodVisitor) {
        mv.visitCode()
        // Here init static final fields.
        staticFinalFields.forEach {
            if (!overflow(it.value)) {
                return@forEach
            }
            write(it.value, mv)
            mv.visitFieldInsn(Opcodes.PUTSTATIC, currentClassName, it.name, StringFiled.DESC)
        }
        mv.visitInsn(Opcodes.RETURN)
        mv.visitMaxs(1, 0)
        mv.visitEnd()
    }

    fun isVisitClInitMethod(): Boolean {
        return !isClInitExists && staticFinalFields.isNotEmpty()
    }

    fun visitMethod(access: Int, mv: MethodVisitor, name: String?): MethodVisitor {
        return when (name) {
            // If clinit exists meaning the static fields (not final) would have be inited here.
            "<clinit>" -> {
                isClInitExists = true
                ClinitMethodVisitor(mv, this, name)
            }
            // Here init final(not static) and normal fields
            "<init>" -> InitMethodVisitor(mv, this, name)
            else -> NormalMethodVisitor(access, mv, this, name)
        }
    }


    fun overflow(data: String?): Boolean {
        return data != null && ModeUtils.getEncodeImpl(modes.first()).overflow(data.toByteArray()) && data.length >= minLength
    }

    fun reportEncrypted(methodName: String?, data: String?, mode: Mode, selectedBytesMode: BytesMode) {
        StringBlurReport.encrypted(reportPath, currentClassName, methodName, data, mode.name, selectedBytesMode.name)
    }

    fun reportIgnored(methodName: String?, value: Any?, reason: String) {
        StringBlurReport.ignored(reportPath, currentClassName, methodName, value, reason)
    }

    fun reportIgnoredLdc(methodName: String?, value: Any?) {
        if (value is String && !ModeUtils.getEncodeImpl(modes.first()).overflow(value.toByteArray())) {
            reportIgnored(methodName, value, "emptyString")
        } else if (value is String && value.length < minLength) {
            reportIgnored(methodName, value, "tooShort")
        } else if (value !is String) {
            reportIgnored(methodName, value, "notStringLdc")
        }
    }

    fun write(data: String?, mv: MethodVisitor, methodName: String? = null) {
        val modeIndex = selectModeIndex(data ?: "")
        val mode = modes[modeIndex]
        val selectedBytesMode = selectBytesMode()
        val stringBlurWrapper = ModeUtils.getEncodeImpl(mode)
        reportEncrypted(methodName, data, mode, selectedBytesMode)
        if (selectedBytesMode == BytesMode.BYTES) {
            writeByBytes(data, stringBlurWrapper, modeIndex, mv)
        } else {
            writeByString(data, stringBlurWrapper, modeIndex, mv)
        }
    }

    private fun selectBytesMode(): BytesMode {
        return when (bytesMode) {
            BytesMode.STRING -> BytesMode.STRING
            BytesMode.BYTES -> BytesMode.BYTES
            BytesMode.RANDOM -> if (random.nextBoolean()) BytesMode.BYTES else BytesMode.STRING
        }
    }

    private fun selectModeIndex(content: String): Int {
        if (modes.size == 1) {
            return 0
        }
        
        // 使用智能选择器选择最佳算法
        val selectedMode = smartSelector.selectBestAlgorithm(
            content = content,
            modes = modes,
            strategy = selectionStrategy,
            performanceWeight = performanceWeight,
            securityWeight = securityWeight
        )
        
        return modes.indexOf(selectedMode).takeIf { it >= 0 } ?: random.nextInt(modes.size)
    }

    private fun writeByString(data: String?, stringBlurWrapper: com.android.string.plugin.IString, modeIndex: Int, mv: MethodVisitor) {
        val encodeText = stringBlurWrapper.encryptString(data, key)
        AsmWriter(stringBlurClassName).write(encodeText, key, modeIndex, mv)
    }

    private fun writeByBytes(data: String?, stringBlurWrapper: com.android.string.plugin.IString, modeIndex: Int, mv: MethodVisitor) {
        val encodeText = stringBlurWrapper.encryptBytes(data, key)
        AsmWriter(stringBlurClassName).write(encodeText, key, modeIndex, mv)
    }
}