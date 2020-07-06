package com.mmall.task;

import ch.qos.logback.classic.gaffer.PropertyUtil;
import com.mmall.common.Const;
import com.mmall.service.IOrderService;
import com.mmall.util.PropertiesUtil;
import com.mmall.util.RedisShardedPoolUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 定时关闭订单任务
 * 理解：生成点单时，如果30分钟还没有付款的话，应该把该订单关闭，把订单中的产品数量再增加到产品库存中。
 * 保证产品表中的库存由回来了。
 *
 * @author Still
 * @version 1.0
 * @date 2020/7/1 10:42
 */
@Component
@Slf4j
public class CloseOrderTask {
    @Autowired
    private IOrderService iOrderService;

    /**
     * 非Tomcat集群：关闭订单任务
     * cron表达式：每一分钟执行一次（每一分钟的整数倍）
     */
    // @Scheduled(cron = "0 */1 * * * ?")
    public void closeOrderTaskV1() {
        log.info("关闭订单定时任务启动");
        int hour = Integer.parseInt(PropertiesUtil.getProperty(
                "close.order.task.time.hour",
                "2")); // 默认2小时
        // 关闭订单
        // iOrderService.closeOrder(hour);
        log.info("关闭订单定时任务结束");
    }

    /**
     * Tomcat集群：Redis分布式锁版本实现任务调度
     * 目的：一个时间点只有一台tomcat执行关闭订单任务
     */
    @Scheduled(cron = "0 */1 * * * ?")
    public void closeOrderTaskV2() {
        log.info("关闭订单定时任务启动");
        // 分布式锁的超时时间（即多久这个锁失效）
        Long lockTimeOut = Long.parseLong(PropertiesUtil.getProperty(
                "lock.timeout", "5000"));
        // 在redis中存储key为CLOSE_ORDER_TASK_LOCK,value为当前时间毫秒数+分布式锁超时时间毫秒数
        // value值为过期的具体时间毫秒数
        Long setNxResult = RedisShardedPoolUtil.setNx(
                Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK,
                String.valueOf(System.currentTimeMillis() + lockTimeOut));
        if (setNxResult != null && setNxResult.intValue() == 1) {
            // 如果返回值为1，代表设置成功，进行关闭订单操作
            closeOrder(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);

        }else{
            // 当redis中key等于CLOSE_ORDER_TASK_LOCK已存在，将走进这里
            log.info("没有获得分布式锁：{}",Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
        }
        log.info("关闭订单定时任务结束");
    }

    /**
     * 关闭订单
     * @param lockName 在redis中的分布式锁key
     */
    private void closeOrder(String lockName){
        // 为key设置过期时间（50秒），防止死锁
        RedisShardedPoolUtil.expire(lockName,50);
        log.info("获取{}，当前线程名称：{}",Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK,
                Thread.currentThread().getName());
        // 关闭订单
        int hour = Integer.parseInt(PropertiesUtil.getProperty(
                "close.order.task.time.hour",
                "2")); // 默认2小时
        // iOrderService.closeOrder(hour);
        // 释放锁（key）
        RedisShardedPoolUtil.del(lockName);
        log.info("释放{}，当前线程名称：{}",Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK,
                Thread.currentThread().getName());
    }
}

