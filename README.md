# PhotoSearch App - 本地相册搜索

基于 WeCLIP V2 端侧图文模型的智能相册搜索应用。

## 功能特性

- 🔍 **图文互搜**：输入文字描述，搜索相关图片
- 🖼️ **图图相似**：选择一张图片，找到相似图片
- 📝 **图文相似**：分析图片内容，生成文字描述
- 🗑️ **重复图片筛选**：智能检测并标记重复/相似图片
- ⚡ **端侧推理**：基于 WeCLIP V2 模型，无需联网，保护隐私
- 🚀 **快速索引**：后台自动构建图片特征向量索引

## 技术栈

- **UI**: Jetpack Compose + Material Design 3
- **架构**: MVVM + Clean Architecture
- **本地存储**: Room + DataStore
- **模型推理**: ONNX Runtime + WeCLIP V2
- **向量搜索**: Faiss / 本地向量索引
- **依赖注入**: Hilt
- **异步处理**: Kotlin Coroutines + Flow

## 项目结构

```
app/
├── src/main/java/com/photosearch/app/
│   ├── data/           # 数据层（Repository、数据库、模型）
│   ├── domain/         # 领域层（UseCase、Entity）
│   ├── presentation/   # 表现层（ViewModel、Screen）
│   ├── service/        # 后台服务（索引服务）
│   └── utils/          # 工具类
├── src/main/res/       # 资源文件
└── src/main/assets/    # 模型文件（weclip_v2.onnx）
```

## 模型说明

本应用使用 WeCLIP V2 模型进行图文特征提取：
- 输入：图片（224x224）或文本
- 输出：512 维特征向量
- 相似度计算：余弦相似度

模型文件需放置于 `app/src/main/assets/models/` 目录下。

## 构建要求

- Android Studio Hedgehog (2023.1.1) 或更高版本
- JDK 17
- Android SDK 34
- NDK (如需编译原生库)

## 快速开始

1. 克隆项目
```bash
git clone https://github.com/yourusername/PhotoSearch.git
cd PhotoSearch
```

2. 下载 WeCLIP V2 模型
```bash
# 模型下载地址（示例，需替换为实际地址）
wget https://github.com/Tencent/WeCLIP/releases/download/v2.0/weclip_v2_quantized.onnx \
  -O app/src/main/assets/models/weclip_v2.onnx
```

3. 构建并运行
```bash
./gradlew assembleDebug
```

## 权限说明

- `READ_MEDIA_IMAGES`: 读取本地相册图片
- `POST_NOTIFICATIONS`: 显示索引进度通知
- `FOREGROUND_SERVICE`: 后台索引服务

## 贡献

欢迎提交 Issue 和 PR！

## 许可证

MIT License
