# 项目说明（Project Description）

本项目用于在VS使用了回退位置作为项目的临时缓存路径时用于清理无效（项目已经删除但是缓存仍然存在）缓存。

# 原理（Principle）

当指定好了回退位置（FallBack location），假设为C://VSTemp，则每次打开一个新的项目，都会在这个文件夹先产生一个类似BROWSE.VC-df664a8e的文件夹，里面
会有一个Browse.VC.db的文件，这是个SQLite数据库，打开这个数据库（可以用Intellij IDEA打开），里面有个projects表，表里面是
跟这个缓存目录关联的项目目录，类似如下


> D:\RUBISHCODEDATA\VS\PROJECTS\CPPCONSOLEPRACTICE\CMAKELISTS.TXT;INVERSEBORLANDEXPRESSION.EXE (INVERSEBORLANDEXPRESSION\INVERSEBORLANDEXPRESSION.EXE)

> D:\RUBISHCODEDATA\VS\PROJECTS\CPPCONSOLEPRACTICE\CPPCONSOLEPRACTICE\CPPCONSOLEPRACTICE.VCXPROJ

> D:\RUBISHCODEDATA\VS\PROJECTS\CPPCONSOLEPRACTICE\CPPPROPERTIES.JSON;DEFAULT

> \\?\:WORKSPACE

所以这个项目关键就是通过如下字符串来判断项目是否存在，如果不存在，即删除此文件夹。

思路：获取表中所有字符串，获取最开头到第一个分号（如果有）之间的字符串，判断是否是一个文件路径，如果是，则将所有文件路径保存到List中，然后再判断这些文件路径代表的文件是否都存在，如果都不存在，那么代表这个缓存目录可以删除了。

配置：项目采用init.props作为配置文件，采用key=value的格式。

FallBackLocation指定缓存根目录（即回退位置）



# 手动模式（Manual Mode）

鉴于判断项目是否存在的算法也许不一定准确，特别提供手动模式供自己手动删除。

手动模式提供扫描缓存目录每个目录的projects路径信息，用于手动判断项目存在，并将这些信息写入到ManualMode.txt文件里面（json格式）


# 什么是回退位置（What is fall back location）

1. 对于VS项目，默认情况下，新建一个项目下面会有个隐藏目录.vs，这个文件通常用不了多久就变得非常大，主要是因为里面存储了大量的缓存数据，这些缓存数据可以通过进行如下设置：

选项>文本编辑器>C/C++>高级>回退位置（FallBack location）：始终使用回退位置=true，回退位置已在使用时，不警告=true，回退位置设置为你想设置的位置，然后以后vs文件夹就会很小了，缓存数据都被放入指定的文件夹中了。



# 其它（Other）

1. 本项目在VS2019下使用通过

2. 缓存文件夹可以随意删除，只不过这样下次打开就要重新生成缓存文件，缓存文件是为了加快项目运行的。

3. 本项目采用Intellij IDEA开发，用Gradle作为构建工具,JDK版本11

4. 项目采用sqlite-jdbc来读取sqlite数据库。


# 项目代码结构

