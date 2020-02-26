# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

###################混淆的基本规则###################
#参考博客 https://blog.csdn.net/guolin_blog/article/details/50451259

#一、基本规则

#一个*表示只是保持该包下的类名，而子包下的类名还是会被混淆；两个*表示把本包和所含子包下的类名都保持；
#-keep class cn.example.test.**
#-keep class cn.example.test.*

#用以上方法保持类后，你会发现类名虽然未混淆，但里面的具体方法和变量命名还是变了，这时如果既想保持类名，又想保持里面的内容不被混淆，需要以下方法
#-keep class com.example.bean.** { *; }


#二、使用Java的规则

#可以用extend，implement等这些Java规则
# 保留四大组件，自定义的Application等等这些类不被混淆
# 因为这些子类都有可能被外部调用
#-keep public class * extends android.app.Activity
#-keep public class * extends android.app.Appliction
#-keep public class * extends android.app.Service
#-keep public class * extends android.content.BroadcastReceiver
#-keep public class * extends android.content.ContentProvider
#-keep public class * extends android.app.backup.BackupAgentHelper
#-keep public class * extends android.preference.Preference
#-keep public class * extends android.view.View
#-keep public class com.android.vending.licensing.ILicensingService

#三、哪些类不能被混淆
#一般以下情况都会不混淆：
#1.使用了自定义控件那么要保证它们不参与混淆
#2.使用了枚举要保证枚举不被混淆
#3.对第三方库中的类不进行混淆
#4.运用了反射的类也不进行混淆
#5.使用了 Gson 之类的工具要使 JavaBean 类即实体类不被混淆
#6.在引用第三方库的时候，一般会标明库的混淆规则的，建议在使用的时候就把混淆规则添加上去，免得到最后才去找
#7.有用到 WebView 的 JS 调用也需要保证写的接口方法不混淆，原因和第一条一样
#8.Parcelable 的子类和 Creator 静态成员变量不混淆，否则会产生 Android.os.BadParcelableException 异常


#keep	                        保留类和类中的成员，防止它们被混淆或移除。
#keepnames	                    保留类和类中的成员，防止它们被混淆，但当成员没有被引用时会被移除。
#keepclassmembers	            只保留类中的成员，防止它们被混淆或移除。
#keepclassmembernames	        只保留类中的成员，防止它们被混淆，但当成员没有被引用时会被移除。
#keepclasseswithmembers	        保留类和类中的成员，防止它们被混淆或移除，前提是指名的类中的成员必须存在，如果不存在则还是会混淆。
#keepclasseswithmembernames	    保留类和类中的成员，防止它们被混淆，但当成员没有被引用时会被移除，前提是指名的类中的成员必须存在，如果不存在则还是会混淆。

#通配符
#<field>	匹配类中的所有字段
#<method>	匹配类中的所有方法
#<init>	    匹配类中的所有构造函数
#*	        匹配任意长度字符，但不含包名分隔符(.)。比如说我们的完整类名是com.example.test.MyActivity，使用com.*，或者com.exmaple.*都是无法匹配的，因为*无法匹配包名中的分隔符，正确的匹配方式是com.exmaple.*.*，或者com.exmaple.test.*，这些都是可以的。但如果你不写任何其它内容，只有一个*，那就表示匹配所有的东西。
#**	        匹配任意长度字符，并且包含包名分隔符(.)。比如proguard-android.txt中使用的-dontwarn android.support.**就可以匹配android.support包下的所有内容，包括任意长度的子包。
#***	    匹配任意参数类型。比如void set*(***)就能匹配任意传入的参数类型，*** get*()就能匹配任意返回值的类型。
#…	        匹配任意长度的任意类型参数。比如void test(…)就能匹配任意void test(String a)或者是void test(int a, String b)这些方法。

###################以上是 混淆的基本规则###################




##################################################################################
##################     自定义混淆部分      #########################################
##################################################################################
#####1.实体类

-keep class com.infinite.downloader.XDownload{
public *;  #类的所有public方法和字段不混淆
}

-keep class com.infinite.downloader.utils.DLogger{
public *;  #类的所有public方法和字段不混淆
}
-keep class com.infinite.downloader.DownloadListener {
 <init>(...);
 <fields>;
 <methods>;
 #类的构造方法，字段，其他方法均不混淆
}
-keep class com.infinite.downloader.task.DownloadTask{
public *; #类的所有public方法和字段不混淆
}
-keep class com.infinite.downloader.config.** {*;}

-keep public class * extends com.infinite.downloader.lru.DiskUsage

#####2.第三方库，包括jar包和dependencies方式引入的库




#####3.js和webview的调用模块

-keepattributes *JavascriptInterface*
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
#-keep public class com.xxx.xxx.xxxActivity$MyWebViewHandler {
# #class后是完整的类名，$符后面是内部类的名称，下面是内部类中的两个方法声明
#    public void showBody (java.lang.String);
#    public void showHeder (java.lang.String);
#}




