package com.javamian.redission.controller;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

/**
 * @program: xxkfz-admin-redisson
 * @ClassName BaseController.java
 * @author: 公众号：小小开发者
 * @create: 2024-01-19 09:58
 * @description: Redisson基本操作
 * @Version 1.0
 **/
@RestController
@RequestMapping(value = "/base")
@Slf4j
public class BaseController {

    @Autowired
    private RedissonClient redissonClient;

    /**
     * 通用对象桶，我们用来存放任类型的对象
     * Redisson将Redis中的字符串数据结构封装成了RBucket，
     * 通过RedissonClient的getBucket(key)方法获取一个RBucket对象实例，
     * 通过这个实例可以设置value或设置value和有效期。
     * 测试：http://127.0.0.1:8080/base/redissonBucket?key=xk&value=xxkfz
     *
     * @return
     */
    @GetMapping("redissonBucket")
    public String redissonBucket(String key, String value) {
        RBucket<String> rBucket = redissonClient.getBucket(key);
        rBucket.set(value, 30, TimeUnit.SECONDS);
        return redissonClient.getBucket(key).get().toString();
    }


    /**
     * Redisson操作List
     * 测试：http://127.0.0.1:8080/base/redissonList?listKey=xxkfz_key_list
     */
    @GetMapping("redissonList")
    public void redissonList(String listKey) {
        RList<String> list = redissonClient.getList(listKey);
        // 使用add方法向List中添加元素
        list.add("公众号: 小小开发者-list1");
        list.add("公众号: 小小开发者-list2");
        list.add("公众号: 小小开发者-list3");
        // 获取List中的元素
        String s = list.get(0);
        System.out.println("s = " + s);
        // 获取列表长度
        int size = list.size();
        System.out.println("size = " + size);
        Object object = redissonClient.getList(listKey).get(1);
        System.out.println("object = " + object);
    }

    /**
     * Redisson操作Set
     * 测试：http://127.0.0.1:8080/base/redissonSet?listKey=xxkfz_key_set
     *
     * @param setKey
     */
    @GetMapping("redissonSet")
    public void redissonSet(String setKey) {
        RSet<Object> set = redissonClient.getSet(setKey);
        set.add("公众号: 小小开发者-set1");
        set.add("公众号: 小小开发者-set2");
        System.out.println(set);
        //通过key取value值
        RSet<Object> setValue = redissonClient.getSet(setKey);
        System.out.println("setValue = " + setValue);
    }


    /**
     * Redisson操作map
     * 测试：http://127.0.0.1:8080/base/redissonMap?mapKey=xxkfz_key_map
     *
     * @param mapKey
     */
    @GetMapping("redissonMap")
    public void redissonMap(String mapKey) {
        // 创建Map对象
        RMap<String, String> map = redissonClient.getMap(mapKey);
        // 添加键值对
        map.put("key1", "value1");
        map.put("key2", "value2");
        map.put("key3", "value3");
        // 获取值
        String value = map.get("key1");
        System.out.println(value);
        // 删除键值对
        String removedValue = map.remove("key2");
        System.out.println(removedValue);
        // 获取Map大小
        int size = map.size();
        System.out.println(size);
    }

    /**
     * Redisson操作Queue
     * 测试：http://127.0.0.1:8080/base/redissonQueue?queueKey=xxkfz_key_queue
     *
     * @param queueKey
     */
    @GetMapping("redissonQueue")
    public void redissonQueue(String queueKey) {
        RQueue<String> rQueue = redissonClient.getQueue(queueKey);
        // 向队列中添加值
        rQueue.add("公众号: 小小开发者-queue1");
        rQueue.add("公众号: 小小开发者-queue2");
        // 取值
        String value = rQueue.poll();
        System.out.println("value = " + value);

        //
        RQueue<Object> queueValue = redissonClient.getQueue(queueKey);
        System.out.println("queueValue = " + queueValue);
    }


