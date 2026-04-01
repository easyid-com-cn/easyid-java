# EasyID Java SDK

EasyID Java SDK 是易验云身份验证 API 的官方 Java 客户端。

English README: [README.md](README.md)

EasyID 提供身份证核验、手机号核验、人脸识别、银行卡核验、风控评分等能力。本 SDK 面向服务端 Java 应用，自动处理签名、认证头和业务错误解析。

## 安装

Maven：

```xml
<dependency>
  <groupId>com.easyid</groupId>
  <artifactId>easyid-java</artifactId>
  <version>1.0.0</version>
</dependency>
```

Gradle：

```groovy
implementation 'com.easyid:easyid-java:1.0.0'
```

要求：

- Java 17+

## 快速开始

```java
import com.easyid.EasyIDClient;
import com.easyid.IDCardService;

public class Main {
    public static void main(String[] args) {
        EasyIDClient client = EasyIDClient.builder()
            .keyId("ak_xxx")
            .secret("sk_xxx")
            .build();

        IDCardService.VerifyResult result =
            client.idCard().verify2("张三", "110101199001011234", null);

        System.out.println("是否匹配：" + result.match());
    }
}
```

## 已支持接口

- `client.idCard().verify2(...)`：身份证二要素核验
- `client.idCard().verify3(...)`：身份证三要素核验
- `client.idCard().ocr(...)`：身份证 OCR
- `client.phone().status(...)`：手机号状态查询
- `client.phone().verify3(...)`：手机号三要素核验
- `client.face().liveness(...)`：人脸活体检测
- `client.face().compare(...)`：人脸比对
- `client.face().verify(...)`：人脸核验
- `client.bank().verify4(...)`：银行卡四要素核验
- `client.risk().score(...)`：风控评分
- `client.risk().storeFingerprint(...)`：存储设备指纹
- `client.billing().balance(...)`：查询账户余额
- `client.billing().records(...)`：查询账单记录

## 配置项

- `baseUrl(...)`：自定义 API 地址
- `timeout(Duration)`：超时时间
- `httpClient(HttpClient)`：自定义 HTTP 客户端

## 错误处理

服务端业务错误会抛出 `APIError`。

```java
import com.easyid.APIError;

try {
    var result = client.phone().status("13800138000");
    System.out.println(result.status());
} catch (APIError error) {
    System.out.println(error.getCode());
    System.out.println(error.getMessage());
    System.out.println(error.getRequestId());
}
```

## 安全说明

- 这是服务端 SDK，不要在浏览器、移动端或不可信客户端暴露 `secret`
- `keyId` 必须符合 `ak_[0-9a-f]+`
- SDK 会自动按统一协议参与 query、body 和 multipart 的签名

## 官方资源

- 官网：`https://www.easyid.com.cn/`
- GitHub：`https://github.com/easyid-com-cn/`
