# Changelog

## v1.0.5：修复字节数组模式的方法体膨胀

- 修复 `BytesMode.BYTES` 及 `BytesMode.RANDOM` 选中 BYTES 时，内联数组初始化导致 SDK 方法超过 64 KB 的问题。
- 将字节数组初始化与解密调用移入 synthetic 辅助方法，长数组按 4096 字节分块。
- 避免辅助方法与原方法重名，支持 Java 8 接口；Java 7 及更早的接口自动使用 STRING。
- 增加大量字符串、长密文、接近 64 KB 方法、接口及静态字段初始化的回归测试。

## v1.0.4：简化配置、逐字符串密钥派生与拼接保护增强

### 破坏性更新（从 1.0.3 升级前请检查）

- **删除算法模式**：`Mode.REVERSE`、`Mode.SHIFT`、`Mode.XOR_SIMD`。请从 `modes` 中移除，改用当前支持的 `DEFAULT`、`XOR`、`XOR_SHIFT`、`FAST_ROT`、`XOR_CHAIN` 或 `LCG_STREAM`。
- **删除 `key` 字段**：插件自动派生每个字符串的独立密钥，不再读取 Gradle/local.properties 中的 `stringblur.key` 或环境变量 `STRINGBLUR_KEY`。请删除旧配置；可选的 `seed` 用于区分项目产物，默认为空，不是密钥。
- **删除智能算法选择配置**：移除 `selectionStrategy`、`performanceWeight`、`securityWeight`，以及 `SelectionStrategy`、`SecurityLevel` 和 `SmartAlgorithmSelector`。现在仅通过 `modes` 选择算法：单个直接使用，多个按字符串随机选择，空列表回退为 `DEFAULT`。
- **调整默认处理范围**：默认仅对当前项目插桩。需要包含第三方依赖时，请显式设置 `includeDependencies = true`；包范围和白名单过滤仍然生效。
- **更新密文与运行时生成逻辑**：升级后应完整重新构建应用，避免混用旧版密文和新版解密代码。

### 新增与改进

- 新增 `XOR_CHAIN`、`LCG_STREAM` 模式。
- 引入逐字符串密钥派生，统一算法源码生成和 UTF-8 编解码。
- 增强 `invokedynamic` 字符串拼接，以及 `StringBuilder`／`StringBuffer` 直接追加常量的保护。
- 在构建结束统一输出处理报告。

### 升级后的配置示例（Kotlin DSL）

```kotlin
import com.android.string.plugin.mode.Mode

stringblur {
    enable = true
    seed = "my-project-seed" // 可选，不是密钥
    modes = listOf(Mode.XOR, Mode.FAST_ROT, Mode.XOR_SHIFT)

    // 需要处理第三方依赖时开启
    // includeDependencies = true
}
```

升级插件版本至 `1.0.4`，移除旧字段及相关 import，再执行一次 clean build。
