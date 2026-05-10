# StringBlur

最新版本：`2.0.0`

StringBlur 是一个 Android Gradle 插件，用于在构建阶段对 class 中的字符串常量进行加密，并在运行时自动解密。

## 安装

### plugins DSL / Version Catalog

在 `settings.gradle` 中配置插件仓库：

```groovy
pluginManagement {
    repositories {
        maven { url "https://raw.githubusercontent.com/dawnuu/maven/refs/heads/main/gradle/"}
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
```

在 `gradle/libs.versions.toml` 中声明插件：

```toml
[plugins]
stringblur = { id = "stringblur", version = "2.0.0" }
```

根目录 `build.gradle`：

```groovy
plugins {
    alias(libs.plugins.stringblur) apply false
}
```

app 或 library 模块 `build.gradle`：

```groovy
plugins {
    alias(libs.plugins.stringblur)
}
```

### buildscript

也可以使用传统 `buildscript` 方式：

```groovy
buildscript {
    repositories {
        maven { url "https://raw.githubusercontent.com/dawnuu/maven/refs/heads/main/gradle/"}
        google()
        mavenCentral()
    }
    dependencies {
        classpath 'com.android.string.plugin:stringblur:2.0.0'
    }
}
```

模块 `build.gradle`：

```groovy
apply plugin: 'stringblur'
```

## 配置

```groovy
import com.android.string.plugin.mode.BytesMode
import com.android.string.plugin.mode.Mode

stringblur {
    key 'Hello World'
    enable true

    whiteList = ['com.xxx.xxx.BuildConfig']
    encodePackages = ['com.xxx.xxx']

    modes = [Mode.XOR, Mode.SHIFT, Mode.XOR_SHIFT]
    bytesMode = BytesMode.STRING
    minLength 3
}
```

## 配置项

- `key`：加密密钥。支持字符串，也支持整数随机长度，例如 `key 16`。
- `enable`：是否开启字符串加密，默认 `false`。
- `whiteList`：类名或包名前缀白名单，匹配到的 class 不处理。
- `encodePackages`：加密范围。`null` 表示处理全部 class；空列表表示只处理当前 applicationId/namespace；非空列表会在当前 applicationId/namespace 基础上追加包名前缀。
- `modes`：加密方式列表。每个字符串会从列表中随机选择一种方式，默认 `[Mode.DEFAULT]`。
- `bytesMode`：密文承载方式，默认 `BytesMode.STRING`。
- `minLength`：最小加密长度，长度小于该值的字符串会跳过，默认 `0`。

## 加密方式

- `Mode.DEFAULT`：按 key 对字节做加减变换。
- `Mode.XOR`：按 key 对字节做异或变换。
- `Mode.REVERSE`：反转字节顺序。
- `Mode.SHIFT`：按 key 低位对字节做位移变换。
- `Mode.XOR_SHIFT`：组合 XOR 与 SHIFT 变换。

## 密文承载方式

- `BytesMode.STRING`：密文写成字符串常量。
- `BytesMode.BYTES`：密文写成 byte array。
- `BytesMode.RANDOM`：每个字符串随机选择 `STRING` 或 `BYTES`。

## 加密报告

开启插件后会按 variant 生成报告文件：

```text
build/reports/stringblur/{variant}.txt
```

报告事件包括：

- `SCAN`：扫描到 class。
- `SKIP`：class 因白名单或 `encodePackages` 范围被跳过。
- `ENCRYPT`：字符串已加密，并记录实际使用的 `mode` 和 `bytesMode`。
- `IGNORE`：LDC 常量未加密，例如空字符串、非字符串常量或长度小于 `minLength`。

## 注意事项

- 注解参数字符串不能替换为运行时解密调用，因此不会按普通字符串加密。
- 资源、Manifest、assets、raw 等文件不属于 class ASM 处理范围。
- 插件默认使用 `InstrumentationScope.ALL`，会处理项目 class 和依赖 class；依赖较多时构建耗时会增加。