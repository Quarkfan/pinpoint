/*
 * Copyright 2016 NAVER Corp.
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
 *
 */

package com.navercorp.pinpoint.bootstrap;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * 参数处理器
 * @author Woonduk Kang(emeroad)
 * @author dean
 */
public class ArgsParser {

    //将字符串参数转换为Map
    public Map<String, String> parse(String args) {
        //参数为空返回空集合
        if (isEmpty(args)) {
            return Collections.emptyMap();
        }

        final Map<String, String> map = new HashMap<String, String>();

        Scanner scanner = new Scanner(args);
        //设置分隔符为空格
        scanner.useDelimiter("\\s*,\\s*");


        while (scanner.hasNext()) {
            String token = scanner.next();
            int assign = token.indexOf('=');

            if (assign == -1) {
                //非键值型参数
                map.put(token, "");
            } else {
                //键值型参数
                String key = token.substring(0, assign);
                String value = token.substring(assign + 1);
                map.put(key, value);
            }
        }
        scanner.close();
        //返回指定map的不可修改视图
        return Collections.unmodifiableMap(map);
    }

    //判断是否为空
    private boolean isEmpty(String args) {
        return args == null || args.isEmpty();
    }

}
