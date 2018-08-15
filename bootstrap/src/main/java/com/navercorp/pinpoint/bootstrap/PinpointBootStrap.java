/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.bootstrap;

import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;

import com.navercorp.pinpoint.ProductInfo;
import com.navercorp.pinpoint.bootstrap.agentdir.AgentDirBaseClassPathResolver;
import com.navercorp.pinpoint.bootstrap.agentdir.AgentDirectory;
import com.navercorp.pinpoint.bootstrap.agentdir.BootDir;
import com.navercorp.pinpoint.bootstrap.agentdir.ClassPathResolver;
import com.navercorp.pinpoint.bootstrap.agentdir.JavaAgentPathResolver;
import com.navercorp.pinpoint.common.util.CodeSourceUtils;

/**
 * Agent的启动类，入口与方法为premain
 * @author emeroad
 * @author netspider
 * @author dean
 */
public class PinpointBootStrap {

    //日志记录
    private static final BootLogger logger = BootLogger.getLogger(PinpointBootStrap.class.getName());

    // 加载状态标记
    private static final LoadState STATE = new LoadState();


    //instrument 规定的方法  agent的入口
    public static void premain(String agentArgs, Instrumentation instrumentation) {
        if (agentArgs == null) {
            agentArgs = "";
        }
        logger.info(ProductInfo.NAME + " agentArgs:" + agentArgs);
        logger.info("classLoader:" + PinpointBootStrap.class.getClassLoader());
        logger.info("contextClassLoader:" + Thread.currentThread().getContextClassLoader());
        //Object的是所有类的父类，按照双亲委派机制，判断PinpointBootStrap是否是和Object为同一加载器
        //如果不是 提示错误 并结束。
        if (Object.class.getClassLoader() != PinpointBootStrap.class.getClassLoader()) {
            //CodeSourceUtils工具类，具体详见工具类内部，在这里主要用户获取类的路径地址
            final URL location = CodeSourceUtils.getCodeLocation(PinpointBootStrap.class);
            logger.warn("Invalid pinpoint-bootstrap.jar:" + location);
            return;
        }

        //标识启动成功
        final boolean success = STATE.start();
        if (!success) {
            logger.warn("pinpoint-bootstrap already started. skipping agent loading.");
            return;
        }

        //参数转换  从String->map
        Map<String, String> agentArgsMap = argsToMap(agentArgs);
        //创建新的agent解析器
        JavaAgentPathResolver javaAgentPathResolver = JavaAgentPathResolver.newJavaAgentPathResolver();
        //处理agent的path
        String agentPath = javaAgentPathResolver.resolveJavaAgentPath();
        logger.info("JavaAgentPath:" + agentPath);
        //创建基本的classpath解析器
        final ClassPathResolver classPathResolver = new AgentDirBaseClassPathResolver(agentPath);
        //获取对classpath的处理聚合结果
        final AgentDirectory agentDirectory = resolveAgentDir(classPathResolver);
        //处理失败记录日志，同时记录加载失败
        if (agentDirectory == null) {
            logger.warn("Agent Directory Verify fail. skipping agent loading.");
            //记录加载失败
            logPinpointAgentLoadFail();
            return;
        }
        //从聚合结果中取出Bootdir
        BootDir bootDir = agentDirectory.getBootDir();
        //追加到BootStrapClassLoader中
        appendToBootstrapClassLoader(instrumentation, bootDir);
        //获取父加载器
        ClassLoader parentClassLoader = getParentClassLoader();
        //模块引导加载器
        final ModuleBootLoader moduleBootLoader = loadModuleBootLoader(instrumentation, parentClassLoader);
        //将准备好的内容传递给PinpointStarter，并启动
        PinpointStarter bootStrap = new PinpointStarter(parentClassLoader, agentArgsMap, agentDirectory, instrumentation, moduleBootLoader);
        //如果启动失败，记录日志
        if (!bootStrap.start()) {
            logPinpointAgentLoadFail();
        }

    }
    //加载模块加载器
    private static ModuleBootLoader loadModuleBootLoader(Instrumentation instrumentation, ClassLoader parentClassLoader) {
        //判断是否支持模块
        if (!ModuleUtils.isModuleSupported()) {
            return null;
        }

        //记录  发现java9的模块化支持
        logger.info("java9 module detected");
        logger.info("ModuleBootLoader start");
        //新建模块加载器
        ModuleBootLoader moduleBootLoader = new ModuleBootLoader(instrumentation, parentClassLoader);
        //加载支持的模块
        moduleBootLoader.loadModuleSupport();
        return moduleBootLoader;
    }

    //获取路径处理聚合结果
    private static AgentDirectory resolveAgentDir(ClassPathResolver classPathResolver) {
        try {
            AgentDirectory agentDir = classPathResolver.resolve();
            return agentDir;
        } catch(Exception e) {
            logger.warn("AgentDir resolve fail Caused by:" + e.getMessage(), e);
            return null;
        }
    }

    //获取父加载器
    private static ClassLoader getParentClassLoader() {
        final ClassLoader classLoader = getPinpointBootStrapClassLoader();
        //判断如果当前加载器与Object类的加载器一样，说明是BootStrapClassLoader
        if (classLoader == Object.class.getClassLoader()) {
            logger.info("parentClassLoader:BootStrapClassLoader:" + classLoader );
        } else {
            //否则说明不是BootStrapClassLoader
            logger.info("parentClassLoader:" + classLoader);
        }
        return classLoader;
    }

    //获取当前类加载
    private static ClassLoader getPinpointBootStrapClassLoader() {
        return PinpointBootStrap.class.getClassLoader();
    }

    //将字符串参数转换为map
    private static Map<String, String> argsToMap(String agentArgs) {
        //利用参数处理器处理参数
        ArgsParser argsParser = new ArgsParser();
        Map<String, String> agentArgsMap = argsParser.parse(agentArgs);
        //如果参数不为空，日志记录
        if (!agentArgsMap.isEmpty()) {
            logger.info("agentParameter:" + agentArgs);
        }
        return agentArgsMap;
    }

    //追加到BootstrapClassLoader
    private static void appendToBootstrapClassLoader(Instrumentation instrumentation, BootDir bootDir) {
        List<JarFile> jarFiles = bootDir.openJarFiles();
        for (JarFile jarFile : jarFiles) {
            logger.info("appendToBootstrapClassLoader:" + jarFile.getName());
            //利用instrumentation追加jar
            instrumentation.appendToBootstrapClassLoaderSearch(jarFile);
        }
    }


    //记录加载失败
    private static void logPinpointAgentLoadFail() {
        final String errorLog =
                "*****************************************************************************\n" +
                        "* Pinpoint Agent load failure\n" +
                        "*****************************************************************************";
        System.err.println(errorLog);
    }


}
