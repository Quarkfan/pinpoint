/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.bootstrap.agentdir;


import com.navercorp.pinpoint.bootstrap.BootLogger;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Agent路径解析器
 * @author emeroad
 */
public class AgentDirBaseClassPathResolver implements ClassPathResolver {

    private final BootLogger logger = BootLogger.getLogger(this.getClass().getName());
    //版本正则表达式
    static final String VERSION_PATTERN = "(-[0-9]+\\.[0-9]+\\.[0-9]+((\\-SNAPSHOT)|(-RC[0-9]+))?)?";
    //其他相关正则表达式解析
    static final Pattern DEFAULT_AGENT_BOOTSTRAP_PATTERN = compile("pinpoint-bootstrap" + VERSION_PATTERN + "\\.jar");
    static final Pattern DEFAULT_AGENT_COMMONS_PATTERN = compile("pinpoint-commons" + VERSION_PATTERN + "\\.jar");
    static final Pattern DEFAULT_AGENT_CORE_PATTERN = compile("pinpoint-bootstrap-core" + VERSION_PATTERN + "\\.jar");
    static final Pattern DEFAULT_AGENT_JAVA9_PATTERN = compile("pinpoint-bootstrap-java9" + VERSION_PATTERN + "\\.jar");
    static final Pattern DEFAULT_AGENT_CORE_OPTIONAL_PATTERN = compile("pinpoint-bootstrap-core-optional" + VERSION_PATTERN + "\\.jar");
    static final Pattern DEFAULT_ANNOTATIONS = compile("pinpoint-annotations" + VERSION_PATTERN + "\\.jar");
    //相关正则匹配类
    private final Pattern agentBootstrapPattern;
    private final Pattern agentCommonsPattern;
    private final Pattern agentCorePattern;
    private final Pattern agentJava9Pattern;
    private final Pattern agentCoreOptionalPattern;
    private final Pattern annotationsPattern;

    //classpath
    private final String classPath;
    //扩展名列表
    private List<String> fileExtensionList;

    //正则编译
    private static Pattern compile(String regex) {
        return Pattern.compile(regex);
    }

    //构造函数，初始化相关正则
    public AgentDirBaseClassPathResolver(String classPath) {
        if (classPath == null) {
            throw new NullPointerException("classPath must not be null");
        }
        this.classPath = classPath;
        this.agentBootstrapPattern = DEFAULT_AGENT_BOOTSTRAP_PATTERN;
        this.agentCommonsPattern = DEFAULT_AGENT_COMMONS_PATTERN;
        this.agentCorePattern = DEFAULT_AGENT_CORE_PATTERN;
        this.agentJava9Pattern = DEFAULT_AGENT_JAVA9_PATTERN;
        this.agentCoreOptionalPattern = DEFAULT_AGENT_CORE_OPTIONAL_PATTERN;
        this.annotationsPattern = DEFAULT_ANNOTATIONS;
        this.fileExtensionList = getDefaultFileExtensionList();
    }

    //获取默认扩展名列表
    static List<String> getDefaultFileExtensionList() {
        List<String> extensionList = new ArrayList<String>(3);
        extensionList.add("jar");
        extensionList.add("xml");
        extensionList.add("properties");
        return extensionList;
    }


    @Override
    //解析方法
    public AgentDirectory resolve() {

        // 查找 boot-strap.jar
        final String bootstrapJarName = this.findBootstrapJar(this.classPath);
        //查找失败抛出异常
        if (bootstrapJarName == null) {
            throw new IllegalStateException("pinpoint-bootstrap-x.x.x(-SNAPSHOT).jar not found.");
        }

        //解析agentJar的全路径
        final String agentJarFullPath = parseAgentJarPath(classPath, bootstrapJarName);
        //如果未找到，抛出异常
        if (agentJarFullPath == null) {
            throw new IllegalStateException("pinpoint-bootstrap-x.x.x(-SNAPSHOT).jar not found. " + classPath);
        }

        //查找agent的目录路径
        final String agentDirPath = getAgentDirPath(agentJarFullPath);
        //解析引导路径内的内容
        final BootDir bootDir = resolveBootDir(agentDirPath);

        //解析agent的lib目录
        final String agentLibPath = getAgentLibPath(agentDirPath);
        final List<URL> libs = resolveLib(agentLibPath, bootDir);

        //解析agent插件路径
        String agentPluginPath = getAgentPluginPath(agentDirPath);
        final List<String> plugins = resolvePlugins(agentPluginPath);

        //聚合结果
        final AgentDirectory agentDirectory = new AgentDirectory(bootstrapJarName, agentJarFullPath, agentDirPath,
                bootDir, libs, plugins);

        return agentDirectory;
    }
    //获取Agent的绝对路径
    private String getAgentDirPath(String agentJarFullPath) {
        //获取jar的路径（目录位置）
        String agentDirPath = parseAgentDirPath(agentJarFullPath);
        //如果路径不存在抛出异常
        if (agentDirPath == null) {
            throw new IllegalStateException("agentDirPath is null " + classPath);
        }

        logger.info("Agent original-path:" + agentDirPath);
        // 获取绝对路径
        agentDirPath = toCanonicalPath(agentDirPath);
        logger.info("Agent canonical-path:" + agentDirPath);
        return agentDirPath;
    }

