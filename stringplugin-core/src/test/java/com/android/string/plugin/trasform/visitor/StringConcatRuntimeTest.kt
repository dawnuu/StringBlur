package com.android.string.plugin.trasform.visitor

import com.android.string.plugin.mode.BytesMode
import com.android.string.plugin.mode.Mode
import com.android.string.plugin.task.build.StringBlurFile
import com.android.string.plugin.trasform.StringBlurClassVisitor
import com.android.string.plugin.util.KeyDerivation
import com.android.string.plugin.util.ModeUtils
import org.junit.Assert.*
import org.junit.Test
import org.objectweb.asm.*
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.InvokeDynamicInsnNode
import org.objectweb.asm.tree.LdcInsnNode
import java.io.File
import java.net.URLClassLoader
import java.nio.file.Files
import javax.tools.ToolProvider

class StringConcatRuntimeTest {
    private val bootstrap = Handle(
        Opcodes.H_INVOKESTATIC,
        "java/lang/invoke/StringConcatFactory",
        "makeConcatWithConstants",
        "(Ljava/lang/invoke/MethodHandles\$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;",
        false
    )
    private val master = KeyDerivation.master("concat-runtime-test")

    private data class Case(
        val descriptor: String,
        val recipe: String,
        val arguments: Array<Any?> = emptyArray(),
        val constants: Array<Any> = emptyArray(),
        val name: String = "makeConcatWithConstants",
        val appends: List<Pair<Number, Char>> = emptyList(),
        val builder: String = "java/lang/StringBuilder"
    )

    @Test
    fun encryptedConcatenationMatchesJvmResults() {
        val chars = charArrayOf('a', 'b')
        val cases = listOf(
            Case("(Ljava/lang/String;)Ljava/lang/String;", "前缀🙂=\u0001/后缀", arrayOf("中文"), name = "customConcatName"),
            Case("(Ljava/lang/String;)Ljava/lang/String;", "null=\u0001", arrayOf(null)),
            Case("(ZCBSIJFD)Ljava/lang/String;", "values=\u0001/\u0001/\u0001/\u0001/\u0001/\u0001/\u0001/\u0001",
                arrayOf(true, '中', (-7).toByte(), 300.toShort(), -42, Long.MIN_VALUE, Float.NaN, Double.POSITIVE_INFINITY)),
            Case("(JLjava/lang/String;D)Ljava/lang/String;", "wide=\u0001\u0001\u0001", arrayOf(123L, null, -0.0)),
            Case("(Ljava/lang/Object;)Ljava/lang/String;", "object=\u0001", arrayOf(object {
                override fun toString() = "custom-object"
            })),
            Case("([C[ILjava/lang/Object;)Ljava/lang/String;", "arrays=\u0001/\u0001/\u0001", arrayOf(chars, intArrayOf(1, 2), null)),
            Case("(I)Ljava/lang/String;", "\u0002:\u0001:\u0002", arrayOf(9), arrayOf("secret\u0001\u0002", "tail")),
            Case("()Ljava/lang/String;", "constant-only=\u0002/\u0002/\u0002/\u0002/\u0002",
                constants = arrayOf("secret", -12, 123L, -0.0f, Double.NaN)),
            Case("()Ljava/lang/String;", "types=\u0002/\u0002/\u0002/\u0002",
                constants = arrayOf(Type.getType(String::class.java), Type.getType(Runnable::class.java),
                    Type.getType("[I"), Type.getMethodType("(IJ)Ljava/lang/String;"))),
            Case("()Ljava/lang/String;", "zero-argument-literal"),
            Case("(I)Ljava/lang/String;", "\u0002\u0001\u0002", arrayOf(9), arrayOf(Long.MIN_VALUE, Double.NaN)),
            Case("()Ljava/lang/String;", "builder-tail", appends = primitiveAppends()),
            Case("()Ljava/lang/String;", "buffer-tail", appends = primitiveAppends(), builder = "java/lang/StringBuffer")
        )
        val dir = Files.createTempDirectory("stringblur-concat").toFile()
        try {
            val modes = Mode.values().toList()
            val src = File(dir, "src").apply { mkdirs() }
            modes.forEach { ModeUtils.getEncodeImplFile(it).create(src, "test.concat") }
            modes.forEach { StringBlurFile(master).create(src, "test.concat", listOf(it), "Runtime${it.name}", "decrypt") }
            val out = File(dir, "out").apply { mkdirs() }
            val errors = java.io.ByteArrayOutputStream()
            val compilerArgs = listOf("-d", out.absolutePath, "-cp", System.getProperty("java.class.path")) +
                src.listFiles()!!.filter { it.extension == "java" }.map { it.absolutePath }
            assertEquals(errors.toString(), 0, ToolProvider.getSystemJavaCompiler().run(null, null, errors, *compilerArgs.toTypedArray()))
            URLClassLoader(arrayOf(out.toURI().toURL()), javaClass.classLoader).use { runtimeLoader ->
                for (case in cases) {
                    val original = sample(case)
                    val expected = invoke(original, case, runtimeLoader)
                    for (bytesMode in listOf(BytesMode.STRING, BytesMode.BYTES)) {
                        // Also execute with every available encryption algorithm.
                        for (mode in modes) {
                            val transformed = transform(original, bytesMode, mode)
                            val node = ClassNode()
                            ClassReader(transformed).accept(node, 0)
                            assertFalse(node.methods.single().instructions.any { it is InvokeDynamicInsnNode })
                            val plaintext = case.recipe.split('\u0001', '\u0002').filter { it.isNotEmpty() } +
                                case.constants.filterIsInstance<String>()
                            assertFalse(node.methods.single().instructions.any { it is LdcInsnNode && it.cst in plaintext })
                            if (case.appends.isNotEmpty()) {
                                assertFalse(node.methods.single().instructions.any {
                                    it is org.objectweb.asm.tree.MethodInsnNode && it.owner == case.builder &&
                                        it.name == "append" && it.desc[1] != 'L'
                                })
                            }
                            assertEquals("$bytesMode/$mode: ${case.recipe}", expected, invoke(transformed, case, runtimeLoader))
                        }
                    }
                }
            }
        } finally {
            dir.deleteRecursively()
        }
    }

