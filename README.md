##### lastest:1.1.7

##### 1、在根目录build.gradle中引入插件依赖。

```
buildscript {
    dependencies {
        ...
        classpath "com.android.string.plugin:stringblur:${lastest}"
    }
}
```

##### 2、在app或lib的build.gradle中配置插件。

```
apply plugin: 'stringblur'

stringblur {
    key 'Hello World' //加密key，自由定义，1.1.0版本支持随机密钥，如key 6，表示随机生成长度为6的密钥
    enable true //混淆开关，默认关闭
    whiteList = ['com.xxx.xxx'] //白名单，默认加密全部，例：不加密MainActivity类['com.xxx.xxx.MainActivity']，不加密某个包['com.xxx.xxx']
    //1.1.3之前：默认加密全部，1.1.3修改为：null时加密全部，[]空列表时加密自身，非空列表则追加自定义列表
    encodePackages = ['com.xxx.xxx']
    //1.1.5新增，1.1.6删除
    customEncodeClass = "com.xxx.xxx.Impl" //自定义加密，需要实现IString接口，implementation "com.android.string.plugin:common:${lastest}"
    //1.1.6新增
    mode = Mode.DEFAULT/Mode.XOR //import com.android.string.plugin.mode.Mode
    // 1.1.0版本移除以下字段
    pkg 'stringblur' //加密相关类所在路径，默认在包名下的stringblur目录中，可以移动到其他目录，如encode.test，则移动到包名下的encode/test中 
    alias 'StringBlur' //加密类别名，默认加密类为StringBlur.java
    // 1.1.0版本新增字段
    useBytes false //是否使用字节码形式加密，默认关闭，开启后加密内容将显示new byte[]形式
}
```

## 更新日志

### v1.1.7
- 最低AGP7.4.0
- 适配AGP9

### v1.1.6

- 新增mode属性：选择加密方式，DEFAULT、XOR
- 删除customEncodeClass属性

### v1.1.5

- 新增customEncodeClass属性：支持自定义加密类

### v1.1.3

- encodePackages属性修改：null加密全部，[]加密自身，[xxx]追加xxx加密

### v1.1.2

- 修改为默认仅加密自身

### v1.1.1

- 兼容性优化

### v1.1.0

- 增加随机密钥功能
- 支持使用字节码形式加密
- 移除自定义类名字段

### v1.0.2

- 加密优化：解决部分字段无法加密问题
- 修复encodePackages不配置时无法加密的问题

### v1.0.1

- 优化白名单功能
- 增加自定义加密类功能
- 兼容性优化：支持AGP8，最低需要gradle7.4

### v1.0.0

- 初始版本