package com.android.string.plugin.trasform.visitor

import org.objectweb.asm.Handle
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type

/** Rewrites JDK string-concat invokedynamic calls so fixed text can be encrypted. */
object StringConcatRewriter {

    fun rewrite(
        visitor: MethodVisitor,
        @Suppress("UNUSED_PARAMETER") name: String?,
        descriptor: String?,
        bootstrapMethodHandle: Handle?,
        bootstrapMethodArguments: Array<out Any?>,
        allocateLocals: (List<Type>) -> IntArray
    ): Boolean {
        if (descriptor == null ||
            bootstrapMethodHandle?.owner != CONCAT_FACTORY_OWNER ||
            bootstrapMethodHandle.tag != Opcodes.H_INVOKESTATIC ||
            bootstrapMethodHandle.isInterface ||
            bootstrapMethodHandle.name != "makeConcatWithConstants" ||
            bootstrapMethodHandle.desc != CONCAT_BOOTSTRAP_DESCRIPTOR
        ) {
            return false
        }
        // The invokedynamic call-site name is arbitrary; identify the bootstrap instead.
        val recipe = bootstrapMethodArguments.firstOrNull() as? String ?: return false
        val constants = bootstrapMethodArguments.drop(1)
        if (recipe.all { it == ARGUMENT_MARKER || it == CONSTANT_MARKER } &&
                constants.none { it is String || it is Number }
        ) {
            return false
        }
        // Handles/ConstantDynamic can have observable linkage-time string conversion.
        // Keep those call sites intact rather than moving conversion to each invocation.
        if (constants.any { !supportedConstant(it) }) {
            return false
        }

        val argumentTypes = Type.getArgumentTypes(descriptor)
        if (Type.getReturnType(descriptor).sort != Type.OBJECT ||
            argumentTypes.sumOf { it.size } > 200 ||
            recipe.count { it == ARGUMENT_MARKER } != argumentTypes.size ||
            recipe.count { it == CONSTANT_MARKER } != constants.size
        ) {
            return false
        }
        val localIndices = allocateLocals(argumentTypes.toList())
        for (index in argumentTypes.indices.reversed()) {
            visitor.visitVarInsn(argumentTypes[index].getOpcode(Opcodes.ISTORE), localIndices[index])
        }

        visitor.visitTypeInsn(Opcodes.NEW, STRING_BUILDER_OWNER)
        visitor.visitInsn(Opcodes.DUP)
        visitor.visitMethodInsn(
            Opcodes.INVOKESPECIAL,
            STRING_BUILDER_OWNER,
            "<init>",
            "()V",
            false
        )

        emitRecipe(visitor, recipe, argumentTypes.toList(), localIndices, constants)

        visitor.visitMethodInsn(
            Opcodes.INVOKEVIRTUAL,
            STRING_BUILDER_OWNER,
            "toString",
            "()Ljava/lang/String;",
            false
        )
        return true
    }

    private fun emitRecipe(
        visitor: MethodVisitor,
        recipe: String,
        argumentTypes: List<Type>,
        localIndices: IntArray,
        constants: List<Any?>
    ) {
        var argumentIndex = 0
        var constantIndex = 0
        var literalStart = 0
        recipe.forEachIndexed { index, character ->
            if (character != ARGUMENT_MARKER && character != CONSTANT_MARKER) {
                return@forEachIndexed
            }
            emitLiteral(visitor, recipe.substring(literalStart, index))
            if (character == ARGUMENT_MARKER && argumentIndex < argumentTypes.size) {
                emitArgument(visitor, argumentTypes[argumentIndex], localIndices[argumentIndex])
                argumentIndex++
            } else if (character == CONSTANT_MARKER && constantIndex < constants.size) {
                emitConstant(visitor, constants[constantIndex++])
            }
            literalStart = index + 1
        }
        emitLiteral(visitor, recipe.substring(literalStart))
    }

    private fun emitLiteral(visitor: MethodVisitor, value: String) {
        if (value.isNotEmpty()) {
            visitor.visitLdcInsn(value)
            visitor.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL,
                STRING_BUILDER_OWNER,
                "append",
                "(Ljava/lang/String;)Ljava/lang/StringBuilder;",
                false
            )
        }
    }

    private fun emitArgument(visitor: MethodVisitor, type: Type, localIndex: Int) {
        visitor.visitVarInsn(type.getOpcode(Opcodes.ILOAD), localIndex)
        emitAppend(visitor, type)
    }

    private fun emitConstant(visitor: MethodVisitor, value: Any?) {
        // Static numeric bootstrap arguments must enter encryption as text too;
        // otherwise D8 can fold their primitive appends into fresh plaintext.
        if (value is Number) {
            emitLiteral(visitor, value.toString())
            return
        }
        visitor.visitLdcInsn(value)
        val type = when (value) {
            is String -> Type.getType(String::class.java)
            // ASM Type constants resolve to Class or MethodType references at runtime.
            else -> Type.getType(Any::class.java)
        }
        emitAppend(visitor, type)
    }

    private fun emitAppend(visitor: MethodVisitor, type: Type) {
        val appendDescriptor = when (type.sort) {
            Type.BOOLEAN -> "(Z)Ljava/lang/StringBuilder;"
            Type.CHAR -> "(C)Ljava/lang/StringBuilder;"
            Type.BYTE, Type.SHORT, Type.INT -> "(I)Ljava/lang/StringBuilder;"
            Type.LONG -> "(J)Ljava/lang/StringBuilder;"
            Type.FLOAT -> "(F)Ljava/lang/StringBuilder;"
            Type.DOUBLE -> "(D)Ljava/lang/StringBuilder;"
            Type.OBJECT -> if (type.descriptor == "Ljava/lang/String;") {
                "(Ljava/lang/String;)Ljava/lang/StringBuilder;"
            } else {
                "(Ljava/lang/Object;)Ljava/lang/StringBuilder;"
            }
            else -> "(Ljava/lang/Object;)Ljava/lang/StringBuilder;"
        }
        visitor.visitMethodInsn(
            Opcodes.INVOKEVIRTUAL,
            STRING_BUILDER_OWNER,
            "append",
            appendDescriptor,
            false
        )
    }

    private const val CONCAT_FACTORY_OWNER = "java/lang/invoke/StringConcatFactory"
    private const val STRING_BUILDER_OWNER = "java/lang/StringBuilder"
    private const val ARGUMENT_MARKER = '\u0001'
    private const val CONSTANT_MARKER = '\u0002'
    private const val CONCAT_BOOTSTRAP_DESCRIPTOR =
        "(Ljava/lang/invoke/MethodHandles\$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;"

    private fun supportedConstant(value: Any?): Boolean = when (value) {
        is String, is Int, is Long, is Float, is Double -> true
        is Type -> value.sort in setOf(Type.OBJECT, Type.ARRAY, Type.METHOD)
        else -> false
    }
}