    @Test
    fun unsupportedOrMalformedSitesAreUntouched() {
        val dynamicConstant = ConstantDynamic("value", "Ljava/lang/String;", bootstrap)
        val unsupported = listOf(
            Case("(I)Ljava/lang/String;", "no-argument-marker", arrayOf(1)),
            Case("()Ljava/lang/String;", "missing=\u0002"),
            Case("()Ljava/lang/String;", "unused-constant", constants = arrayOf("unused")),
            Case("()Ljava/lang/String;", "handle=\u0002", constants = arrayOf(bootstrap)),
            Case("()Ljava/lang/String;", "dynamic=\u0002", constants = arrayOf(dynamicConstant)),
            Case("(I)Ljava/lang/String;", "\u0001", arrayOf(1)) // no fixed text to encrypt
        )
        for (case in unsupported) {
            var touched = false
            val visitor = object : MethodVisitor(Opcodes.ASM9) {
                override fun visitTypeInsn(opcode: Int, type: String) { touched = true }
                override fun visitVarInsn(opcode: Int, index: Int) { touched = true }
            }
            assertFalse(StringConcatRewriter.rewrite(visitor, case.name, case.descriptor, bootstrap,
                arrayOf(case.recipe, *case.constants)) { touched = true; IntArray(it.size) })
            assertFalse("Fallback must not emit instructions or allocate locals", touched)
        }
        assertFalse(StringConcatRewriter.rewrite(MethodVisitorSink(), "makeConcatWithConstants", "()Ljava/lang/String;",
            Handle(Opcodes.H_INVOKESTATIC, "other/Factory", bootstrap.name, bootstrap.desc, false),
            arrayOf("text")) { error("must not allocate") })
    }

    private class MethodVisitorSink : MethodVisitor(Opcodes.ASM9)

