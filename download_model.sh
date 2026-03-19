#!/bin/bash

# WeCLIP V2 模型下载脚本
# 注意：WeCLIP V2 模型需要从微信/Tencent 的官方渠道获取

MODEL_DIR="app/src/main/assets/models"

# 创建目录
mkdir -p "$MODEL_DIR"

# 提示信息
echo "⚠️  WeCLIP V2 模型获取说明"
echo ""
echo "由于 WeCLIP V2 是微信开发的模型，模型文件需要从官方渠道获取："
echo "1. 访问 WeCLIP GitHub 仓库: https://github.com/Tencent/WeCLIP"
echo "2. 下载 ONNX 格式的模型"
echo "3. 转换模型为适合 Android 端侧运行的格式"
echo "4. 将模型文件放入 $MODEL_DIR 目录"
echo ""
echo "📦 需要以下文件:"
echo "  - weclip_v2_image.onnx (图片特征提取模型)"
echo "  - weclip_v2_text.onnx  (文本特征提取模型)"
echo ""
echo "🔧 模型转换:"
echo "使用 ONNX Runtime 工具将模型量化优化:"
echo "  onnxruntime-quantize --input model.onnx --output model_quantized.onnx --mode qint8"
echo ""
echo "📄 参考文档:"
echo "- ONNX Runtime Android: https://onnxruntime.ai/docs/get-started/with-android.html"
echo "- WeCLIP 官方文档: https://github.com/Tencent/WeCLIP"
