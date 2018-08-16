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

package com.navercorp.pinpoint.bootstrap.config;

import com.navercorp.pinpoint.common.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * HTTP状态码错误
 * @author jaehong.kim
 * @author dean
 */
public class HttpStatusCodeErrors {
    private static final StatusCode ALL_STATUS_CODES = new StatusCode() {
        @Override
        //判断是否是一个合法的状态码 从100到599
        public boolean isCode(int statusCode) {
            return 100 <= statusCode && statusCode <= 599;
        }
    };
    //默认错误码
    private static final List<String> DEFAULT_ERROR_CODES = Arrays.asList("5xx");

    private final StatusCode[] errors;

    //构造方法
    public HttpStatusCodeErrors() {
        this(DEFAULT_ERROR_CODES);
    }

    //构造方法
    public HttpStatusCodeErrors(final List<String> errorCodes) {
        this.errors = newErrorCode(errorCodes);
    }

    public boolean isHttpStatusCode(final int statusCode) {
        return ALL_STATUS_CODES.isCode(statusCode);
    }

    public boolean isErrorCode(final int statusCode) {
        for (StatusCode code : this.errors) {
            if (code.isCode(statusCode)) {
                return true;
            }
        }
        return false;
    }

    //error Code 转换
    private StatusCode[] newErrorCode(List<String> errorCodes) {
        //为空，返回空的状态数组
        if (CollectionUtils.isEmpty(errorCodes)) {
            return new StatusCode[0];
        }


        List<StatusCode> statusCodeList = new ArrayList<StatusCode>();
        //迭代
        for (String errorCode : errorCodes) {
            if (errorCode.equalsIgnoreCase("5xx")) {
                //如果是5xx，添加一个 服务端错误
                statusCodeList.add(new ServerError());
            } else if (errorCode.equalsIgnoreCase("4xx")) {
                //如果是4xx，添加一个  客户端错误
                statusCodeList.add(new ClientError());
            } else if (errorCode.equalsIgnoreCase("3xx")) {
                //如果是3xx，添加一个  重定向
                statusCodeList.add(new Redirection());
            } else if (errorCode.equalsIgnoreCase("2xx")) {
                //如果是2xx，添加一个  成功
                statusCodeList.add(new Success());
            } else if (errorCode.equalsIgnoreCase("1xx")) {
                //如果是1xx，添加一个  信息记录
                statusCodeList.add(new Informational());
            } else {
                //如果都不在上面的列表里面，创建一个默认的状态对象。
                try {
                    final int statusCode = Integer.parseInt(errorCode);
                    statusCodeList.add(new DefaultStatusCode(statusCode));
                } catch (NumberFormatException ignored) {
                }
            }
        }
        //转换为状态数组
        return toArray(statusCodeList);
    }

    //转换为数组
    private <T> StatusCode[] toArray(List<StatusCode> list) {
        if (CollectionUtils.isEmpty(list)) {
            return new StatusCode[0];
        }
        return list.toArray(new StatusCode[0]);
    }

    //状态码接口
    private interface StatusCode {
        boolean isCode(int statusCode);
    }

    //默认状态码
    private static class DefaultStatusCode implements StatusCode {
        private final int statusCode;

        public DefaultStatusCode(final int statusCode) {
            this.statusCode = statusCode;
        }

        @Override
        public boolean isCode(int statusCode) {
            return this.statusCode == statusCode;
        }

        @Override
        public String toString() {
            return String.valueOf(statusCode);
        }
    }

    //信息
    private static class Informational implements StatusCode {
        @Override
        public boolean isCode(int statusCode) {
            return 100 <= statusCode && statusCode <= 199;
        }

        @Override
        public String toString() {
            return "1xx";
        }
    }

    //成功
    private static class Success implements StatusCode {
        @Override
        public boolean isCode(int statusCode) {
            return 200 <= statusCode && statusCode <= 299;
        }

        @Override
        public String toString() {
            return "2xx";
        }

    }

    //重定向
    private static class Redirection implements StatusCode {
        @Override
        public boolean isCode(int statusCode) {
            return 300 <= statusCode && statusCode <= 399;
        }

        @Override
        public String toString() {
            return "3xx";
        }

    }

    //客户端错误
    private static class ClientError implements StatusCode {
        @Override
        public boolean isCode(int statusCode) {
            return 400 <= statusCode && statusCode <= 499;
        }

        @Override
        public String toString() {
            return "4xx";
        }

    }

    //服务端错误
    private static class ServerError implements StatusCode {
        @Override
        public boolean isCode(int statusCode) {
            return 500 <= statusCode && statusCode <= 599;
        }

        @Override
        public String toString() {
            return "5xx";
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("HttpStatusCodeErrors{");
        sb.append("errors=").append(Arrays.toString(errors));
        sb.append('}');
        return sb.toString();
    }
}