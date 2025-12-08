# OOP源码阅读：Djanjo-Forms表单验证机制解析
## 一、核心流程概述
Django的forms表单验证核心是通过「类定义→元类处理→实例初始化→数据校验」四个阶段完成，最终通过`is_valid()`方法返回验证结果，合法数据存储在`cleaned_data`中，错误信息存储在`errors`中。
在下面,我们跟随web开发中使用Forms表单的典型流程，逐步解析其核心源码实现。

## 二、关键类与方法解析
### 1. 元类处理：DeclarativeFieldsMetaclass
负责动态创建Form类，提取自定义字段并绑定到类属性。
```python
from django.forms.fields import Field
from django.forms.widgets import MediaDefiningClass
from collections import OrderedDict

class DeclarativeFieldsMetaclass(MediaDefiningClass):
    def __new__(mcs, name, bases, attrs):
        # 提取所有Field子类实例（自定义表单字段）
        current_fields = []
        for key, value in list(attrs.items()):
            if isinstance(value, Field):
                current_fields.append((key, value))
                attrs.pop(key)  # 从类属性中移除字段定义
        
        # 绑定声明字段到类属性
        attrs['declared_fields'] = OrderedDict(current_fields)
        new_class = super().__new__(mcs, name, bases, attrs)
        
        # 合并父类字段（支持继承）
        declared_fields = OrderedDict()
        for base in reversed(new_class.__mro__):
            if hasattr(base, 'declared_fields'):
                declared_fields.update(base.declared_fields)
            # 过滤空字段
            for attr, value in base.__dict__.items():
                if value is None and attr in declared_fields:
                    declared_fields.pop(attr)
        
        # 绑定最终字段集合
        new_class.base_fields = declared_fields
        new_class.declared_fields = declared_fields
        return new_class
```
**核心作用**：
- 提取自定义字段（如name、password），从类属性中移除并单独存储
- 合并父类表单字段，支持表单类继承
- 绑定`base_fields`和`declared_fields`属性，存储所有有效字段

### 2. 基类初始化：BaseForm.__init__
Form类的真正初始化方法，处理传入数据和配置。
```python
from django.utils.html import html_safe
from django.forms.utils import ErrorList
from .renderers import get_default_renderer
import copy

@html_safe
class BaseForm:
    default_renderer = None
    field_order = None
    prefix = None
    use_required_attribute = True

    def __init__(self, data=None, files=None, auto_id='id_%s', prefix=None,
                 initial=None, error_class=ErrorList, label_suffix=None,
                 empty_permitted=False, field_order=None, use_required_attribute=None, renderer=None):
        # 标记是否绑定数据（传入data或files即视为绑定）
        self.is_bound = data is not None or files is not None
        self.data = {} if data is None else data
        self.files = {} if files is None else files
        self.auto_id = auto_id
        self.prefix = prefix
        self.initial = initial or {}
        self.error_class = error_class
        self.label_suffix = label_suffix if label_suffix is not None else _(':')
        self.empty_permitted = empty_permitted
        self._errors = None  # 错误信息存储
        
        # 深拷贝字段集合，避免修改影响原类
        self.fields = copy.deepcopy(self.base_fields)
        self._bound_fields_cache = {}
        
        # 字段排序和渲染器配置（略）
        self.order_fields(self.field_order if field_order is None else field_order)
        # ... 其他配置逻辑
```
**核心作用**：
- 接收请求数据（request.POST/request.FILES）
- 初始化错误存储和字段集合
- 配置表单渲染相关参数（auto_id、label_suffix等）

### 3. 验证入口：is_valid()
验证结果的入口方法，通过`errors`属性触发实际校验。
```python
def is_valid(self):
    # 已绑定数据且无错误则返回True
    return self.is_bound and not self.errors
```

### 4. 错误处理：errors属性
通过`full_clean()`完成完整校验，返回错误信息字典。
```python
@property
def errors(self):
    if self._errors is None:
        self.full_clean()  # 触发完整校验
    return self._errors
```

### 5. 完整校验：full_clean()
协调字段校验、表单级校验和后续处理。
```python
from django.forms.utils import ErrorDict

def full_clean(self):
    self._errors = ErrorDict()  # 初始化错误字典
    if not self.is_bound:
        return
    
    self.cleaned_data = {}  # 存储校验通过的数据
    if self.empty_permitted and not self.has_changed():
        return
    
    # 依次执行三级校验
    self._clean_fields()  # 字段级校验
    self._clean_form()    # 表单级校验
    self._post_clean()    # 后续处理（模型表单用）
```

### 6. 字段级校验：_clean_fields()
逐个校验字段，支持自定义`clean_<字段名>`方法。
```python
from django.forms.fields import FileField
from django.core.exceptions import ValidationError

def _clean_fields(self):
    for name, field in self.fields.items():
        # 获取字段值（文件字段特殊处理）
        if field.disabled:
            value = self.get_initial_for_field(field, name)
        else:
            # 从请求数据中获取对应字段值
            value = field.widget.value_from_datadict(self.data, self.files, self.add_prefix(name))
        
        try:
            # 字段自身校验
            if isinstance(field, FileField):
                initial = self.get_initial_for_field(field, name)
                value = field.clean(value, initial)
            else:
                value = field.clean(value)
            
            # 存储校验后的值
            self.cleaned_data[name] = value
            # 执行自定义字段校验方法（如clean_name）
            if hasattr(self, 'clean_%s' % name):
                value = getattr(self, 'clean_%s' % name)()
                self.cleaned_data[name] = value
        except ValidationError as e:
            # 记录错误信息
            self.add_error(name, e)
```
**核心流程**：
1. 从请求数据中提取字段值
2. 调用字段自身的`clean()`方法校验
3. 执行自定义`clean_<字段名>`方法（如有）
4. 捕获校验异常，记录错误信息

