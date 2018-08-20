/*
 * Copyright 2014 NAVER Corp.
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

import com.navercorp.pinpoint.ProductInfo;
import com.navercorp.pinpoint.bootstrap.agentdir.AgentDirectory;
import com.navercorp.pinpoint.bootstrap.classloader.PinpointClassLoaderFactory;
import com.navercorp.pinpoint.bootstrap.classloader.ProfilerLibs;
import com.navercorp.pinpoint.bootstrap.config.DefaultProfilerConfig;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.common.Version;
import com.navercorp.pinpoint.common.util.PinpointThreadFactory;
import com.navercorp.pinpoint.common.util.SimpleProperty;
import com.navercorp.pinpoint.common.util.SystemProperty;

import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * pinpoint启动器
 * @author Jongho Moon
 * @author dean
 *
 */
class PinpointStarter {
    //日志记录器
    private final BootLogger logger = BootLogger.getLogger(PinpointStarter.class.getName());
    // agent类型 用于区分测试的agent和非测试等情况
    public static final String AGENT_TYPE = "AGENT_TYPE";
    //FIXME 默认agent
    public static final String DEFAULT_AGENT = "DEFAULT_AGENT";
    //引导类
    public static final String BOOT_CLASS = "com.navercorp.pinpoint.profiler.DefaultAgent";
    //插件测试agent
    public static final String PLUGIN_TEST_AGENT = "PLUGIN_TEST";
    //插件测试引导类
    public static final String PLUGIN_TEST_BOOT_CLASS = "com.navercorp.pinpoint.test.PluginTestAgent";

    //初始化配置存储
    private SimpleProperty systemProperty = SystemProperty.INSTANCE;

    private final Map<String, String> agentArgs;
    private final AgentDirectory agentDirectory;
    private final Instrumentation instrumentation;
    private final ClassLoader parentClassLoader;
    private final ModuleBootLoader moduleBootLoader;

    //构造函数
    public PinpointStarter(ClassLoader parentClassLoader, Map<String, String> agentArgs,
                           AgentDirectory agentDirectory,
                           Instrumentation instrumentation, ModuleBootLoader moduleBootLoader) {
        //        null == BootstrapClassLoader
//        if (bootstrapClassLoader == null) {
//            throw new NullPointerException("bootstrapClassLoader must not be null");
//        }
        //agent参数不能为空
        if (agentArgs == null) {
            throw new NullPointerException("agentArgs must not be null");
        }
        //agent目录不能为空
        if (agentDirectory == null) {
            throw new NullPointerException("agentDirectory must not be null");
        }
        //探针不能为空
        if (instrumentation == null) {
            throw new NullPointerException("instrumentation must not be null");
        }
        this.agentArgs = agentArgs;
        this.parentClassLoader = parentClassLoader;
        this.agentDirectory = agentDirectory;
        this.instrumentation = instrumentation;
        this.moduleBootLoader = moduleBootLoader;

    }

    //启动
    boolean start() {
        //agentid验证器
        final IdValidator idValidator = new IdValidator();
        //读取agentID
        final String agentId = idValidator.getAgentId();
        //agentID为空启动失败
        if (agentId == null) {
            return false;
        }
        //读取应用名称，为空启动失败
        final String applicationName = idValidator.getApplicationName();
        if (applicationName == null) {
            return false;
        }

        //判断是否是容器
        final ContainerResolver containerResolver = new ContainerResolver();
        final boolean isContainer = containerResolver.isContainer();

        //读取插件jar列表
        List<String> pluginJars = agentDirectory.getPlugins();
        //读取配置路径
        String configPath = getConfigPath(agentDirectory);
        if (configPath == null) {
            return false;
        }

        // 设置日志记录地址到系统属性中
        saveLogFilePath(agentDirectory);

        //保存版本信息
        savePinpointVersion();

        try {
            // Is it right to load the configuration in the bootstrap?
            // 装载配置信息
            ProfilerConfig profilerConfig = DefaultProfilerConfig.load(configPath);

            // 必须装载的lib列表
            final URL[] urls = resolveLib(agentDirectory);
            //创建类加载器
            final ClassLoader agentClassLoader = createClassLoader("pinpoint.agent", urls, parentClassLoader);
            //如果模块引导装载器不为空，开始装载模块
            if (moduleBootLoader != null) {
                this.logger.info("defineAgentModule");
                moduleBootLoader.defineAgentModule(agentClassLoader, urls);
            }
            //引导类加载
            final String bootClass = getBootClass();
            AgentBootLoader agentBootLoader = new AgentBootLoader(bootClass, urls, agentClassLoader);
            logger.info("pinpoint agent [" + bootClass + "] starting...");

            //加载agent参数
            AgentOption option = createAgentOption(agentId, applicationName, isContainer, profilerConfig, instrumentation, pluginJars, agentDirectory);

            //用Agent参数引导
            Agent pinpointAgent = agentBootLoader.boot(option);
            //agent启动
            pinpointAgent.start();
            //注册关闭的钩子
            registerShutdownHook(pinpointAgent);
            //agent启动完成
            logger.info("pinpoint agent started normally.");
        } catch (Exception e) {
            // unexpected exception that did not be checked above
            logger.warn(ProductInfo.NAME + " start failed.", e);
            return false;
        }
        return true;
    }

