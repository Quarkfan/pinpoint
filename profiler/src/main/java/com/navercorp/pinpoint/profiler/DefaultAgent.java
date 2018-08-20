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

package com.navercorp.pinpoint.profiler;

import com.navercorp.pinpoint.ProductInfo;
import com.navercorp.pinpoint.bootstrap.Agent;
import com.navercorp.pinpoint.bootstrap.AgentOption;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerBinder;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.context.module.ApplicationContext;
import com.navercorp.pinpoint.profiler.context.module.DefaultApplicationContext;
import com.navercorp.pinpoint.profiler.context.module.DefaultModuleFactoryResolver;
import com.navercorp.pinpoint.profiler.context.module.ModuleFactory;
import com.navercorp.pinpoint.profiler.context.module.ModuleFactoryResolver;
import com.navercorp.pinpoint.profiler.util.SystemPropertyDumper;
import com.navercorp.pinpoint.profiler.logging.Slf4jLoggerBinder;

import com.navercorp.pinpoint.rpc.ClassPreLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 默认agent
 * @author emeroad
 * @author koo.taejin
 * @author hyungil.jeong
 * @author dean
 */
public class DefaultAgent implements Agent {

    //日志记录 self4j
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final PLoggerBinder binder;

    private final ProfilerConfig profilerConfig;

    private final ApplicationContext applicationContext;


    //agent状态锁
    private final Object agentStatusLock = new Object();
    private volatile AgentStatus agentStatus;


    static {
        // Preload classes related to pinpoint-rpc module.
        //预加载类和pinpoint-rpc模块有关
        //预加载用于验证网络环境
        ClassPreLoader.preload();
    }

    //构造函数
    public DefaultAgent(AgentOption agentOption) {
        //agent配置校验
        if (agentOption == null) {
            throw new NullPointerException("agentOption must not be null");
        }
        if (agentOption.getInstrumentation() == null) {
            throw new NullPointerException("instrumentation must not be null");
        }
        if (agentOption.getProfilerConfig() == null) {
            throw new NullPointerException("profilerConfig must not be null");
        }

        logger.info("AgentOption:{}", agentOption);

        //绑定日志记录
        this.binder = new Slf4jLoggerBinder();
        bindPLoggerFactory(this.binder);

        //备份系统属性和配置信息
        dumpSystemProperties();
        dumpConfig(agentOption.getProfilerConfig());

        //改变agent状态
        changeStatus(AgentStatus.INITIALIZING);

        //配置信息加载
        this.profilerConfig = agentOption.getProfilerConfig();

        //创建应用上下文
        this.applicationContext = newApplicationContext(agentOption);

    }

    //新建应用上下文
    protected ApplicationContext newApplicationContext(AgentOption agentOption) {
        //断言agent的配置不能为空
        Assert.requireNonNull(agentOption, "agentOption must not be null");
        ProfilerConfig profilerConfig = Assert.requireNonNull(agentOption.getProfilerConfig(), "profilerConfig must not be null");

        ModuleFactoryResolver moduleFactoryResolver = new DefaultModuleFactoryResolver(profilerConfig.getInjectionModuleFactoryClazzName());
        ModuleFactory moduleFactory = moduleFactoryResolver.resolve();
        return new DefaultApplicationContext(agentOption, moduleFactory);
    }

    protected ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    //备份系统配置信息
    private void dumpSystemProperties() {
        SystemPropertyDumper dumper = new SystemPropertyDumper();
        dumper.dump();
    }

    //备份配置信息
    private void dumpConfig(ProfilerConfig profilerConfig) {
        //利用日志备份信息
        if (logger.isInfoEnabled()) {
            logger.info("{}\n{}", "dumpConfig", profilerConfig);

        }
    }

    //agent状态
    private void changeStatus(AgentStatus status) {
        this.agentStatus = status;
        if (logger.isDebugEnabled()) {
            logger.debug("Agent status is changed. {}", status);
        }
    }

    //绑定日志工厂
    private void bindPLoggerFactory(PLoggerBinder binder) {
        final String binderClassName = binder.getClass().getName();
        PLogger pLogger = binder.getLogger(binder.getClass().getName());
        pLogger.info("PLoggerFactory.initialize() bind:{} cl:{}", binderClassName, binder.getClass().getClassLoader());
        // Set binder to static LoggerFactory
        // Should we unset binder at shutdown hook or stop()?
        PLoggerFactory.initialize(binder);
    }


    @Override
    public void start() {
        synchronized (agentStatusLock) {
            if (this.agentStatus == AgentStatus.INITIALIZING) {
                changeStatus(AgentStatus.RUNNING);
            } else {
                logger.warn("Agent already started.");
                return;
            }
        }
        logger.info("Starting {} Agent.", ProductInfo.NAME);
        this.applicationContext.start();

    }

    @Override
    public void stop() {
        synchronized (agentStatusLock) {
            if (this.agentStatus == AgentStatus.RUNNING) {
                changeStatus(AgentStatus.STOPPED);
            } else {
                logger.warn("Cannot stop agent. Current status = [{}]", this.agentStatus);
                return;
            }
        }
        logger.info("Stopping {} Agent.", ProductInfo.NAME);
        this.applicationContext.close();

        // for testcase
        if (profilerConfig.getStaticResourceCleanup()) {
            PLoggerFactory.unregister(this.binder);
        }
    }

}