### 7. 字段自身校验：Field.clean()
所有字段的基类校验方法，包含类型转换、空值校验和验证器执行。
```python
def clean(self, value):
    # 1. 类型转换（转为Python原生类型）
    value = self.to_python(value)
    # 2. 空值校验（required=True时不能为空）
    self.validate(value)
    # 3. 执行所有验证器（内置+自定义）
    self.run_validators(value)
    return value

def run_validators(self, value):
    if value in self.empty_values:
        return
    
    errors = []
    # 执行所有验证器
    for v in self.validators:
        try:
            v(value)  # 验证器需实现__call__方法
        except ValidationError as e:
            # 替换为自定义错误信息（如有）
            if hasattr(e, 'code') and e.code in self.error_messages:
                e.message = self.error_messages[e.code]
            errors.extend(e.error_list)
    
    if errors:
        raise ValidationError(errors)
```

### 8. 表单级校验：_clean_form()
支持自定义`clean()`方法进行跨字段校验。
```python
from django.core.exceptions import ValidationError

def _clean_form(self):
    try:
        # 执行自定义表单校验方法（clean）
        cleaned_data = self.clean()
    except ValidationError as e:
        self.add_error(None, e)
    else:
        if cleaned_data is not None:
            self.cleaned_data = cleaned_data
```

### 9. 错误添加：add_error()
统一处理错误信息存储，支持字段错误和全局错误。
```python
from django.core.exceptions import ValidationError
NON_FIELD_ERRORS = '__all__'

def add_error(self, field, error):
    # 标准化错误对象
    if not isinstance(error, ValidationError):
        error = ValidationError(error)
    
    # 处理多字段错误
    if hasattr(error, 'error_dict'):
        if field is not None:
            raise TypeError("多字段错误时field必须为None")
        error = error.error_dict
    else:
        error = {field or NON_FIELD_ERRORS: error.error_list}
    
    # 存储错误信息
    for field, error_list in error.items():
        if field not in self.errors:
            # 验证字段有效性
            if field != NON_FIELD_ERRORS and field not in self.fields:
                raise ValueError(f"'{self.__class__.__name__}' 没有字段 '{field}'")
            self._errors[field] = self.error_class(error_class='nonfield' if field == NON_FIELD_ERRORS else '')
        
        self._errors[field].extend(error_list)
        # 移除错误字段的干净数据
        if field in self.cleaned_data:
            del self.cleaned_data[field]
```

### 10. 异常类：ValidationError
统一的校验异常类，支持单字段、多字段和全局错误。
```python
class ValidationError(Exception):
    def __init__(self, message, code=None, params=None):
        super().__init__(message, code, params)
        
        # 处理嵌套异常
        if isinstance(message, ValidationError):
            if hasattr(message, 'error_dict'):
                message = message.error_dict
            elif not hasattr(message, 'message'):
                message = message.error_list
            else:
                message, code, params = message.message, message.code, message.params
        
        # 处理字典类型错误（多字段）
        if isinstance(message, dict):
            self.error_dict = {}
            for field, messages in message.items():
                if not isinstance(messages, ValidationError):
                    messages = ValidationError(messages)
                self.error_dict[field] = messages.error_list
        # 处理列表类型错误（多错误信息）
        elif isinstance(message, list):
            self.error_list = []
            for msg in message:
                if not isinstance(msg, ValidationError):
                    msg = ValidationError(msg)
                if hasattr(msg, 'error_dict'):
                    self.error_list.extend(sum(msg.error_dict.values(), []))
                else:
                    self.error_list.extend(msg.error_list)
        # 单错误信息
        else:
            self.message = message
            self.code = code
            self.params = params
            self.error_list = [self]
```

## 三、核心总结
### 1. 校验顺序
1. 元类提取字段 → 2. 实例绑定数据 → 3. 字段级校验（`_clean_fields`）→ 4. 表单级校验（`_clean_form`）→ 5. 存储结果（`cleaned_data`/`errors`）

### 2. 关键特性
- **字段继承**：通过元类合并父类字段，支持表单类复用
- **自定义校验**：支持`clean_<字段名>`（字段级）和`clean`（表单级）方法
- **错误处理**：统一使用`ValidationError`异常，错误信息分类存储
- **数据隔离**：通过深拷贝字段集合，避免多实例相互影响

### 3. 常用扩展点
- 自定义字段：继承`Field`类，重写`to_python`/`validate`/`run_validators`
- 自定义验证器：实现`__call__`方法的类或函数，通过`validators`参数传入
- 自定义错误信息：通过`error_messages`参数配置字段级错误提示