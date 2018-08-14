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

    //FIXME 加载状态？
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

        JavaAgentPathResolver javaAgentPathResolver = JavaAgentPathResolver.newJavaAgentPathResolver();
        String agentPath = javaAgentPathResolver.resolveJavaAgentPath();
        logger.info("JavaAgentPath:" + agentPath);
        final ClassPathResolver classPathResolver = new AgentDirBaseClassPathResolver(agentPath);

        final AgentDirectory agentDirectory = resolveAgentDir(classPathResolver);
        if (agentDirectory == null) {
            logger.warn("Agent Directory Verify fail. skipping agent loading.");
            logPinpointAgentLoadFail();
            return;
        }
        BootDir bootDir = agentDirectory.getBootDir();
        appendToBootstrapClassLoader(instrumentation, bootDir);

        ClassLoader parentClassLoader = getParentClassLoader();
        final ModuleBootLoader moduleBootLoader = loadModuleBootLoader(instrumentation, parentClassLoader);
        PinpointStarter bootStrap = new PinpointStarter(parentClassLoader, agentArgsMap, agentDirectory, instrumentation, moduleBootLoader);
        if (!bootStrap.start()) {
            logPinpointAgentLoadFail();
        }

    }

    private static ModuleBootLoader loadModuleBootLoader(Instrumentation instrumentation, ClassLoader parentClassLoader) {
        if (!ModuleUtils.isModuleSupported()) {
            return null;
        }
        logger.info("java9 module detected");
        logger.info("ModuleBootLoader start");
        ModuleBootLoader moduleBootLoader = new ModuleBootLoader(instrumentation, parentClassLoader);
        moduleBootLoader.loadModuleSupport();
        return moduleBootLoader;
    }

    private static AgentDirectory resolveAgentDir(ClassPathResolver classPathResolver) {
        try {
            AgentDirectory agentDir = classPathResolver.resolve();
            return agentDir;
        } catch(Exception e) {
            logger.warn("AgentDir resolve fail Caused by:" + e.getMessage(), e);
            return null;
        }
    }


    private static ClassLoader getParentClassLoader() {
        final ClassLoader classLoader = getPinpointBootStrapClassLoader();
        if (classLoader == Object.class.getClassLoader()) {
            logger.info("parentClassLoader:BootStrapClassLoader:" + classLoader );
        } else {
            logger.info("parentClassLoader:" + classLoader);
        }
        return classLoader;
    }

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

    private static void appendToBootstrapClassLoader(Instrumentation instrumentation, BootDir bootDir) {
        List<JarFile> jarFiles = bootDir.openJarFiles();
        for (JarFile jarFile : jarFiles) {
            logger.info("appendToBootstrapClassLoader:" + jarFile.getName());
            instrumentation.appendToBootstrapClassLoaderSearch(jarFile);
        }
    }



    private static void logPinpointAgentLoadFail() {
        final String errorLog =
                "*****************************************************************************\n" +
                        "* Pinpoint Agent load failure\n" +
                        "*****************************************************************************";
        System.err.println(errorLog);
    }


}
