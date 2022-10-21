# How to make a bundle

1. In Android Studio - Build / Generate Signed Bundle/APK... and choose `Android App Bundle`.
2. Download [bundletool](https://github.com/google/bundletool)

```bash
java -jar bundletool-all-1.12.1.jar build-apks --bundle=app-release.aab --output=app-release.apks
java -jar bundletool-all-1.12.1.jar install-apks --apks=app-release.apks
```
