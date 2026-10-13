package com.android.string.plugin.util

import com.android.string.plugin.mode.BytesMode
import com.android.string.plugin.mode.Mode

/**
 * 由配置拼出稳定种子：主密钥材料与解密入口名都由它得出。
 * 配置不变则种子不变（增量构建与构建缓存不受影响）；
 * [userSeed] 是用户可选的盐，改动它会让全部产物变化，但它不是需要保密的密钥。
 *
 * @author chancey
 * @date   2026/10/12
 **/
object SeedBuilder {

    fun build(
        pluginVersion: String,
        variantName: String,
        applicationId: String,
        modes: List<Mode>,
        bytesMode: BytesMode,
        userSeed: String
    ): String {
        return listOf(
            pluginVersion,
            variantName,
            applicationId,
            modes.joinToString(",") { it.name },
            bytesMode.name,
            userSeed
        ).joinToString("|")
    }
}