    //查找相关jar的位置
    private BootDir resolveBootDir(String agentDirPath) {
        String bootDirPath = agentDirPath + File.separator + "boot";
        //查找目录下相关jar的绝对路径
        String pinpointCommonsJar = find(bootDirPath, "pinpoint-commons.jar", agentCommonsPattern);
        String bootStrapCoreJar = find(bootDirPath, "pinpoint-bootstrap-core.jar", agentCorePattern);
        String bootStrapJava9Jar = find(bootDirPath, "pinpoint-bootstrap-java9.jar", agentJava9Pattern);
        String bootStrapCoreOptionalJar = find(bootDirPath, "pinpoint-bootstrap-core-optional.jar", agentCoreOptionalPattern);
        String annotationsJar = find(bootDirPath,"pinpoint-annotations.jar", annotationsPattern);
        //构建BootDir对象存储绝对路径
        return new BootDir(pinpointCommonsJar, bootStrapCoreJar, bootStrapCoreOptionalJar, bootStrapJava9Jar, annotationsJar);
    }

    //查找Bootstrap.Jar
    String findBootstrapJar(String classPath) {
        //解析出匹配结果
        final Matcher matcher = agentBootstrapPattern.matcher(classPath);
        //匹配失败返回null
        if (!matcher.find()) {
            return null;
        }
        //成功返回具体内容
        return parseAgentJar(matcher, classPath);
    }

    //转换为标准路径（绝对路径）
    private String toCanonicalPath(String path) {
        final File file = new File(path);
        return toCanonicalPath(file);
    }
    //转换为标准路径（绝对路径）
    private String toCanonicalPath(File file) {
        try {
            return file.getCanonicalPath();
        } catch (IOException e) {
            logger.warn(file.getPath() + " getCanonicalPath() error. Error:" + e.getMessage(), e);
            return file.getAbsolutePath();
        }
    }
    //查找目录内某个符合正则的文件绝对路径
    private String find(String bootDirPath, final String name, final Pattern pattern) {
        final File[] files = listFiles(name, pattern, bootDirPath);
        if (isEmpty(files)) {
            //文件没找到
            logger.info(name + " not found.");
            return null;
        } else if (files.length == 1) {
            //只有一个，返回局对路径
            File file = files[0];
            return toCanonicalPath(file);
        } else {
            //返回多个记录日志不返回。
            logger.info("too many " + name + " found. " + Arrays.toString(files));
            return null;
        }
    }
    //判断文件列表是否为空
    private boolean isEmpty(File[] files) {
        return files == null || files.length == 0;
    }

    //列出目录内符合正则的查找文件
    private File[] listFiles(final String name, final Pattern pattern, String bootDirPath) {
        File bootDir = new File(bootDirPath);
        return bootDir.listFiles(new FilenameFilter() {
            @Override
            //实现FilenameFilter接口，文件过滤器
            public boolean accept(File dir, String fileName) {
                Matcher matcher = pattern.matcher(fileName);
                if (matcher.matches()) {

                    logger.info("found " + name + ". " + dir.getAbsolutePath() + File.separator + fileName);
                    return true;
                }
                return false;
            }
        });
    }

    //截取agentJar
    private String parseAgentJar(Matcher matcher, String classpath) {
        int start = matcher.start();
        int end = matcher.end();
        return classPath.substring(start, end);
    }

    //解析agentJar的全路径
    private String parseAgentJarPath(String classPath, String agentJar) {
        //按照路径分割切分出classpath地址列表
        String[] classPathList = classPath.split(File.pathSeparator);
        //遍历查找是否有路径含有所需的jar包，一旦查找到就返回
        for (String findPath : classPathList) {
            boolean find = findPath.contains(agentJar);
            if (find) {
                return findPath;
            }
        }
        return null;
    }

