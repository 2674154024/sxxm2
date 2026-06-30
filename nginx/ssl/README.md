# SSL 证书配置

## 开发环境 - 自签名证书

使用 OpenSSL 生成自签名证书：

```bash
openssl req -x509 -newkey rsa:4096 -nodes -keyout server.key -out server.crt -days 365 \
  -subj "/C=CN/ST=Hunan/L=Changsha/O=Parttime Platform/OU=IT/CN=*.parttime-cs.com"
```

## 生产环境

请使用正规 CA 签发的证书（如 Let's Encrypt、阿里云 SSL 证书等）。

将以下文件放置在此目录：
- `server.crt` - 证书文件
- `server.key` - 私钥文件

## 证书格式要求

- 证书格式：PEM
- 私钥格式：PEM（无密码保护）
- 密钥长度：至少 2048 位（推荐 4096 位）