    private fun primitiveAppends(): List<Pair<Number, Char>> = listOf(
        1 to 'Z', 0 to 'Z', '中'.code to 'C', 0 to 'C', -1 to 'I', 5 to 'I',
        -7 to 'I', 300 to 'I', Int.MIN_VALUE to 'I', 0L to 'J', 1L to 'J', Long.MIN_VALUE to 'J',
        0.0f to 'F', 1.0f to 'F', 2.0f to 'F', -0.0f to 'F', Float.NaN to 'F', Float.POSITIVE_INFINITY to 'F',
        0.0 to 'D', 1.0 to 'D', -0.0 to 'D', Double.NaN to 'D', Double.NEGATIVE_INFINITY to 'D'
    )

    private fun sample(case: Case): ByteArray {
        val writer = ClassWriter(ClassWriter.COMPUTE_FRAMES or ClassWriter.COMPUTE_MAXS)
        writer.visit(Opcodes.V11, Opcodes.ACC_PUBLIC, "test/ConcatSample", null, "java/lang/Object", null)
        val method = writer.visitMethod(Opcodes.ACC_PUBLIC or Opcodes.ACC_STATIC, "join", case.descriptor, null, null)
        method.visitCode()
        // Keep another value below the concat operands to exercise stack preservation.
        method.visitTypeInsn(Opcodes.NEW, case.builder)
        method.visitInsn(Opcodes.DUP)
        method.visitMethodInsn(Opcodes.INVOKESPECIAL, case.builder, "<init>", "()V", false)
        for ((value, type) in case.appends) {
            when {
                value is Int && value in -1..5 -> method.visitInsn(Opcodes.ICONST_0 + value)
                value is Int && value in Byte.MIN_VALUE..Byte.MAX_VALUE -> method.visitIntInsn(Opcodes.BIPUSH, value)
                value is Int && value in Short.MIN_VALUE..Short.MAX_VALUE -> method.visitIntInsn(Opcodes.SIPUSH, value)
                value is Long && value in 0..1 -> method.visitInsn(Opcodes.LCONST_0 + value.toInt())
                value is Float && value.toRawBits() == 0.0f.toRawBits() -> method.visitInsn(Opcodes.FCONST_0)
                value is Float && value in 1.0f..2.0f && value % 1 == 0.0f -> method.visitInsn(Opcodes.FCONST_0 + value.toInt())
                value is Double && value.toRawBits() == 0.0.toRawBits() -> method.visitInsn(Opcodes.DCONST_0)
                value is Double && value == 1.0 -> method.visitInsn(Opcodes.DCONST_1)
                else -> method.visitLdcInsn(value)
            }
            method.visitMethodInsn(Opcodes.INVOKEVIRTUAL, case.builder, "append", "($type)L${case.builder};", false)
        }
        var index = 0
        for (type in Type.getArgumentTypes(case.descriptor)) {
            method.visitVarInsn(type.getOpcode(Opcodes.ILOAD), index)
            index += type.size
        }
        method.visitInvokeDynamicInsn(case.name, case.descriptor, bootstrap, case.recipe, *case.constants)
        method.visitMethodInsn(Opcodes.INVOKEVIRTUAL, case.builder, "append", "(Ljava/lang/String;)L${case.builder};", false)
        method.visitMethodInsn(Opcodes.INVOKEVIRTUAL, case.builder, "toString", "()Ljava/lang/String;", false)
        method.visitInsn(Opcodes.ARETURN)
        method.visitMaxs(0, 0)
        method.visitEnd()
        writer.visitEnd()
        return writer.toByteArray()
    }

    private fun transform(input: ByteArray, bytesMode: BytesMode, mode: Mode): ByteArray {
        val writer = ClassWriter(ClassWriter.COMPUTE_FRAMES or ClassWriter.COMPUTE_MAXS)
        val visitor = StringBlurClassVisitor(writer, "test/concat/stringblur/Runtime${mode.name}", "decrypt", master,
            bytesMode, listOf(mode), null, 0)
        ClassReader(input).accept(visitor, ClassReader.EXPAND_FRAMES)
        return writer.toByteArray()
    }

    private fun invoke(bytes: ByteArray, case: Case, parent: ClassLoader): Any? {
        val loader = object : ClassLoader(parent) {
            fun loadSample() = defineClass("test.ConcatSample", bytes, 0, bytes.size)
        }
        return loader.loadSample().declaredMethods.single().invoke(null, *case.arguments)
    }
}