    //创建类加载器
    private ClassLoader createClassLoader(final String name, final URL[] urls, final ClassLoader parentClassLoader) {
        //系统中有安全配置
        if (System.getSecurityManager() != null) {
            //通过访问控制器进行特权运行，防止由于权限问题导致调用链后边的内容不能正常工作
            return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
                public ClassLoader run() {
                    //通过类加载器工厂，创建类加载器
                    return PinpointClassLoaderFactory.createClassLoader(name, urls, parentClassLoader, ProfilerLibs.PINPOINT_PROFILER_CLASS);
                }
            });
        } else {
            //没有安全管理
            return PinpointClassLoaderFactory.createClassLoader(name, urls, parentClassLoader, ProfilerLibs.PINPOINT_PROFILER_CLASS);
        }
    }

    //根据type返回测试的引导还是常规的引导
    private String getBootClass() {
        final String agentType = getAgentType().toUpperCase();
        if (PLUGIN_TEST_AGENT.equals(agentType)) {
            return PLUGIN_TEST_BOOT_CLASS;
        }
        return BOOT_CLASS;
    }

    //获取agent的类型
    private String getAgentType() {
        String agentType = agentArgs.get(AGENT_TYPE);
        if (agentType == null) {
            return DEFAULT_AGENT;
        }
        return agentType;

    }

    //创建agent参数
    private AgentOption createAgentOption(String agentId, String applicationName, boolean isContainer,
                                          ProfilerConfig profilerConfig,
                                          Instrumentation instrumentation,
                                          List<String> pluginJars,
                                          AgentDirectory agentDirectory) {
        List<String> bootstrapJarPaths = agentDirectory.getBootDir().toList();
        return new DefaultAgentOption(instrumentation, agentId, applicationName, isContainer, profilerConfig, pluginJars, bootstrapJarPaths);
    }

    // for test
    void setSystemProperty(SimpleProperty systemProperty) {
        this.systemProperty = systemProperty;
    }


    //注册关闭钩子
    private void registerShutdownHook(final Agent pinpointAgent) {
        final Runnable stop = new Runnable() {
            @Override
            public void run() {
                pinpointAgent.stop();
            }
        };
        //创建线程
        PinpointThreadFactory pinpointThreadFactory = new PinpointThreadFactory("Pinpoint-shutdown-hook", false);
        Thread thread = pinpointThreadFactory.newThread(stop);
        //注册停止钩子
        Runtime.getRuntime().addShutdownHook(thread);
    }

    //log保存地址设置到系统属性里
    private void saveLogFilePath(AgentDirectory agentDirectory) {
        String agentLogFilePath = agentDirectory.getAgentLogFilePath();
        logger.info("logPath:" + agentLogFilePath);

        systemProperty.setProperty(ProductInfo.NAME + ".log", agentLogFilePath);
    }

    //版本信息写入到系统的属性中
    private void savePinpointVersion() {
        logger.info("pinpoint version:" + Version.VERSION);
        systemProperty.setProperty(ProductInfo.NAME + ".version", Version.VERSION);
    }

    //读取配置路径
    private String getConfigPath(AgentDirectory agentDirectory) {
        //拼接配置名称
        final String configName = ProductInfo.NAME + ".config";
        //从systemProperty中读取配置的具体值
        String pinpointConfigFormSystemProperty = systemProperty.getProperty(configName);
        //读取成功返回
        if (pinpointConfigFormSystemProperty != null) {
            logger.info(configName + " systemProperty found. " + pinpointConfigFormSystemProperty);
            return pinpointConfigFormSystemProperty;
        }

        //从systemProperty中读取配置的具体值失败，尝试从config文件中读取
        String classPathAgentConfigPath = agentDirectory.getAgentConfigPath();
        if (classPathAgentConfigPath != null) {
            logger.info("classpath " + configName + " found. " + classPathAgentConfigPath);
            return classPathAgentConfigPath;
        }

        //读取失败
        logger.info(configName + " file not found.");
        return null;
    }

    //处理lib列表
    private URL[] resolveLib(AgentDirectory classPathResolver) {
        // this method may handle only absolute path,  need to handle relative path (./..agentlib/lib)
        //只支持绝对路径，处理相对路径需要补充
        String agentJarFullPath = classPathResolver.getAgentJarFullPath();
        String agentLibPath = classPathResolver.getAgentLibPath();
        List<URL> urlList = resolveLib(classPathResolver.getLibs());
        String agentConfigPath = classPathResolver.getAgentConfigPath();

        //记录日志
        if (logger.isInfoEnabled()) {
            logger.info("agent JarPath:" + agentJarFullPath);
            logger.info("agent LibDir:" + agentLibPath);
            for (URL url : urlList) {
                logger.info("agent Lib:" + url);
            }
            logger.info("agent config:" + agentConfigPath);
        }
        //转换为数组返回
        return urlList.toArray(new URL[0]);
    }

    //处理lib列表
    private List<URL> resolveLib(List<URL> urlList) {
        //默认agent的处理方式
        if (DEFAULT_AGENT.equalsIgnoreCase(getAgentType())) {
            final List<URL> releaseLib = new ArrayList<URL>(urlList.size());
            for (URL url : urlList) {
                //将URL转换为字符串，判断是否含有测试标记，如没有，添加到发布库中
                if (!url.toExternalForm().contains("pinpoint-profiler-test")) {
                    releaseLib.add(url);
                }
            }
            return releaseLib;
        } else {
            //测试全加载
            logger.info("load " + PLUGIN_TEST_AGENT + " lib");
            // plugin test
            return urlList;
        }
    }

}