#####4.反射相关的类和方法










##########################################################################################
######################                                                #####################
######################  这是分隔墙！！！    以下是公共混淆内容，请慎重修改   #####################
#####################                                                 #####################
##########################################################################################

####################一些基本的混淆指令#########################
-ignorewarnings   # 抑制警告，不建议使用
#关闭压缩，默认开启，用以减小应用体积，移除未被使用的类和成员，
#并且会在优化动作执行之后再次执行（因为优化后可能会再次暴露一些未被使用的类和成员）
-dontshrink

#表示不进行优化，建议使用此选项，因为根据proguard-android-optimize.txt中的描述，
#优化可能会造成一些潜在风险，不能保证在所有版本的Dalvik上都正常运行
-dontoptimize

-optimizationpasses 5  # 代码混淆压缩比，在0~7之间，默认为5，一般不做修改

-dontusemixedcaseclassnames # 混合时不使用大小写混合，混合后的类名为小写

-dontskipnonpubliclibraryclasses # 指定不去忽略非公共库的类

-dontskipnonpubliclibraryclassmembers  # 指定不去忽略非公共库的类成员

-verbose # 这句话能够使项目混淆后产生映射文件，包含有类名->混淆后类名的映射关系

# 不做预校验，preverify是proguard的四个步骤之一，Android不需要preverify，去掉这一步能够加快混淆速度
-dontpreverify

-keepattributes *Annotation*,InnerClasses  # 保留Annotation不混淆

-keepattributes Signature  # 避免混淆泛型

-keepattributes SourceFile,LineNumberTable # 抛出异常时保留代码行号

-keepattributes Exceptions,Deprecated,LocalVariable*Table,Synthetic,EnclosingMethod

# 指定混淆是采用的算法，后面的参数是一个过滤器， 这个过滤器是谷歌推荐的算法，一般不做更改
-optimizations !code/simplification/cast,!field/*,!class/merging/*
-repackageclasses com.infinite.downloader.keepsafe

###########################混淆日志###########################
-dump class_files.txt        #apk所有class的内部结构
-printseeds seeds.txt           #未混淆的class和成员变量
-printusage unused.txt          #打印未被使用的代码
-printmapping mapping.txt       #混淆前后的映射
###########################混淆日志###########################

####################以上是 一些基本的混淆指令#########################


##################保留的公共部分，无特殊情况请勿更改！！！###########################

# 保留四大组件，自定义的Application等等这些类不被混淆
# 因为这些子类都有可能被外部调用
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Appliction
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class * extends android.view.View
-keep public class com.android.vending.licensing.ILicensingService

# 保留support下的所有类及其内部类
-keep class android.support.** {*;}
-dontwarn android.support.**

# 保留继承的
-keep public class * extends android.support.v4.**
-keep public class * extends android.support.v7.**
-keep public class * extends android.support.annotation.**

# 保留继承自AndroidX
-keep class com.google.android.material.** {*;}
-keep class androidx.** {*;}
-keep public class * extends androidx.**
-keep interface androidx.** {*;}
-dontwarn com.google.android.material.**
-dontnote com.google.android.material.**
-dontwarn androidx.**

# 保留R下面的资源
-keep class **.R$* {*;}

# 保留本地native方法不被混淆
-keepclasseswithmembernames class * {
    native <methods>;
}

# 保留在Activity中的方法参数是view的方法，这样在layout中写的onClick就不会被影响
-keepclassmembers class * extends android.app.Activity{
    public void *(android.view.View);
}

# 保留枚举类不被混淆
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

#避免混淆JSON类的构造函数
-keepclassmembers class * {
    public <init>(org.json.JSONObject);
}

# 保留自定义控件（继承自View）不被混淆
-keep public class * extends android.view.View{
    *** get*();
    void set*(***);
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# 保留Parcelable序列化类不被混淆
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# 保留Serializable序列化的类不被混淆
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    !private <fields>;
    !private <methods>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# 对于带有回调函数的onXXEvent、**On*Listener的，不能被混淆
-keepclassmembers class * {
    void *(**On*Event);
    void *(**On*Listener);
}

# webView处理，项目中没有使用到webView忽略即可
-keepclassmembers class fqcn.of.javascript.interface.for.webview {
    public *;
}
-keepclassmembers class * extends android.webkit.webViewClient {
    public void *(android.webkit.WebView, java.lang.String, android.graphics.Bitmap);
    public boolean *(android.webkit.WebView, java.lang.String);
}
-keepclassmembers class * extends android.webkit.webViewClient {
    public void *(android.webkit.webView, jav.lang.String);
}

#################以上是 保留的公共部分，无特殊情况请勿更改！！！###########################


