package com.mmall.task;

import com.mmall.common.Const;
import com.mmall.common.RedissonManager;
import com.mmall.service.IOrderService;
import com.mmall.util.PropertiesUtil;
import com.mmall.util.RedisShardedPoolUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.concurrent.TimeUnit;

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

    @Autowired
    private RedissonManager redissonManager;

    /**
     * tomcat容器关闭前执行的方法（执行shutdown才能执行，直接kill不会执行）
     */
    @PreDestroy
    public void delLock() {
        RedisShardedPoolUtil.del(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
    }

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
     * V2版本：当使用kill关闭tomcat，会造成死锁的可能。
     */
    // @Scheduled(cron = "0 */1 * * * ?")
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

        } else {
            // 当redis中key等于CLOSE_ORDER_TASK_LOCK已存在，将走进这里
            log.info("没有获得分布式锁：{}", Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
        }
        log.info("关闭订单定时任务结束");
    }

    /**
     * V3版本：解决V2中存在的死锁问题
     */
    // @Scheduled(cron = "0 */1 * * * ?")
    public void closeOrderTaskV3() {
        log.info("=====关闭订单任务启动=====");
        Long lockTimeOut = Long.parseLong(PropertiesUtil.getProperty(
                "lock.timeout", "5000"));
        // 向redis中set关闭订单的锁
        Long setNxResult = RedisShardedPoolUtil.setNx(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK,
                String.valueOf(System.currentTimeMillis() + lockTimeOut));
        // 返回結果如果是1则证明：1.redis中不存在CLOSE_ORDER_TASK_LOCK这个key 2.set成功
        log.info("当前获取的setNxResult:{}", setNxResult);
        if (setNxResult != null && setNxResult.intValue() == 1) {
            // 关闭指定时间未支付的订单
            closeOrder(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
        } else {
            // 如果结果不为1，则证明redis中存在CLOSE_ORDER_TASK_LOCK这个key
            // 获取key的value（时间戳：锁的有效时间-当前时间+锁的过期时间lockTimeOut）
            String lockVal = RedisShardedPoolUtil.get(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
            // 如果当前时间大于lockVal，证明这个锁已经失效
            // 那么我们就可以重新设置锁，然后进行关闭订单操作
            if (lockVal != null && System.currentTimeMillis() > Long.parseLong(lockVal)) {
                // 设置新锁的同时，将锁的旧值返回
                String getLockVal = RedisShardedPoolUtil.getSet(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK,
                        String.valueOf(System.currentTimeMillis() + lockTimeOut));
                // 如果锁的旧值getLockVal为null，即其他进程（tomcat）对这个锁进行了del操作
                // 如果锁的旧值不为空且上面获取的lockVal相同，代表期间没有其他进程对锁进行过操作。
                // 那么我们就可以放心的进行关闭订单的操作
                if (getLockVal == null || (getLockVal != null && StringUtils.equals(lockVal, getLockVal))) {
                    // 关闭订单
                    closeOrder(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
                } else {
                    // 如果getLockVal和lockVal不相等，证明其他的进行对锁进行了操作，结束当前动作
                    log.info("没有获取到分布式锁：{}", Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
                }
            } else {
                log.info("没有获取到分布式锁：{}", Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
            }
        }
        log.info("=====关闭订单任务结束=====");
    }

    /**
     * springSchedule+Redisson框架分布式锁实现任务调度
     */
    @Scheduled(cron = "0 */1 * * * ?")
    public void closeOrderTaskV4() {
        // 创建锁
        RLock lock = redissonManager
                .getRedisson()
                .getLock(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
        boolean getLock = false;
        try {
            // 获取锁：waitTime尝试获取锁的时候等待时间，leaseTime锁的被动释放时间，单位：秒
            getLock = lock.tryLock(0, 5, TimeUnit.SECONDS);
            // 如果获取到锁，即getLock为true，关闭未支付订单
            if (getLock) {
                log.info("Redisson获取到分布式锁：{},线程名称：{}",
                        Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK,
                        Thread.currentThread().getName());
                int hour = Integer.parseInt(PropertiesUtil.getProperty(
                        "close.order.task.time.hour",
                        "2")); // 默认2小时
                iOrderService.closeOrder(hour);
            }else{
                log.info("Redisson没有获取到分布式锁：{},线程名称：{}",
                        Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK,
                        Thread.currentThread().getName());
            }
        } catch (InterruptedException e) {
            log.error("Redisson获取分布式锁异常",e);
        } finally {
            if(!getLock){
                return;
            }
            // 释放锁
            lock.unlock();
            log.info("Redisson分布式锁释放");
        }

    }

    /**
     * 关闭订单
     *
     * @param lockName 在redis中的分布式锁key
     */
    private void closeOrder(String lockName) {
        // 为key设置过期时间（5秒），防止死锁
        RedisShardedPoolUtil.expire(lockName, 50);
        log.info("获取{}，当前线程名称：{}", Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK,
                Thread.currentThread().getName());
        // 关闭订单
        int hour = Integer.parseInt(PropertiesUtil.getProperty(
                "close.order.task.time.hour",
                "2")); // 默认2小时
        // 模拟消耗了一段时间的关闭订单任务
        //int j = 0;
        //for(int i=0;i<200;i++){
        //    j = j+i;
        //}
        iOrderService.closeOrder(hour);
        // 释放锁（key）
        RedisShardedPoolUtil.del(lockName);
        log.info("释放{}，当前线程名称：{}", Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK,
                Thread.currentThread().getName());
    }
}

