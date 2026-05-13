# 构建与开发规范

## 核心原则

### 1. 修改接口/基类时，搜索所有实现类
- **规则**：修改接口签名、抽象方法、基类时，必须先用 grep 搜索所有继承/实现该类的地方
- **工具**：`grep -r "extends\|implements" --include="*.java"`

### 2. 修改 workflow 后必须验证制品存在
- **规则**：任何修改 workflow 后，构建完成必须检查制品是否存在
- **检查**：
  1. `gh run view <id> --log` 中确认 `BUILD SUCCESSFUL`
  2. 确认制品数量符合预期
  3. 验证制品可以正常下载

### 3. 不要猜测路径和配置
- **规则**：遇到路径、配置问题时，先查看实际成功构建的日志
- **工具**：`gh run view <id> --log` 查看实际路径和配置

---

## 构建常见问题

### 1. Keystore 路径问题
```yaml
# 正确：解密到 app 目录
echo "${{ secrets.KEYSTORE_BASE64 }}" | base64 --decode > app/release.jks
```

### 2. 必须构建 Release 版本
- Debug 版本使用自动 debug.keystore，不是正式签名
- workflow 中：`./gradlew assembleMobileArm64_v8aRelease`

### 3. 制品路径
- Debug: `app/build/outputs/apk/mobileArm64_v8a/debug/*.apk`
- Release: `app/build/outputs/apk/mobileArm64_v8a/release/*.apk`

### 4. versionName 访问（AGP 9.x）
```groovy
// 错误：variant.versionName 是 Provider
def ver = variant.versionName.get()  // 可能报错

// 正确：用 project.version
def ver = project.version
```

---

## 版本号规则

- **GitHub Actions 构建编号 = versionName**
- 每次推送前递增版本号
- 例如：构建 #201 → versionName "5.4.201"

---

## 验证清单

构建完成后检查：
- [ ] `gh run view <id> --log` 中 `BUILD SUCCESSFUL`
- [ ] 制品数量 = 预期数量
- [ ] 制品可以正常下载
- [ ] 签名使用正确（Release 构建）
- [ ] APK 路径正确

---

## workflow 关键配置

```yaml
- name: Create local.properties
  run: |
    echo "sdk.dir=/usr/local/lib/android/sdk" > local.properties
    echo "storeFile=release.jks" >> local.properties
    echo "keyAlias=${{ secrets.KEY_ALIAS }}" >> local.properties
    echo "keyPassword=${{ secrets.KEY_PASSWORD }}" >> local.properties
    echo "keystorePassword=${{ secrets.KEYSTORE_PASSWORD }}" >> local.properties

- name: Decode Keystore
  run: |
    echo "${{ secrets.KEYSTORE_BASE64 }}" | base64 --decode > app/release.jks

- name: Build Release APK
  run: |
    ./gradlew assembleMobileArm64_v8aRelease --no-daemon

- name: Upload APK
  if: success()
  uses: actions/upload-artifact@v4
  with:
    name: mobile-arm64-v8a
    path: app/build/outputs/apk/mobileArm64_v8a/release/*.apk
```

---

## 历史构建问题

| 问题 | 原因 | 解决 |
|------|------|------|
| 接口签名修改遗漏 | 只改了一个实现类 | 搜索所有实现类同步修改 |
| 路径错误 | 盲目猜测路径 | 参考成功构建日志确认路径 |
| variant.versionName 报错 | AGP 9.x 访问方式 | 用 project.version |
| 签名不匹配 | Debug 构建用自动签名 | 改用 Release 构建 |
| keystore not found | 路径不在 app 目录 | 解密到 app/release.jks |