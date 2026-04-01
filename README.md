# EasyID Java SDK

Official Java SDK for the EasyID identity verification API.

EasyID 易验云 focuses on identity verification and security risk control APIs, including real-name verification, liveness detection, face recognition, phone verification, and fraud-risk related capabilities.

中文文档： [README.zh-CN.md](README.zh-CN.md)

## Install

```xml
<dependency>
  <groupId>com.easyid</groupId>
  <artifactId>easyid-java</artifactId>
  <version>1.0.0</version>
</dependency>
```

## Quick Start

```java
EasyIDClient client = EasyIDClient.builder()
    .keyId("ak_xxx")
    .secret("sk_xxx")
    .build();

IDCardService.Verify2Result result = client.idCard().verify2("张三", "110101199001011234", null);
System.out.println(result.match());
```

## Supported APIs

- IDCard: `verify2`, `verify3`, `ocr`
- Phone: `status`, `verify3`
- Face: `liveness`, `compare`, `verify`
- Bank: `verify4`
- Risk: `score`, `storeFingerprint`
- Billing: `balance`, `records`

## Configuration

- `baseUrl(...)`
- `timeout(...)`
- `httpClient(...)`

## Error Handling

Service-side business errors throw `APIError`.

```java
try {
    client.phone().status("13800138000");
} catch (APIError error) {
    System.out.println(error.getCode());
}
```

## Security Notice

This is a server-side SDK. Do not expose `secret` in browsers or mobile apps.

## Official Resources

- Official website: `https://www.easyid.com.cn/`
- GitHub organization: `https://github.com/easyid-com-cn/`
