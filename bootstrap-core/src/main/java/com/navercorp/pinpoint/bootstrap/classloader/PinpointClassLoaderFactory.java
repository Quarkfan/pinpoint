/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.bootstrap.classloader;

import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.common.util.JvmUtils;
import com.navercorp.pinpoint.common.util.JvmVersion;

import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.List;

/**
 * 类加载器工厂
 * @author Taejin Koo
 * @author dean
 */
public final class PinpointClassLoaderFactory {

    private static final PLogger LOGGER = PLoggerFactory.getLogger(PinpointClassLoaderFactory.class);

    private static final ClassLoaderFactory CLASS_LOADER_FACTORY = createClassLoaderFactory();

    // Jdk 7+
    private static final String PARALLEL_CLASS_LOADER_FACTORY = "com.navercorp.pinpoint.bootstrap.classloader.ParallelClassLoaderFactory";

    // jdk9
    private static final String JAVA9_CLASSLOADER = "com.navercorp.pinpoint.bootstrap.java9.classloader.Java9ClassLoader";

    //无参构造函数抛出异常，私有化，禁止利用构造函数构造对象
    private PinpointClassLoaderFactory() {
        throw new IllegalAccessError();
    }

    //创建类加载工厂
    public static ClassLoaderFactory createClassLoaderFactory() {
        //读取JVM版本
        final JvmVersion jvmVersion = JvmUtils.getVersion();
        //判断是否9+
        if (jvmVersion.onOrAfter(JvmVersion.JAVA_9)) {
            //返回JAVA9的ClassLoader
            return newClassLoaderFactory(JAVA9_CLASSLOADER);
        }

        // URLClassLoader 不能在java9工作
        if (disableChildFirst()) {
            return new URLClassLoaderFactory();
        }

        //7+版本
        if (jvmVersion.onOrAfter(JvmVersion.JAVA_7)) {
            return newParallelClassLoaderFactory();
        }

        // JDK6 --
        return new Java6ClassLoaderFactory();
    }

    //系统中读取disable参数判断是否使用URL类加载器
    private static boolean disableChildFirst() {
        String disable = System.getProperty("pinpoint.agent.classloader.childfirst.disable");
        return "true".equalsIgnoreCase(disable);
    }

    //根据给定的名字创建对应的类加载工厂
    private static ClassLoaderFactory newClassLoaderFactory(String factoryName) {
        ClassLoader classLoader = PinpointClassLoaderFactory.class.getClassLoader();

        //给出工厂名和当前的类加载器
        return new DynamicClassLoaderFactory(factoryName, classLoader);
    }

    //创建并行多线程类加载工厂
    private static ClassLoaderFactory newParallelClassLoaderFactory() {
        try {
            ClassLoader classLoader = PinpointClassLoaderFactory.class.getClassLoader();

            //反射后调用构造方法实例化
            final Class<? extends ClassLoaderFactory> classLoaderFactoryClazz =
                    (Class<? extends ClassLoaderFactory>) Class.forName(PARALLEL_CLASS_LOADER_FACTORY, true, classLoader);
            Constructor<? extends ClassLoaderFactory> constructor = classLoaderFactoryClazz.getDeclaredConstructor();
            return constructor.newInstance();
        } catch (Exception e) {
            throw new IllegalStateException(PARALLEL_CLASS_LOADER_FACTORY  + " create fail Caused by:" + e.getMessage(), e);
        }
    }


    //执行类加载工厂中的创建类加载器方法
    public static ClassLoader createClassLoader(String name, URL[] urls, ClassLoader parent, List<String> libClass) {
        return CLASS_LOADER_FACTORY.createClassLoader(name, urls, parent, libClass);
    }

}
