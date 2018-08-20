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

import java.util.concurrent.Callable;

/**
 * This template is used for changing the current thread's classloader to the assigned one and executing a callable.
 * 用于把当前线程的加载器改变为指定的加载器，并执行callable线程
 * @author emeroad
 */
public class ContextClassLoaderExecuteTemplate<V> {
    // @Nullable
    private final ClassLoader classLoader;

    public ContextClassLoaderExecuteTemplate(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public V execute(Callable<V> callable) throws BootStrapException {
        try {
            //读取当前线程
            final Thread currentThread = Thread.currentThread();
            //保存之前的类加载器
            final ClassLoader before = currentThread.getContextClassLoader();
            // ctxCl == null safe?
            //设置当前线程得上下文类加载器为指定的这个
            currentThread.setContextClassLoader(this.classLoader);
            try {
                //执行线程
                return callable.call();
            } finally {
                // even though  the "BEFORE" classloader  is null, rollback  is needed.
                // if an exception occurs BEFORE callable.call(), the call flow can't reach here.
                // so  rollback  here is right.

                //出现异常后，重新把classloader设置回去
                currentThread.setContextClassLoader(before);
            }
        } catch (BootStrapException ex){
            throw ex;
        } catch (Exception ex) {
            throw new BootStrapException("execute fail. Error:" + ex.getMessage(), ex);
        }
    }
}
