#!/bin/bash
#env:jbr-17.0.6 Android Studio Flamingo | 2022.2.1 Patch 2
cd web && npm run build && npm run install
cd ..

adb disconnect 192.168.0.100
adb connect 192.168.0.100
#./gradlew assambleDebug
./gradlew installDebug
#adb shell am start -n "com.usbtv.demo/com.usbtv.demo.MainActivity" -a android.intent.action.MAIN -c android.intent.category.LAUNCHER -D
adb shell am start -n "com.usbtv.demo/com.usbtv.demo.MainActivity"
#adb shell am start -n com.demo.hmi.xxxservices.xxx/.MainActivity


adb disconnect 192.168.0.100
