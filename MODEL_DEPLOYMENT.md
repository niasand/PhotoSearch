# 模型转换和部署指南

## 1. 获取 WeCLIP V2 模型

### 官方仓库
WeCLIP 是微信/腾讯 AI Lab 开发的多模态图文理解模型：
- GitHub: https://github.com/Tencent/WeCLIP
- 论文: https://arxiv.org/abs/xxxx.xxxx

### 模型下载
1. 从 GitHub Release 下载最新版本的模型
2. 或使用 huggingface: `huggingface.co/Tencent/WeCLIP-v2`
3. 自己训练模型（参考官方文档）

## 2. 模型转换 (ONNX Runtime)

### 转换为 ONNX 格式
如果从 PyTorch 模型开始：
```python
import torch
from models import WeCLIP

model = WeCLIP.from_pretrained("weclip-v2")
model.eval()

# 图片编码模型
dummy_input_image = torch.randn(1, 3, 224, 224)
torch.onnx.export(
    model.image_encoder,
    dummy_input_image,
    "weclip_v2_image.onnx",
    opset_version=13,
    input_names=["image"],
    output_names=["feature"]
)

# 文本编码模型
dummy_input_text = torch.randint(0, 10000, (1, 77))
torch.onnx.export(
    model.text_encoder,
    dummy_input_text,
    "weclip_v2_text.onnx",
    opset_version=13,
    input_names=["text"],
    output_names=["feature"]
)
```

### 量化优化 (端侧)
```bash
# 量化为 INT8 模型
onnxruntime-quantize \
  --input weclip_v2_image.onnx \
  --output weclip_v2_image_quantized.onnx \
  --mode qint8

# 文本模型同理
onnxruntime-quantize \
  --input weclip_v2_text.onnx \
  --output weclip_v2_text_quantized.onnx \
  --mode qint8
```

## 3. 部署到 Android

### 放入项目目录
将转换好的模型放入：
```
app/src/main/assets/models/
├── weclip_v2_image.onnx
└── weclip_v2_text.onnx
```

### 更新 AndroidManifest.xml
```xml
<!-- 添加必要权限 -->
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_DATA_SYNC" />
```

## 4. 性能优化

### 内存管理
- 使用 `inSampleSize` 缩小图片尺寸
- 及时释放 Bitmap
- 使用 CoroutineScope 管理后台任务

### 推理优化
- 使用 ONNX Runtime 内置优化器
- 调整 batch size
- 开启多线程推理
```kotlin
val sessionOptions = OrtSession.SessionOptions().apply {
    setInterOpNumThreads(2)
    setIntraOpNumThreads(4)
    enableMemoryPattern(true)
    enableProfiling(false)
}
```

## 5. 测试构建

### 构建 Debug APK
```bash
./gradlew assembleDebug
```

### 运行测试
```bash
./gradlew test
```

### 运行 Lint 检查
```bash
./gradlew lint
```

## 6. 常见问题

### 模型加载失败
1. 检查模型路径是否正确
2. 确认 ONNX Runtime 版本与模型 opset 版本兼容
3. 尝试在 x86 模拟器上测试，某些设备可能不支持

### 推理速度慢
1. 使用量化后的模型
2. 减少线程数
3. 缩小输入图片尺寸

### 权限问题
1. 动态申请权限
2. 处理权限拒绝的情况
3. 提供权限引导界面
