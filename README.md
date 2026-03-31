# EasyID Java SDK

Official Java SDK for the EasyID identity verification API.

## Install

```xml
<dependency>
  <groupId>com.easyid</groupId>
  <artifactId>easyid-java</artifactId>
  <version>1.0.0</version>
</dependency>
```

## Usage

```java
EasyIDClient client = EasyIDClient.builder()
    .keyId("ak_xxx")
    .secret("sk_xxx")
    .build();

IDCardService.Verify2Result result = client.idCard().verify2("张三", "110101199001011234", null);
System.out.println(result.match());
```

## Notes

- This is a server-side SDK. Do not expose `secret` in browsers or mobile apps.
- Use `baseUrl(...)` for private deployments.
- See `../docs/integration-guide.md` for end-to-end integration and troubleshooting.
