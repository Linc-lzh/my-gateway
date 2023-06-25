package com.gateway.filter;

import com.gateway.config.ZookeeperConfigItem;
import com.gateway.util.E2s;
import com.gateway.util.RedisUtil;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class AntiSpamFilter implements Filter {
    @Autowired
    ZookeeperConfigItem zookeeperConfigItem;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        ZookeeperConfigItem zookeeperConfigItem1 = zookeeperConfigItem;
        HttpServletRequest httpReq = (HttpServletRequest)servletRequest;
        HttpServletResponse httpResp = (HttpServletResponse)servletResponse;
        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {

    }
}
