package com.mmall.task;

import com.mmall.service.IOrderService;
import com.mmall.util.PropertiesUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 定时关闭订单任务
 * 理解：生成点单时，如果30分钟还没有付款的话，应该把该订单关闭，把订单中的产品数量再增加到产品库存中。
 * 保证产品表中的库存由回来了。
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
    @Scheduled(cron = "0 */1 * * * ?")
    public void closeOrderTaskV1(){
        log.info("关闭订单定时任务启动");
        int hour = Integer.parseInt(PropertiesUtil.getProperty(
                        "close.order.task.time.hour",
                        "2")); // 默认2小时
        // 关闭订单
        iOrderService.closeOrder(hour);
        log.info("关闭订单定时任务结束");
    }
}
