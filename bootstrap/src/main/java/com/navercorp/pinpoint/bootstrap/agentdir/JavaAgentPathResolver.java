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

import com.navercorp.pinpoint.common.annotations.VisibleForTesting;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 解析JavaAgent地址
 * @author Woonduk Kang(emeroad)
 * @author dean
 */
public class JavaAgentPathResolver {

    static final String JAVA_AGENT_OPTION = "-javaagent:";
    private final Pattern DEFAULT_AGENT_PATTERN = AgentDirBaseClassPathResolver.DEFAULT_AGENT_BOOTSTRAP_PATTERN;

    private final ResolvingType resolvingType;

    enum    ResolvingType {
        INPUT_ARGUMENT,
        // Added for unknown bugs.
        @Deprecated
        SYSTEM_PROPERTY
    };

    //构造方法
    JavaAgentPathResolver(ResolvingType resolvingType) {
        if (resolvingType == null) {
            throw new NullPointerException("type must not be null");
        }
        this.resolvingType = resolvingType;
    }

    //初始化一个Agent的路径处理器
    public static JavaAgentPathResolver newJavaAgentPathResolver() {
        //读取处理类型
        final ResolvingType resolvingType = getResolvingType();
        //调用构造方法处理
        return new JavaAgentPathResolver(resolvingType);
    }

    //读取配置
    private static ResolvingType getResolvingType() {
        final String type = System.getProperty("pinpoint.javaagent.resolving", "");
        if (type.equalsIgnoreCase("system")) {
            return ResolvingType.SYSTEM_PROPERTY;
        }
        return ResolvingType.INPUT_ARGUMENT;
    }

    //处理agent的路径
    public String resolveJavaAgentPath() {
        //如果是系统配置类型，从classpath读取
        if (resolvingType == ResolvingType.SYSTEM_PROPERTY) {
            return getClassPathFromSystemProperty();
        }

        //RuntimeMXBean用于获取系统信息
        RuntimeMXBean runtimeMXBean = getRuntimeMXBean();

        //获取参数列表
        List<String> inputArguments = runtimeMXBean.getInputArguments();
        //遍历，并利用正则匹配出agent所需参数，并去掉agent参数的前缀
        for (String inputArgument : inputArguments) {
            if (isPinpointAgent(inputArgument, DEFAULT_AGENT_PATTERN)) {
                return removeJavaAgentPrefix(inputArgument);
            }
        }
        //没找到抛出异常
        throw new IllegalArgumentException(JAVA_AGENT_OPTION + " not found");
    }

    @VisibleForTesting
    RuntimeMXBean getRuntimeMXBean() {
        //从ManagementFactory中获取MXBean，可以或去运行时信息，包括容器信息
        return ManagementFactory.getRuntimeMXBean();
    }
    //移除参数名前缀
    private String removeJavaAgentPrefix(String inputArgument) {
        return inputArgument.substring(JAVA_AGENT_OPTION.length(), inputArgument.length());
    }

    //判断是否为agent参数
    private boolean isPinpointAgent(String inputArgument, Pattern javaPattern) {
        if (!inputArgument.startsWith(JAVA_AGENT_OPTION)) {
            return false;
        }
        //利用正则匹配其中结果
        Matcher matcher = javaPattern.matcher(inputArgument);
        return matcher.find();
    }

    //系统配置类型，从classpath中读取
    String getClassPathFromSystemProperty() {
        return System.getProperty("java.class.path");
    }

}
