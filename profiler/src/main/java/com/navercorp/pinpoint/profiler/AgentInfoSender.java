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

package com.navercorp.pinpoint.profiler;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import com.navercorp.pinpoint.io.request.EmptyMessage;
import com.navercorp.pinpoint.io.request.Message;
import com.navercorp.pinpoint.profiler.util.AgentInfoFactory;
import com.navercorp.pinpoint.rpc.DefaultFuture;
import com.navercorp.pinpoint.rpc.ResponseMessage;
import com.navercorp.pinpoint.thrift.dto.TResult;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseDeserializerFactory;
import com.navercorp.pinpoint.thrift.util.SerializationUtils;
import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.profiler.sender.EnhancedDataSender;
import com.navercorp.pinpoint.thrift.dto.TAgentInfo;

/**
 * agent信息发送器
 * @author emeroad
 * @author koo.taejin
 * @author HyunGil Jeong
 * @author dean
 */
public class AgentInfoSender {
    // refresh daily
    //刷新周期
    private static final long DEFAULT_AGENT_INFO_REFRESH_INTERVAL_MS = 24 * 60 * 60 * 1000L;
    // retry every 3 seconds
    //重试周期
    private static final long DEFAULT_AGENT_INFO_SEND_INTERVAL_MS = 3 * 1000L;
    // retry 3 times per attempt
    //重试次数
    private static final int DEFAULT_MAX_TRY_COUNT_PER_ATTEMPT = 3;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    //增强数据发送器
    private final EnhancedDataSender dataSender;
    //agent信息工厂
    private final AgentInfoFactory agentInfoFactory;
    private final long refreshIntervalMs;
    private final long sendIntervalMs;
    private final int maxTryPerAttempt;
    //执行计划
    private final Scheduler scheduler;

    //构造agent信息发送器
    private AgentInfoSender(Builder builder) {
        this.dataSender = builder.dataSender;
        this.agentInfoFactory = builder.agentInfoFactory;
        this.refreshIntervalMs = builder.refreshIntervalMs;
        this.sendIntervalMs = builder.sendIntervalMs;
        this.maxTryPerAttempt = builder.maxTryPerAttempt;
        this.scheduler = new Scheduler();
    }

    //开始
    public void start() {
        scheduler.start();
    }

    //停止
    public void stop() {
        scheduler.stop();
        logger.info("AgentInfoSender stopped");
    }

    //刷新
    public void refresh() {
        scheduler.refresh();
    }

    //成功接口
    private interface SuccessListener {
        void onSuccess();

        //无操作实现
        SuccessListener NO_OP = new SuccessListener() {
            @Override
            public void onSuccess() {
                // noop
            }
        };
    }

    //内部类，计划任务
    private class Scheduler {

        //立即执行
        private static final long IMMEDIATE = 0L;
        //timer计时器
        private final Timer timer = new Timer("Pinpoint-AgentInfoSender-Timer", true);
        //锁标记
        private final Object lock = new Object();
        // 运行标记。利用锁保护
        private boolean isRunning = true;

        //构造函数
        private Scheduler() {
            // 预加载测试，执行一下默认的无操作监听器
            AgentInfoSendTask task = new AgentInfoSendTask(SuccessListener.NO_OP);
            task.run();
        }

        //启动，不断添加任务保证任务持续
        public void start() {
            final SuccessListener successListener = new SuccessListener() {
                @Override
                public void onSuccess() {
                    schedule(this, maxTryPerAttempt, refreshIntervalMs, sendIntervalMs);
                }
            };
            schedule(successListener, Integer.MAX_VALUE, IMMEDIATE, sendIntervalMs);
        }

        //刷新
        public void refresh() {
            schedule(SuccessListener.NO_OP, maxTryPerAttempt, IMMEDIATE, sendIntervalMs);
        }

        //添加日程 成功后回调内容，重试次数，发送时间，发送间隔
        private void schedule(SuccessListener successListener, int retryCount, long delay, long period) {
            //加锁然后判断运行状态
            synchronized (lock) {
                if (isRunning) {
                    //创建发送任务
                    AgentInfoSendTask task = new AgentInfoSendTask(successListener, retryCount);
                    //添加到定时器上
                    timer.scheduleAtFixedRate(task, delay, period);
                }
            }
        }

        //停止
        public void stop() {
            //加锁
            synchronized (lock) {
                //运行状态标记修改
                isRunning = false;
                //取消所有任务
                timer.cancel();
            }
        }
    }

    //发送任务，用于交个Timer计时器执行
    private class AgentInfoSendTask extends TimerTask {

        //成功处理器
        private final SuccessListener taskHandler;
        //重试次数
        private final int retryCount;
        //计数器
        private AtomicInteger counter;

