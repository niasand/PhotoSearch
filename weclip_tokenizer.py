# WeCLIP V2 Tokenizer 实现
# 注意：需要从 WeCLIP 仓库获取 tokenizer 的配置文件

import json
import torch
from typing import List, Tuple

class WeCLIPTokenizer:
    def __init__(self, vocab_path: str = "weclip_vocab.json"):
        with open(vocab_path, "r", encoding="utf-8") as f:
            self.vocab = json.load(f)
        self.id_to_token = {v: k for k, v in self.vocab.items()}
        self.max_len = 77
    
    def tokenize(self, text: str) -> Tuple[List[str], List[int]]:
        """中文分词和编码"""
        tokens = self._chinese_tokenize(text)
        tokens = ["<s>"] + tokens + ["</s>"]
        
        # 截断到最大长度
        if len(tokens) > self.max_len:
            tokens = tokens[:self.max_len-1] + ["</s>"]
        
        input_ids = self._convert_tokens_to_ids(tokens)
        return tokens, input_ids
    
    def _chinese_tokenize(self, text: str) -> List[str]:
        """简单的中文分词实现"""
        # 这里应该使用更完善的中文分词工具，如 jieba 或 HanLP
        tokens = []
        for char in text:
            if char.strip():
                tokens.append(char)
        return tokens
    
    def _convert_tokens_to_ids(self, tokens: List[str]) -> List[int]:
        """将 tokens 转换为 id"""
        input_ids = []
        for token in tokens:
            input_ids.append(self.vocab.get(token, self.vocab["<unk>"]))
        
        # 填充到最大长度
        if len(input_ids) < self.max_len:
            input_ids += [self.vocab["<pad>"]] * (self.max_len - len(input_ids))
        
        return input_ids
    
    def decode(self, ids: List[int]) -> str:
        """将 ids 转换为文本"""
        tokens = [self.id_to_token.get(id, "<unk>") for id in ids if id != self.vocab["<pad>"]]
        tokens = [token for token in tokens if token not in ["<s>", "</s>"]]
        return "".join(tokens)

if __name__ == "__main__":
    # 示例使用
    tokenizer = WeCLIPTokenizer()
    tokens, input_ids = tokenizer.tokenize("这是一张包含猫的图片")
    print("Tokens:", tokens)
    print("Input IDs:", input_ids)
    print("Decode:", tokenizer.decode(input_ids))
