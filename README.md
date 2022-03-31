# SlideBar
仿微信联系人侧边栏，支持滑动选择和点击选择，带选中提示框

### Add SlideBar to your project

### Step1
Add it in your root build.gradle at the end of repositories:
```groovy
allprojects {
    repositories {
      ...
      maven { url 'https://jitpack.io' }
    }
}
```

### Step2
```groovy
implementation 'com.github.JeffrayZ:SlideBar:1.0.3'
```
### Step3
```xml
<com.jeffray.slidebar.SlideBar
        android:id="@+id/slide_bar"
        android:layout_width="80dp"
        android:layout_height="0dp"
        app:item_normal_text_color="@android:color/black"
        app:item_normal_text_size="16sp"
        app:item_touched_text_color="@android:color/holo_blue_bright"
        app:item_touched_text_size="24sp"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintRight_toRightOf="parent"
        app:layout_constraintTop_toTopOf="parent"
        app:tip_height="32dp"
        app:tip_icon="@mipmap/login_country_initials"
        app:tip_text_color="@android:color/white"
        app:tip_text_size="24sp"
        app:tip_width="32dp" />
```
![Dingtalk_20220324115444](https://user-images.githubusercontent.com/15990982/159839029-f660a091-a222-4b3d-bab2-d595e4d461ee.jpg)
