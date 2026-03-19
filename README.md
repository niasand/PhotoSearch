# PhotoSearch App - 本地相册搜索

基于 **WeCLIP V2 端侧图文模型** 的智能相册搜索应用，无需联网即可实现图文互搜、重复图片筛选、图图相似等功能。

## 🌟 功能特性

- 📝 **图文互搜**：输入文字描述，搜索相关图片（如：海边、猫咪、美食）
- 🖼️ **图图相似**：选择一张图片，找到相册中相似的图片
- 🗑️ **重复图片筛选**：智能检测并标记重复/高相似度图片
- 🔍 **图文相似**：自动分析图片内容，生成文字描述
- ⚡ **端侧推理**：WeCLIP V2 模型本地运行，无需联网，保护隐私
- 🚀 **快速索引**：后台自动构建图片特征向量索引
- 📱 **现代 UI**：Jetpack Compose + Material Design 3

## 🏗️ 技术栈

- **UI**: Jetpack Compose + Material Design 3
- **架构**: MVVM + Clean Architecture + Hilt 依赖注入
- **本地存储**: Room + DataStore
- **模型推理**: ONNX Runtime + WeCLIP V2
- **向量搜索**: 余弦相似度计算
- **异步处理**: Kotlin Coroutines + Flow
- **图片加载**: Coil Compose
- **权限管理**: Accompanist Permissions
- **CI/CD**: GitHub Actions + Gradle Build

## 📦 项目结构

```
app/src/main/java/com/photosearch/app/
├── data/                 # 数据层
│   ├── local/          # Room 数据库（图片元数据、特征向量）
│   ├── model/          # WeCLIP V2 模型推理接口
│   └── repository/     # PhotoSearchRepository
├── domain/              # 领域层
│   └── usecase/        # UseCase 实现（搜索、索引、去重）
├── presentation/        # 表现层
│   ├── home/           # 主界面 + ViewModel
│   ├── similar/        # 相似图片搜索
│   ├── duplicate/      # 重复图片检测
│   ├── settings/       # 设置界面
│   └── Navigation.kt   # 导航配置
├── service/             # 后台服务
│   └── IndexingService # 相册索引服务
├── di/                 # 依赖注入
├── utils/              # 工具类
└── ui/theme/           # Material Design 3 主题
```

## 🚀 快速开始

### 1. 克隆项目
```bash
git clone https://github.com/niasand/PhotoSearch.git
cd PhotoSearch
chmod +x gradlew
```

### 2. 获取 WeCLIP V2 模型
⚠️ **重要**：需要获取 WeCLIP V2 ONNX 模型（微信开发的模型）

```bash
# 查看模型获取和转换说明
./download_model.sh

# 模型放入以下目录
mkdir -p app/src/main/assets/models/
# app/src/main/assets/models/
# ├── weclip_v2_image.onnx （图片特征提取）
# └── weclip_v2_text.onnx （文本特征提取）
```

详细说明见 [MODEL_DEPLOYMENT.md](MODEL_DEPLOYMENT.md)

### 3. 构建 APK
```bash
# 构建 Debug APK
./gradlew assembleDebug

# 安装到设备
adb install app/build/outputs/apk/debug/app-debug.apk

# 或直接运行
./gradlew connectedDebugAndroidTest
```

### 4. 测试
```bash
# 运行单元测试
./gradlew test

# 运行 UI 测试
./gradlew connectedDebugAndroidTest

# 代码检查
./gradlew lint
```

## 📱 使用指南

### 首次使用
1. 安装并打开应用
2. 授予相册读取权限
3. 点击右下角按钮开始索引相册（根据图片数量可能需要几分钟）
4. 索引完成后即可使用各种搜索功能

### 功能使用

**文搜图**:
- 在搜索栏输入文字描述（如："海边日落"、"可爱猫咪"）
- 点击搜索按钮或回车
- 查看搜索结果，支持按相似度排序

**图搜图**:
- 点击任意图片
- 进入相似图片搜索界面
- 显示所有相似图片及相似度百分比

**重复图片检测**:
- 点击右上角重复图片图标
- 等待扫描完成，显示所有重复组
- 可选择删除不需要的重复图片

## ⚙️ GitHub Actions CI/CD

项目已配置完整的 CI/CD 流程：

### Pull Request / Push
- 自动运行单元测试
- 构建 Debug APK
- 代码静态分析（Lint）
- 上传构建产物

### Release 发布
- 自动构建 Release APK 和 AAB
- 上传到 GitHub Release
- 发布通知

## 📜 权限说明

| 权限 | 用途 |
|------|------|
| `READ_MEDIA_IMAGES` | 读取本地相册图片 |
| `POST_NOTIFICATIONS` | 显示索引进度通知 |
| `FOREGROUND_SERVICE` | 后台索引服务 |
| `FOREGROUND_SERVICE_DATA_SYNC` | 数据同步服务 |

## 🤝 贡献

欢迎提交 Issue 和 PR！请遵循以下规范：

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开 Pull Request

## 📄 许可证

MIT License - 详见 [LICENSE](LICENSE)

## 🙏 致谢

- [WeCLIP](https://github.com/Tencent/WeCLIP) - 微信 AI Lab 开发的多模态模型
- [ONNX Runtime](https://onnxruntime.ai/) - 微软开源的推理引擎
- [Jetpack Compose](https://developer.android.com/jetpack/compose) - Android 现代 UI 框架

## 📞 联系方式

如有问题或建议，欢迎提交 Issue 或邮件联系！

