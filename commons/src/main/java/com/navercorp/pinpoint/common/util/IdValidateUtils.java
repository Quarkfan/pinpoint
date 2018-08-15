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

package com.navercorp.pinpoint.common.util;

import com.navercorp.pinpoint.common.PinpointConstants;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 校验工具类
 * @author emeroad
 * @author dean
 */
public final class IdValidateUtils {

    private static final int DEFAULT_MAX_LENGTH = PinpointConstants.AGENT_NAME_MAX_LEN;

    public static String STABLE_VERSION_PATTERN_VALUE = "[0-9]+\\.[0-9]+\\.[0-9]";

    //    private static final Pattern ID_PATTERN = Pattern.compile("[a-zA-Z0-9\\._\\-]{1,24}");
    public static final String ID_PATTERN_VALUE = "[a-zA-Z0-9\\._\\-]+";
    private static final Pattern ID_PATTERN = Pattern.compile(ID_PATTERN_VALUE);

    private IdValidateUtils() {
    }

    public static boolean validateId(String id) {
        return validateId(id, DEFAULT_MAX_LENGTH);
    }
    //校验
    public static boolean validateId(String id, int maxLength) {
        if (id == null) {
            throw new NullPointerException("id must not be null");
        }
        if (maxLength <= 0) {
            throw new IllegalArgumentException("negative maxLength:" + maxLength);
        }
        //校验组成是否正确
        if (!checkPattern(id)) {
            return false;
        }
        //校验长度是否正确
        if (!checkLength(id, maxLength)) {
            return false;
        }

        return true;
    }

    //正则校验
    public static boolean checkPattern(String id) {
        final Matcher matcher = ID_PATTERN.matcher(id);
        return matcher.matches();
    }

    //校验长度是否超长
    public static boolean checkLength(String id, int maxLength) {
        if (id == null) {
            throw new NullPointerException("id must not be null");
        }
        // 尝试编码
        final int idLength = getLength(id);
        if (idLength <= 0) {
            return false;
        }
        return idLength <= maxLength;
    }

    //编码返回长度
    public static int getLength(String id) {
        if (id == null) {
            return -1;
        }

        final byte[] idBytes = BytesUtils.toBytes(id);
        if (idBytes == null) {
            // 编码失败
            return -1;
        }
        return idBytes.length;
    }

}
