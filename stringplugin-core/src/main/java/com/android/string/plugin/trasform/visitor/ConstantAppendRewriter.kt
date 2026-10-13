package com.android.string.plugin.trasform.visitor

import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.*
import org.objectweb.asm.tree.analysis.Analyzer
import org.objectweb.asm.tree.analysis.AnalyzerException
import org.objectweb.asm.tree.analysis.SourceInterpreter

/** Expose constant primitive appends to encryption before D8 can fold them into text. */
object ConstantAppendRewriter {
    fun rewrite(owner: String, method: MethodNode) {
        if (method.instructions.size() == 0) return
        val instructions = method.instructions.toArray()
        if (instructions.none { it is MethodInsnNode && isPrimitiveAppend(it) }) return
        val frames = try {
            Analyzer(SourceInterpreter()).analyze(owner, method)
        } catch (_: AnalyzerException) {
            // Do not guess stack provenance when analysis is unavailable.
            return
        }
        instructions.forEachIndexed { index, instruction ->
            val call = instruction as? MethodInsnNode ?: return@forEachIndexed
            if (!isPrimitiveAppend(call)) return@forEachIndexed
            // Only direct constant producers: leave variables, arithmetic and shared
            // stack values intact. Frames also guard jumps into the append instruction.
            var producer = call.previous
            while (producer != null && producer.opcode < 0) producer = producer.previous
            val frame = frames[index] ?: return@forEachIndexed
            if (frame.stackSize == 0 || frame.getStack(frame.stackSize - 1).insns != setOf(producer)) return@forEachIndexed
            val value = number(producer) ?: return@forEachIndexed
            val text = when (call.desc[1]) {
                'Z' -> if (value is Int) (value != 0).toString() else null
                // The encryption pipeline uses UTF-8, which cannot preserve a lone surrogate.
                'C' -> if (value is Int && !value.toChar().isSurrogate()) value.toChar().toString() else null
                'I' -> if (value is Int) value.toString() else null
                'J' -> if (value is Long) value.toString() else null
                'F' -> if (value is Float) java.lang.Float.toString(value) else null
                'D' -> if (value is Double) java.lang.Double.toString(value) else null
                else -> null
            } ?: return@forEachIndexed
            method.instructions.set(producer, LdcInsnNode(text))
            call.desc = "(Ljava/lang/String;)L${call.owner};"
        }
    }

    private fun number(instruction: AbstractInsnNode?): Number? = when (instruction) {
        is LdcInsnNode -> instruction.cst as? Number
        is IntInsnNode -> if (instruction.opcode == Opcodes.BIPUSH || instruction.opcode == Opcodes.SIPUSH) instruction.operand else null
        is InsnNode -> when (instruction.opcode) {
            in Opcodes.ICONST_M1..Opcodes.ICONST_5 -> instruction.opcode - Opcodes.ICONST_0
            in Opcodes.LCONST_0..Opcodes.LCONST_1 -> (instruction.opcode - Opcodes.LCONST_0).toLong()
            in Opcodes.FCONST_0..Opcodes.FCONST_2 -> (instruction.opcode - Opcodes.FCONST_0).toFloat()
            in Opcodes.DCONST_0..Opcodes.DCONST_1 -> (instruction.opcode - Opcodes.DCONST_0).toDouble()
            else -> null
        }
        else -> null
    }

    private val BUILDERS = setOf("java/lang/StringBuilder", "java/lang/StringBuffer")
    private val DESCRIPTORS = setOf('Z', 'C', 'I', 'J', 'F', 'D')
    private fun isPrimitiveAppend(call: MethodInsnNode) = call.opcode == Opcodes.INVOKEVIRTUAL &&
        !call.itf && call.name == "append" && call.owner in BUILDERS &&
        DESCRIPTORS.any { call.desc == "($it)L${call.owner};" }
}
