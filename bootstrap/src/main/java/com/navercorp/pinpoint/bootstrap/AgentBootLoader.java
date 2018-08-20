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

import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.concurrent.Callable;


/**
 * 引导加载器
 * @author emeroad
 * @author dean
 */
public class AgentBootLoader {

    /*
     * java.lang包下的类，安全管理器。
     *
     */
    private static final SecurityManager SECURITY_MANAGER = System.getSecurityManager();

    /*
    * 类加载器。
    *
    * */
    private final ClassLoader classLoader;

    /*
    * 引导类
    * */
    private final String bootClass;

    private final ContextClassLoaderExecuteTemplate<Object> executeTemplate;

    //构造函数，构造Agent引导加载器
    public AgentBootLoader(String bootClass, URL[] urls, ClassLoader agentClassLoader) {
        if (bootClass == null) {
            throw new NullPointerException("bootClass must not be null");
        }
        if (urls == null) {
            throw new NullPointerException("urls");
        }
        this.bootClass = bootClass;
        this.classLoader = agentClassLoader;
        //将agent的classloader放到上下文加载执行模板中，用于后续执行
        this.executeTemplate = new ContextClassLoaderExecuteTemplate<Object>(agentClassLoader);
    }

    //引导启动
    public Agent boot(final AgentOption agentOption) {
        //获取引导类
        final Class<?> bootStrapClazz = getBootStrapClass();

        //上下文加载执行模板执行，更换线程的类加载器为前面设置的agentclassloader
        //并执行给出的callable
        //并获取agent
        final Object agent = executeTemplate.execute(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                try {
                    //反射引导类构造方法
                    Constructor<?> constructor = bootStrapClazz.getDeclaredConstructor(AgentOption.class);
                    //构造实例化
                    return constructor.newInstance(agentOption);
                } catch (InstantiationException e) {
                    throw new BootStrapException("boot create failed. Error:" + e.getMessage(), e);
                } catch (IllegalAccessException e) {
                    throw new BootStrapException("boot method invoke failed. Error:" + e.getMessage(), e);
                }
            }
        });

        //检查agent，确认实例接口
        if (agent instanceof Agent) {
            return (Agent) agent;
        } else {
            //启动失败，抛出异常
            String agentClassName;
            if (agent == null) {
                agentClassName = "Agent is null";
            } else {
                agentClassName = agent.getClass().getName();
            }
            throw new BootStrapException("Invalid AgentType. boot failed. AgentClass:" + agentClassName);
        }
    }

    //获取引导类
    private Class<?> getBootStrapClass() {
        try {
            return this.classLoader.loadClass(bootClass);
        } catch (ClassNotFoundException e) {
            throw new BootStrapException("boot class not found. bootClass:" + bootClass + " Error:" + e.getMessage(), e);
        }
    }

}