        private AgentInfoSendTask(SuccessListener taskHandler) {
            this(taskHandler, 0);
        }

        private AgentInfoSendTask(SuccessListener taskHandler, int retryCount) {
            if (taskHandler == null) {
                throw new NullPointerException("taskHandler must not be null");
            }
            this.taskHandler = taskHandler;
            this.retryCount = retryCount;
            this.counter = new AtomicInteger(0);
        }

        @Override
        public void run() {
            int runCount = counter.incrementAndGet();
            //达到最大的重试次数，取消当前任务
            if (runCount > retryCount) {
                this.cancel();
                return;
            }
            //发送agent信息
            boolean isSuccessful = sendAgentInfo();
            //成功后取消任务，然后执行一下成功回调
            if (isSuccessful) {
                logger.info("AgentInfo sent.");
                this.cancel();
                taskHandler.onSuccess();
            }
        }

        //发送agent信息
        private boolean sendAgentInfo() {
            try {
                //agent信息
                TAgentInfo agentInfo = agentInfoFactory.createAgentInfo();
                //响应接受
                final DefaultFuture<ResponseMessage> future = new DefaultFuture<ResponseMessage>();

                logger.info("Sending AgentInfo {}", agentInfo);
                //请求发送
                dataSender.request(agentInfo, new ResponseMessageFutureListener(future));
                //请求响应等待超时
                if (!future.await()) {
                    logger.warn("request timed out while waiting for response.");
                    return false;
                }
                //失败
                if (!future.isSuccess()) {
                    Throwable t = future.getCause();
                    logger.warn("request failed.", t);
                    return false;
                }
                //读取相应信息
                ResponseMessage responseMessage = future.getResult();
                if (responseMessage == null) {
                    logger.warn("result not set.");
                    return false;
                }
                return getResult(responseMessage);
            } catch (Exception e) {
                logger.warn("failed to send agent info.", e);
            }
            return false;
        }

        //从responseMessage中提取结果（是否成功）
        private boolean getResult(ResponseMessage responseMessage) {
            byte[] message = responseMessage.getMessage();
            Message<TBase<?, ?>> deserialize = SerializationUtils.deserialize(message, HeaderTBaseDeserializerFactory.DEFAULT_FACTORY, EmptyMessage.INSTANCE);
            TBase<?, ?> tbase = deserialize.getData();
            if (tbase == null) {
                logger.warn("tbase is null");
                return false;
            }
            if (!(tbase instanceof TResult)) {
                logger.warn("Invalid response : {}", tbase.getClass());
                return false;
            }
            TResult result = (TResult) tbase;
            if (!result.isSuccess()) {
                logger.warn("request unsuccessful. Cause : {}", result.getMessage());
                return false;
            }
            return true;
        }
    }

    //构建发送器的时候用于承载参数
    public static class Builder {
        private final EnhancedDataSender dataSender;
        private final AgentInfoFactory agentInfoFactory;
        private long refreshIntervalMs = DEFAULT_AGENT_INFO_REFRESH_INTERVAL_MS;
        private long sendIntervalMs = DEFAULT_AGENT_INFO_SEND_INTERVAL_MS;
        private int maxTryPerAttempt = DEFAULT_MAX_TRY_COUNT_PER_ATTEMPT;

        public Builder(EnhancedDataSender dataSender, AgentInfoFactory agentInfoFactory) {
            if (dataSender == null) {
                throw new NullPointerException("enhancedDataSender must not be null");
            }
            if (agentInfoFactory == null) {
                throw new NullPointerException("agentInfoFactory must not be null");
            }
            this.dataSender = dataSender;
            this.agentInfoFactory = agentInfoFactory;
        }

        public Builder refreshInterval(long refreshIntervalMs) {
            this.refreshIntervalMs = refreshIntervalMs;
            return this;
        }

        public Builder sendInterval(long sendIntervalMs) {
            this.sendIntervalMs = sendIntervalMs;
            return this;
        }

        public Builder maxTryPerAttempt(int maxTryCountPerAttempt) {
            this.maxTryPerAttempt = maxTryCountPerAttempt;
            return this;
        }

        //构建agent信息发送器
        public AgentInfoSender build() {
            if (this.refreshIntervalMs <= 0) {
                throw new IllegalStateException("agentInfoRefreshIntervalMs must be greater than 0");
            }
            if (this.sendIntervalMs <= 0) {
                throw new IllegalStateException("agentInfoSendIntervalMs must be greater than 0");
            }
            if (this.maxTryPerAttempt <= 0) {
                throw new IllegalStateException("maxTryPerAttempt must be greater than 0");
            }
            return new AgentInfoSender(this);
        }
    }
}
