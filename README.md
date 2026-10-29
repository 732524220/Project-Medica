# Project Medica

一个正在开发中的 Minecraft Java 版 Fabric 草药与医药模组。

## 当前内容

- 野生薄荷自然生成、采集与耕地种植，包含四档生长外观和骨粉催熟。
- 满水炼药锅下放置燃烧的营火或岩浆块，投入两个薄荷，累计加热 400 游戏刻（正常速度约 20 秒），冲泡四瓶薄荷茶。失去热源时暂停，恢复热源后继续。
- 用玻璃瓶手动盛取；饮用恢复 2 点饥饿值，饱和系数为 0.3，饮后返还玻璃瓶。
- 获得薄荷解锁“医学之始”，首次喝下薄荷茶解锁“清凉一夏”。根进度使用单蛇缠杖图标。

搅拌保留为设计方案；漏斗投料、自动补水和自动装瓶暂缓。薄荷茶目前没有治疗或防暑效果。

## 开发环境

- Minecraft Java 1.21.4、Fabric Loader 0.19.5、Fabric API 0.119.4+1.21.4。
- Gradle Wrapper 9.7.1、Fabric Loom 1.18-SNAPSHOT，使用 Mojang mappings。
- **构建 Gradle 使用 JDK 25 或更高版本，开发游戏使用 JDK 21。** 本地使用 JDK 26 构建，编译目标为 Java 21。

用 IntelliJ IDEA 打开项目根目录并导入 Gradle。将 Gradle JVM 设为 JDK 25 或更高版本。`gradle.properties` 中的 `org.gradle.java.installations.paths` 是原开发机器的 JDK 21 路径；在另一台机器上，请改为自己的 JDK 21 安装目录。

Windows PowerShell 中，在项目根目录运行：

```powershell
.\gradlew.bat runClient
.\gradlew.bat build
```

构建产物为 `build/libs/project-medica-1.0.0.jar`；安装模组时使用这个文件，不使用 `-sources.jar`。游戏需要同时安装对应的 Fabric Loader 与 Fabric API。

按需运行独立服务端 GameTest：

```powershell
.\gradlew.bat runGameTest -PmedicaGameTests
```

测试世界位于 `build/run/gameTest`，测试代码不会进入发布 JAR。更多验证步骤见 [冲泡测试说明](docs/mint-tea-testing.md)。

## 文档与许可

- [开发进度](MEMORY.md)
- [设定文档](docs/Project_Medica_Design_v0.2.txt)
- 当前沿用项目已有的 [CC0-1.0 许可](LICENSE)。