    /**
     * Redisson消息发布订阅操作
     * 消息监听器：详见TopicListener.java
     * 测试：http://127.0.0.1:8080/base/redissonTopic?topicKey=xxkfz_key_topic
     *
     * @param topicKey
     */
    @GetMapping("redissonTopic")
    public String redissonTopic(String topicKey) {
        RTopic rTopic = redissonClient.getTopic(topicKey);
        String msgId = UUID.fastUUID().toString();
        long victory = rTopic.publish(new com.javamian.redission.controller.TopicMsg(msgId, "消息：我是小小开发者"));
        return StrUtil.toString(victory);
    }

    /**
     * Redisson的可重入锁操作
     * 测试：http://127.0.0.1:8080/base/redissonLock?lockKey=xxkfz_key_lock
     *
     * @param lockKey
     * @return
     */
    @GetMapping("redissonLock")
    public String redissonLock(String lockKey) {
        RLock rLock = redissonClient.getLock(lockKey);
        for (int i = 0; i < 5; i++) {
            new Thread(() -> {
                rLock.lock();
                try {
                    System.out.println(Thread.currentThread() + "-" + System.currentTimeMillis() + "-" + "获取了锁");
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    rLock.unlock();
                }
            }).start();
        }
        return "success";
    }

    /**
     * Redisson实现限流
     * 具体可以查看接口的限流应用
     * <p>
     * <p>
     * 测试：http://127.0.0.1:8080/base/redissonRateLimiter?rateKey=xxkfz_key_rate
     *
     * @param rateKey
     */
    @GetMapping("redissonRateLimiter")
    public String redissonRateLimiter(String rateKey) {
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(rateKey);
        //创建限流器，最大流速:每10秒钟产生8个令牌
        rateLimiter.trySetRate(RateType.OVERALL, 8, 10, RateIntervalUnit.SECONDS);
        String availCount = "-1";
        if (rateLimiter.tryAcquire()) {
            availCount = StrUtil.toString(rateLimiter.availablePermits());
        }
        return availCount;
    }


    /**
     * Redisson操作布隆过滤器
     * 解决方案：缓存穿透
     * 测试：http://127.0.0.1:8080/base/redissonBloomFilter?bloomKey=xxkfz_key_bloom
     *
     * @param bloomKey
     */
    @GetMapping("redissonBloomFilter")
    public void redissonBloomFilter(String bloomKey) {
        RBloomFilter<String> rBloomFilter = redissonClient.getBloomFilter(bloomKey);
        // 初始化布隆过滤器，初始化预期插入的数据量为200，期望误差率为0.01
        rBloomFilter.tryInit(200, 0.01);
        // 插入数据
        rBloomFilter.add("小小开发者");
        rBloomFilter.add("开发者小小");
        rBloomFilter.add("开发");
        // 判断是否存在
        boolean victory = rBloomFilter.contains("开发者小小");
        boolean forward = rBloomFilter.contains("小小程序");
        System.out.println(victory); //true
        System.out.println(forward); //false
    }


    /**
     * 分布式锁案详解
     */
    @GetMapping("redissonLockTest")
    public void redissonLockTest(String lock) {
        RLock rLock = redissonClient.getLock(lock);
        // 获取锁，并设置锁的自动释放时间。
        try {
            //waitTime：等待获取锁的最大时间量; leaseTime：锁的自动释放时间; unit：时间单位。
            boolean tryLock = rLock.tryLock(2, 12, TimeUnit.SECONDS);
            if (tryLock) {
                // 模拟执行业务逻辑
                log.error("开始执行业务逻辑......");
                ThreadUtil.sleep(10000);
                log.error("业务逻辑执行完成......");
            } else {
                log.error("锁已存在......");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            if (rLock.isHeldByCurrentThread()) {
                rLock.unlock();
                log.error("释放锁完成");
            } else {
                log.error("锁已过期......");
            }
        }

    }
}