    //拼接lib地址
    private String getAgentLibPath(String agentDirPath) {
        return agentDirPath + File.separator + "lib";
    }
    //拼接plugin地址
    private String getAgentPluginPath(String agentDirPath) {
        return agentDirPath + File.separator + "plugin";
    }

    //处理lib目录，bootdir用于处理没找到引导jar时，在此处添加进入一起处理，暂时不需要
    private List<URL> resolveLib(String agentLibPath, BootDir bootDir) {
        File libDir = new File(agentLibPath);
        //如果目录不存在或者不是一个目录，返回空集合
        if (checkDirectory(libDir)) {
            return Collections.emptyList();
        }
        final List<URL> jarURLList = new ArrayList<URL>();
        //在目录中查找jar
        final File[] findJarList = findJar(libDir);

        if (findJarList != null) {
            //jarlist不为空，循环迭代转换为url，然后放入jarurllist
            for (File file : findJarList) {
                URL url = toURI(file);
                if (url != null) {
                    jarURLList.add(url);
                }
            }
        }
        //将lib目录转化为URL后，也放入到jarURLList
        URL agentDirUri = toURI(new File(agentLibPath));
        if (agentDirUri != null) {
            jarURLList.add(agentDirUri);
        }

        // hot fix. boot jars not found from classPool ??
//        jarURLList.add(toURI(new File(bootDir.getCommons())));
//        jarURLList.add(toURI(new File(bootDir.getBootstrapCore())));
//        String bootstrapCoreOptionalJar = bootDir.getBootstrapCoreOptional();
//        // bootstrap-core-optional jar is not required and is okay to be null
//        if (bootstrapCoreOptionalJar != null) {
//            jarURLList.add(toURI(new File(bootstrapCoreOptionalJar)));
//        }
        return jarURLList;
    }

    //处理插件路径
    private List<String> resolvePlugins(String agentPluginPath) {
        final File directory = new File(agentPluginPath);

        //目录校验，如果不存在或者不是一个目录返回空集合
        if (checkDirectory(directory)) {
            logger.warn(directory + " is not a directory");
            return Collections.emptyList();
        }

        //插件路径内只接受.jar结尾的文件
        final File[] jars = directory.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                  return name.endsWith(".jar");
            }
        });

        //如果插件列表为空，返回空集合
        if (isEmpty(jars)) {
            return Collections.emptyList();
        }

        //过滤出所有可读插件
        List<String> pluginFileList = filterReadPermission(jars);
        //输出日志
        for (String pluginJar : pluginFileList) {
            logger.info("Found plugins:" + pluginJar);
        }
        return pluginFileList;
    }

    //目录校验
    private boolean checkDirectory(File file) {
        //如果目录不存在 返回true
        if (!file.exists()) {
            logger.warn(file + " not found");
            return true;
        }
        //如果不是一个目录返回true
        if (!file.isDirectory()) {
            logger.warn(file + " is not a directory");
            return true;
        }
        return false;
    }

    //过滤所有有可读权限的jar
    private List<String> filterReadPermission(File[] jars) {
        List<String> result = new ArrayList<String>();
        for (File pluginJar : jars) {
            //循环迭代，不可读的略过
            if (!pluginJar.canRead()) {
                logger.info("File '" + pluginJar + "' cannot be read");
                continue;
            }

            result.add(pluginJar.getPath());
        }
        return result;
    }

    //将给定文件转换为URL
    private URL toURI(File file) {
        URI uri = file.toURI();
        try {
            return uri.toURL();
        } catch (MalformedURLException e) {
            logger.warn(file.getName() + ".toURL() failed.", e);
            return null;
        }
    }

    //在给定目录中查找jar
    private File[] findJar(File libDir) {
        return libDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                String path = pathname.getName();
                //判断是否在给定的扩展名列表中
                for (String extension : fileExtensionList) {
                    if (path.lastIndexOf("." + extension) != -1) {
                        return true;
                    }
                }
                return false;
            }
        });
    }

    //截取从0开始到最后一个路径斜杠的目录
    private String parseAgentDirPath(String agentJarFullPath) {
        int index1 = agentJarFullPath.lastIndexOf("/");
        int index2 = agentJarFullPath.lastIndexOf("\\");
        int max = Math.max(index1, index2);
        if (max == -1) {
            return null;
        }
        return agentJarFullPath.substring(0, max);
    }

}
