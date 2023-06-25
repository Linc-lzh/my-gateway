package com.gateway.config;

import com.gateway.util.E2s;
import com.gateway.util.RedisUtil;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Sets;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Component
@Data
public class ZookeeperConfigItem {

    @Value("${zookeeper.config.host}")
    private String host;

    @Value("${zookeeper.config.port}")
    private String port;

    private static volatile Set<String> deviceIdSet = Sets.newHashSet();
    private static volatile Set<String> ipSet = Sets.newHashSet();//从缓存内获取的封禁IP
    private static volatile Set<String> ipSet_1 = Sets.newHashSet();//从antispider服务获取的封禁IP
    private static volatile Set<String> uidSet = Sets.newHashSet();
    private static final long PERIOD = 60L * 1000;

    @PostConstruct
    private void init() throws Exception {
        refreshCaches();
        initZookeeperServer();
    }

    private void initZookeeperServer() throws Exception {
        String ServerHost = host + ":" + port;
        //构建客户端实例
        CuratorFramework curatorFramework= CuratorFrameworkFactory.builder()
                .connectString(ServerHost)
                .retryPolicy(new ExponentialBackoffRetry(1000,3)) // 设置重试策略
                .build();
        //启动客户端
        curatorFramework.start();

        String path = "/gateway-config";
        Stat stat = curatorFramework.checkExists().forPath(path);
        if (stat != null) {
            // 删除节点
            curatorFramework.delete()
                    .deletingChildrenIfNeeded()  // 如果存在子节点，则删除所有子节点
                    .forPath(path);  // 删除指定节点
        }

        // 创建节点
        curatorFramework.create()
                .creatingParentsIfNeeded()  // 如果父节点不存在，则创建父节点
                .withMode(CreateMode.PERSISTENT)
                .forPath(path, "Init Data".getBytes());

        final NodeCache nodeCache = new NodeCache(curatorFramework, path);
        // 启动NodeCache并立即从服务端获取最新数据
        nodeCache.start(true);

        // 注册节点变化监听器
        nodeCache.getListenable().addListener(new NodeCacheListener() {
            @Override
            public void nodeChanged() throws Exception {
                byte[] newData = nodeCache.getCurrentData().getData();
                System.out.println("Refresh cache when node data changed: " + new String(newData));
                refreshCaches();
            }
        });
    }

    public void refreshCaches() {
        /* 类加载时更新缓存 */
        deviceIdSet = getUpdateCache("spam:deviceIds");
        ipSet = getUpdateCache("spam:ips");
        uidSet = getUpdateCache("spam:uids");
    }

    private Set<String> getUpdateCache(String key) {
        String logStr = "antispiderutil update " + key;
        Jedis jedis = null;
        String redisKey = key;
        Set<String> container = new HashSet<String>();
        try {
            Stopwatch stopwatch = Stopwatch.createStarted();
            log.info(logStr, " begin key=", redisKey);
            jedis = RedisUtil.getJedis();
            if (jedis != null) {
                container = jedis.smembers(redisKey);
                log.info(logStr, " size=", container == null ? 0 : container.size());
            }
            log.info(logStr, " finish, took:" + stopwatch.toString());
        } catch (Exception ex) {
            log.error(logStr, " act=updateCache_error, ex=", E2s.exception2String(ex));
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return container;
    }
}
