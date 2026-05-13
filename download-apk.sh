#!/bin/bash
# 下载最新的 fongmi-TV APK 构建制品
# 用法: ./download-apk.sh

REPO="taurusqh/fongmi-TV"
ARTIFACT_NAME="mobile-arm64-v8a"
DOWNLOAD_DIR="./downloads"

mkdir -p "$DOWNLOAD_DIR"

echo "正在查找最新构建..."

ARTIFACT_ID=$(gh api repos/$REPO/actions/artifacts --paginate --jq ".artifacts[] | select(.name == \"$ARTIFACT_NAME\") | .id" | head -1)

if [ -z "$ARTIFACT_ID" ]; then
    echo "错误: 未找到 artifact: $ARTIFACT_NAME"
    exit 1
fi

echo "下载 artifact ID: $ARTIFACT_ID"

curl -sL -H "Accept: application/vnd.github+json" -H "Authorization: Bearer $(gh auth token)" \
  "https://api.github.com/repos/$REPO/actions/artifacts/${ARTIFACT_ID}/zip" \
  -o "$DOWNLOAD_DIR/$ARTIFACT_NAME.apk"

if [ -f "$DOWNLOAD_DIR/$ARTIFACT_NAME.apk" ]; then
    SIZE=$(ls -lh "$DOWNLOAD_DIR/$ARTIFACT_NAME.apk" | awk '{print $5}')
    echo "下载完成: $DOWNLOAD_DIR/$ARTIFACT_NAME.apk ($SIZE)"
else
    echo "下载失败"
    exit 1
fi