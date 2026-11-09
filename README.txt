【TV安装测试项目】使用说明

目的：只验证 TCL 电视能不能安装"换了包名和签名的自制APK"。
这个App没有任何功能，只显示"安装测试成功"和电视的型号信息。

一、注册并上传（只需做一次）
1. 打开 github.com 注册账号并登录。
2. 点右上角 "+" → "New repository"，名字随便起（如 tv-test），选 Public，点 "Create repository"。
3. 先把本压缩包解压到电脑。
4. 在新仓库页面点 "uploading an existing file"，把解压后文件夹里的【所有内容】
   （包括 .github 文件夹、app 文件夹、各 .gradle 文件）拖进浏览器，点 "Commit changes"。

   如果上传后仓库里看不到 .github 文件夹（有些系统会隐藏它）：
   点 "Add file" → "Create new file"，文件名栏输入：
       .github/workflows/build.yml
   然后把 "build.yml.备用" 文件里的内容整个复制进去，点 "Commit changes"。

二、云端打包
1. 点仓库顶部的 "Actions" 标签，第一次可能要点绿色按钮启用。
2. 左边点 "Build APK"，右边点 "Run workflow" → "Run workflow"。
3. 等 3~8 分钟，出现绿色对勾即成功。点进这次运行记录，页面底部 "Artifacts" 里点 "apk" 下载。
4. 解压下载的 zip，里面有两个文件：
     app-debug.apk    （调试版）
     app-release.apk  （发布版）

三、装到电视
1. 把两个 APK 拷到 U 盘。
2. 在电视文件管理器里先装 app-release.apk，再试 app-debug.apk。
3. 把每个文件的结果告诉我：装上了，还是提示什么（请原样抄下提示文字）。

如果打包失败（出现红叉）：点进失败的运行记录，把红色报错文字复制给我。

【补充：如果 Actions 里出现 "缺少 gradlew" 的红叉】
1. 看一下失败的那条运行记录的名字。只有名字是 "Build APK" 的才是本项目的打包任务。
2. 如果失败的是别的名字（比如 "Android CI"，那是 GitHub 自动推荐的模板），不用管它，
   也可以进 .github/workflows 文件夹把它删掉，避免每次上传都跑一遍。
3. 只运行 "Build APK"（左边点它 → Run workflow）。
