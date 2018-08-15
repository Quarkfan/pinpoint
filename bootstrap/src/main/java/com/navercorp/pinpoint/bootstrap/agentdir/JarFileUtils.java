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

import java.io.File;
import java.io.IOException;
import java.util.jar.JarFile;

/**
 * JarFile工具类
 * @author Woonduk Kang(emeroad)
 * @author dean
 */
final class JarFileUtils {

    //给定filepath打开jarFile
    public static JarFile openJarFile(String filePath) {
        //路径不为空
        if (filePath == null) {
            throw new NullPointerException("filePath must not be null");
        }

        final File file = new File(filePath);
        //路径存在
        if (!file.exists()) {
            throw new IllegalArgumentException(file + " not found");
        }
        //路径不是一个目录
        if (file.isDirectory()) {
            throw new IllegalArgumentException(file + " is directory");
        }
        //必须是一个文件
        if (!(file.isFile())) {
            throw new IllegalArgumentException(file + " not file");
        }
        //文件必须可读
        if (!file.canRead()) {
            throw new IllegalArgumentException(file + " can read");
        }
        //构建JarFile
        try {
            return new JarFile(file);
        } catch (IOException e) {
            throw new IllegalStateException(file + " create fail Caused by:" + e.getMessage(), e);
        }
    }
}
