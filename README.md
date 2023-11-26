##### 1、在根目录build.gradle中引入插件依赖。

```
buildscript {
    dependencies {
        ...
        classpath "com.android.string.plugin:stringblur:1.0.1"
    }
}
```

##### 2、在app或lib的build.gradle中配置插件。

```
apply plugin: 'stringblur'

stringblur {
    key 'Hello World' //加密key，自由定义
    enable true //混淆开关，默认关闭
    whiteList = ['com.xxx.xxx'] //白名单，默认加密全部，例：不加密MainActivity类['com.xxx.xxx.MainActivity']，不加密某个包['com.xxx.xxx']
    encodePackages = ['com.xxx.xxx'] //自定义加密目录，默认加密全部
    pkg 'stringblur' //加密相关类所在路径，默认在包名下的stringblur目录中，可以移动到其他目录，如encode.test，则移动到包名下的encode/test中 
    alias 'StringBlur' //加密类别名，默认加密类为StringBlur.java
}
```

## 更新日志

### v1.0.1
- 优化白名单功能
- 增加自定义加密类功能
- 兼容性优化：支持AGP8，最低需要gradle7.4

### v1.0.0

- 初始版本