import json
import os
from typing import Any, Dict, List, Optional

from django import forms
from django.conf import settings
from django.core.exceptions import ValidationError
from django.utils import timezone


class ValidationConfigLoader:
    """从 settings.FORM_VALIDATION_CONFIG_PATH 读取 JSON 配置。
    - 不存在/解析失败时返回空字典
    - 内存缓存，避免重复 IO
    """

    def __init__(self, config_path: Optional[str] = None) -> None:
        self.config_path = config_path or getattr(
            settings,
            "FORM_VALIDATION_CONFIG_PATH",
            os.path.join(settings.BASE_DIR, "config", "form_validation.json"),
        )
        self._cache: Optional[Dict[str, Any]] = None
        self._mtime: Optional[float] = None

    def load(self) -> Dict[str, Any]:
        try:
            if not os.path.exists(self.config_path):
                return {}
            mtime = os.path.getmtime(self.config_path)
            if self._cache is None or self._mtime is None or mtime > self._mtime:
                with open(self.config_path, "r", encoding="utf-8") as f:
                    self._cache = json.load(f)
                self._mtime = mtime
            return self._cache or {}
        except Exception as e:
            # 打印并返回空配置
            print(f"[DynamicValidation] 配置加载失败: {e}")
            return {}

    def get_form_config(self, form_name: str) -> Dict[str, Any]:
        cfg = self.load()
        # 简化结构：顶层即表单名键
        return cfg.get(form_name, {})


class ValidationMethodGenerator:
    """根据配置生成 clean()/clean_<field>() 方法。"""

    @staticmethod
    def create_clean_method(rules: List[Dict[str, Any]]):
        def clean(self):
            cleaned_data = super(type(self), self).clean()
            ctx = {"cleaned_data": cleaned_data, "timezone": timezone}
            for rule in rules or []:
                try:
                    field = rule.get("field")
                    condition = rule.get("condition")
                    error = rule.get("error")
                    if field:
                        ctx[field] = cleaned_data.get(field)
                    if condition and ValidationMethodGenerator._eval(condition, ctx):
                        # 表单级错误添加到目标字段
                        self.add_error(field, error)
                except Exception as e:
                    print(f"[DynamicValidation] clean 规则执行错误: {e}")
            return cleaned_data
        return clean

    @staticmethod
    def create_field_clean_method(field_name: str, rules: List[Dict[str, Any]]):
        def field_clean(self):
            value = self.cleaned_data.get(field_name)
            ctx = {"value": value, "timezone": timezone, "cleaned_data": self.cleaned_data}
            for rule in rules or []:
                try:
                    condition = rule.get("condition")
                    error = rule.get("error")
                    if condition and ValidationMethodGenerator._eval(condition, ctx):
                        raise ValidationError(error)
                except ValidationError:
                    raise
                except Exception as e:
                    print(f"[DynamicValidation] clean_{field_name} 规则执行错误: {e}")
            return value
        return field_clean

    @staticmethod
    def _eval(expr: str, context: Dict[str, Any]) -> bool:
        """最小表达式求值：使用 eval，禁用内置，提供受限上下文。
        仅用于简单比较/逻辑表达式。
        """
        try:
            return bool(eval(expr, {"__builtins__": {}}, context))
        except Exception:
            return False


class DynamicValidationForm(forms.Form):
    """在 __init__ 时加载配置并注入 clean/clean_<field> 方法（若未显式定义）。"""

    _config_loader = ValidationConfigLoader()

    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self._inject_methods()

    def _inject_methods(self) -> None:
        form_name = type(self).__name__
        cfg = self._config_loader.get_form_config(form_name)
        if not cfg:
            return
        # clean 方法：如果当前类未自定义 clean（仅继承基类），则注入
        if "clean" in cfg and ("clean" not in type(self).__dict__):
            clean_method = ValidationMethodGenerator.create_clean_method(cfg.get("clean", []))
            setattr(self, "clean", clean_method.__get__(self, type(self)))
        # clean_<field> 方法
        for key, rules in cfg.items():
            if key.startswith("clean_"):
                field_name = key[6:]
                if field_name in self.fields and not hasattr(self, key):
                    m = ValidationMethodGenerator.create_field_clean_method(field_name, rules or [])
                    setattr(self, key, m.__get__(self, type(self)))


class DynamicValidationModelForm(forms.ModelForm):
    """ModelForm 的动态注入版本。"""

    _config_loader = ValidationConfigLoader()

    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self._inject_methods()

    def _inject_methods(self) -> None:
        form_name = type(self).__name__
        cfg = self._config_loader.get_form_config(form_name)
        if not cfg:
            return
        # clean 方法：如果当前类未自定义 clean（仅继承基类），则注入
        if "clean" in cfg and ("clean" not in type(self).__dict__):
            clean_method = ValidationMethodGenerator.create_clean_method(cfg.get("clean", []))
            setattr(self, "clean", clean_method.__get__(self, type(self)))
        for key, rules in cfg.items():
            if key.startswith("clean_"):
                field_name = key[6:]
                if field_name in self.fields and not hasattr(self, key):
                    m = ValidationMethodGenerator.create_field_clean_method(field_name, rules or [])
                    setattr(self, key, m.__get__(self, type(self)))
