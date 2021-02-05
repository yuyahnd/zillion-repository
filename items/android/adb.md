# はじめに
Android 開発に欠かせない adbコマンドについて基本的なことをまとめます。
Android の SDK には ADB(Android Debug Bridge)というツールが用意されており、
Android 開発をしていれば一度は使う機会があります。

# adb とは
Android Debug Bridge は adb と省略されます。
これはAndroid SDK の platform-tools に含まれるツールです。
このツールを用いると、現在利用可能なデバイス・エミュレータの列挙、シェルコマンドの発行、ファイルの転送などが行えます。
Android 端末を adb コマンドで操作できます。

# 基本

## バージョンを表示
```sh:コマンド
adb version
```
```shell-session:結果
Android Debug Bridge version 1.0.41
Version 30.0.3-6597393
Installed as {インストールディレクトリ}/adb.exe
```

## 端末の確認
```sh:コマンド
adb devices
```
```shell-session:結果
List of devices attached
emulator-5554   device
```

## アプリのインストール
デバイスにインストール

```sh:コマンド
adb install xxxxx.apk
```
デバイスに上書きインストール

```sh:コマンド
adb install -r xxxxx.apk
```
複数のデバイスを接続している時は、端末を指定

```sh:コマンド
adb -s {デバイス} install -r xxxxx.apk
# 例 adb -s emulator-5554 install -r xxxxx.apk
```

## ログ

```sh:コマンド
adb logcat
```
## shell

```sh:コマンド
adb shell
```
