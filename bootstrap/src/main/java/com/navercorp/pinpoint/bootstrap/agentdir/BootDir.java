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

import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;

/**
 * 用于存储引导路径内的各个包的地址
 * @author Woonduk Kang(emeroad)
 * @author dean
 */
public class BootDir {

    private final BootLogger logger = BootLogger.getLogger(this.getClass().getName());

    //相关包
    private final String commons;
    private final String bootstrapCore;
    private final String bootstrapCoreOptional;
    private final String bootstrapJava9;
    private final String annotations;


    public BootDir(String commons, String bootstrapCore, String bootstrapCoreOptional, String bootstrapJava9, String annotations) {
        //common和BootStrapcore必须存在否则抛出异常
        if (commons == null) {
            throw new NullPointerException("commons must not be null");
        }
        if (bootstrapCore == null) {
            throw new NullPointerException("bootstrapCore must not be null");
        }

        this.commons = commons;
        this.bootstrapCore = bootstrapCore;
        // 可选包
        this.bootstrapCoreOptional = bootstrapCoreOptional;
        // 可选包
        this.bootstrapJava9 = bootstrapJava9;
        // 可选包
        this.annotations = annotations;
        //验证是否完整
        verify();
    }

    //验证是否完整
    private void verify() {
        // 验证 pinpoint-commons.jar 是否存在，不存在抛出异常
        final String pinpointCommonsJar = getCommons();
        if (pinpointCommonsJar == null) {
            throw new IllegalStateException("pinpoint-commons-x.x.x(-SNAPSHOT).jar not found");
        }

        // 验证 bootstrap-core.jar 是否存在，不存在抛出异常
        final String bootStrapCoreJar = getBootstrapCore();
        if (bootStrapCoreJar == null) {
            throw new IllegalStateException("pinpoint-bootstrap-core-x.x.x(-SNAPSHOT).jar not found");
        }

        //验证 bootstrap-core-optional.jar 是否存在，不存在记录日志
        final String bootStrapCoreOptionalJar = getBootstrapCoreOptional();
        if (bootStrapCoreOptionalJar == null) {
            logger.info("pinpoint-bootstrap-core-optional-x.x.x(-SNAPSHOT).jar not found");
        }

        //验证 bootstrap-java9.jar 是否存在，不存在记录日志
        final String bootStrapJava9Jar = getBootstrapJava9();
        if (bootStrapJava9Jar == null) {
            logger.info("pinpoint-bootstrap-java9-x.x.x(-SNAPSHOT).jar not found");
        }
        // 验证 annotations.jar 是否存在，可选的依赖jar，不存在记录日志
        final String annotationsJar = getAnnotations();
        if (annotationsJar == null) {
            logger.info("pinpoint-annotations-x.x.x(-SNAPSHOT).jar not found");
        }
    }

    public String getCommons() {
        return commons;
    }

    public String getBootstrapCore() {
        return bootstrapCore;
    }

    public String getBootstrapCoreOptional() {
        return bootstrapCoreOptional;
    }

    public String getBootstrapJava9() {
        return bootstrapJava9;
    }

    public String getAnnotations() {
        return annotations;
    }

    //将各个绝对路径存储到一个list中返回
    public List<String> toList() {
        final List<String> list = new ArrayList<String>();
        //将文件放入给定的list
        addFilePath(list, commons, true);
        addFilePath(list, bootstrapCore, true);
        addFilePath(list, bootstrapCoreOptional, false);
        addFilePath(list, bootstrapJava9, false);
        addFilePath(list, annotations, false);

        return list;
    }

    //将文件放入给定的list，根据required字段决定如果为空是否抛出异常
    private void addFilePath(List<String> list, String filePath, boolean required) {
        if (checkRequired(filePath, required)) return;
        list.add(filePath);
    }

    //判断是否是必须字段，必须字段为空抛出异常
    private boolean checkRequired(String filePath, boolean required) {
        if (required) {
            //字段必须并且为空，抛出异常
            if (filePath == null) {
                throw new IllegalStateException("filePath must not be null");
            }
        } else {
            //字段如果非必须，忽略
            if (filePath == null) {
                return true;
            }
        }
        return false;
    }
    //打开相关jar
    public List<JarFile> openJarFiles() {
        final List<JarFile> jarFileList = new ArrayList<JarFile>();
        //添加jar到list中
        addJarFile(jarFileList, commons, true);
        addJarFile(jarFileList, bootstrapCore, true);
        addJarFile(jarFileList, bootstrapCoreOptional, false);
        addJarFile(jarFileList, bootstrapJava9, false);
        addJarFile(jarFileList, annotations, false);

        return jarFileList;
    }

    //添加jar到给定list中，并按照是否必须处理抛出异常和忽略
    private void addJarFile(List<JarFile> list, String filePath, boolean required) {
        if (checkRequired(filePath, required)) return;
        //将filepath转换为JarFile
        JarFile jarFile = JarFileUtils.openJarFile(filePath);
        list.add(jarFile);
    }
}
