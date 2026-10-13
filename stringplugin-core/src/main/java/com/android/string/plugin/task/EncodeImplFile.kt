package com.android.string.plugin.task

import com.android.string.plugin.data.Constant
import com.android.string.plugin.mode.Mode
import com.android.string.plugin.util.ModeUtils
import java.io.File

/**
 * 生成 IString 实现类：直接复制 demo_files 下对应算法的源码（打包在插件 jar 的资源里）并改写包名。
 * 构建期加密用的实现与打进 App 的解密实现因此始终是同一份源码，新增算法只需写一份 Java。
 *
 * @author chancey
 * @date   2026/10/12
 **/
class EncodeImplFile(private val mode: Mode) {

    fun create(path: File, applicationId: String) {
        val className = ModeUtils.getEncodeImplClassName(mode)
        val resource = "$RESOURCE_DIR/$className.java"
        val source = EncodeImplFile::class.java.classLoader.getResourceAsStream(resource)
            ?.use { it.readBytes().toString(Charsets.UTF_8) }
            ?: throw IllegalStateException("找不到算法源码资源: $resource")
        val pkg = Constant.PLUGIN_CLASS_PACKAGE.format(applicationId)
        File(path, "$className.java").writeText(rewritePackage(source, pkg), Charsets.UTF_8)
    }

    companion object {
        private const val RESOURCE_DIR = "com/android/string/plugin/demo_files"
        private val PACKAGE_LINE = Regex("^package\\s+[\\w.]+;[ \\t]*\\r?\\n", RegexOption.MULTILINE)
        private val PACKAGE_HINT_LINE = Regex("^//\\s*package\\s+[\\w.]+;[ \\t]*\\r?\\n", RegexOption.MULTILINE)

        internal fun rewritePackage(source: String, pkg: String): String {
            val withoutHint = PACKAGE_HINT_LINE.replace(source, "")
            check(PACKAGE_LINE.containsMatchIn(withoutHint)) { "算法源码缺少 package 声明" }
            return PACKAGE_LINE.replaceFirst(withoutHint, "package $pkg;\n")
        }
    }
}
