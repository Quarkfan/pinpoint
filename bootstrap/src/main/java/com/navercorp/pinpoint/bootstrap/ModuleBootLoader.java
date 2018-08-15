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

package com.navercorp.pinpoint.bootstrap;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;

/**
 * 模块加载器
 * @author Woonduk Kang(emeroad)
 * @author dean
 */
class ModuleBootLoader {

    private final Instrumentation instrumentation;
    // @Nullable
    private final ClassLoader parentClassLoader;

    private Object moduleSupport;

    //构造方法
    ModuleBootLoader(Instrumentation instrumentation, ClassLoader parentClassLoader) {
        //instrumentation不能为空
        if (instrumentation == null) {
            throw new NullPointerException("instrumentation must not be null");
        }
        this.instrumentation = instrumentation;
        this.parentClassLoader = parentClassLoader;
    }

    //加载支持模块
    void loadModuleSupport() {
        try {
            //获取模块工厂
            Class<?> bootStrapClass = getModuleSupportFactoryClass(parentClassLoader);
            //实例化模块工厂
            Object moduleSupportFactory = newModuleSupportFactory(bootStrapClass);
            //反射其中的newModuleSupport方法
            Method newModuleSupportMethod = moduleSupportFactory.getClass().getMethod("newModuleSupport", Instrumentation.class);
            //初始化一个moduleSupport
            this.moduleSupport = newModuleSupportMethod.invoke(moduleSupportFactory, instrumentation);
            //获取modulesupport类，反射setup方法，执行，设置id，name等相关
            Class<?> moduleSupportSetup = moduleSupport.getClass();
            Method setupMethod = moduleSupportSetup.getMethod("setup");
            setupMethod.invoke(moduleSupport);
        } catch (Exception e) {
            throw new IllegalStateException("moduleSupport load fail Caused by:" + e.getMessage(), e);
        }
    }

    //定义agent模块
    void defineAgentModule(ClassLoader classLoader, URL[] jarFileList) {
        //如果不支持模块化，抛出异常
        if (moduleSupport == null) {
            throw new IllegalStateException("moduleSupport not loaded");
        }

        try {
            //通过反射执行defineAgentModule方法，加载相关模块
            Method definePinpointPackage = this.moduleSupport.getClass().getDeclaredMethod("defineAgentModule", ClassLoader.class, URL[].class);
            definePinpointPackage.invoke(moduleSupport, classLoader, jarFileList);
        } catch (Exception ex) {
            throw new IllegalStateException("defineAgentPackage fail: Caused by:" + ex.getMessage(), ex);
        }
    }

    //加载模块工厂类
    private Class<?> getModuleSupportFactoryClass(ClassLoader parentClassLoader) {
        try {
            return Class.forName("com.navercorp.pinpoint.bootstrap.java9.module.ModuleSupportFactory", false, parentClassLoader);
        } catch (ClassNotFoundException ex) {
            throw new IllegalStateException("ModuleSupportFactory not found Caused by:" + ex.getMessage(), ex);
        }
    }

    //实例化模块工厂
    private Object newModuleSupportFactory(Class<?> bootStrapClass) {
        try {
            Constructor<?> constructor = bootStrapClass.getDeclaredConstructor();
            return constructor.newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("ModuleSupportFactory() initialize fail", e);
        }
    }
}
