package com.bingo.gpumanager.service;

import com.bingo.gpumanager.entity.GpuServer;
import com.bingo.gpumanager.exception.BusinessException;
import com.bingo.gpumanager.mapper.GpuServerMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import java.util.concurrent.ThreadLocalRandom;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;
import java.util.List;


@Slf4j
@Service
public class GpuServerService {

    private final GpuServerMapper gpuServerMapper;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private static final String SERVER_LIST_KEY = "gpu:servers:list";
    private static final String SERVER_DETAIL_PREFIX = "gpu:server:";

    public GpuServerService(
            GpuServerMapper gpuServerMapper,
            StringRedisTemplate redisTemplate,
            ObjectMapper objectMapper) {

        this.gpuServerMapper = gpuServerMapper;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public List<GpuServer> getAllServers() {

        String cache = redisTemplate.opsForValue().get(SERVER_LIST_KEY);

        // 1. Redis 有缓存
        if (cache != null) {

            try {
                System.out.println("从 Redis 查询服务器列表");

                return objectMapper.readValue(
                        cache,
                        new TypeReference<List<GpuServer>>() {}
                );

            } catch (Exception e) {
                redisTemplate.delete(SERVER_LIST_KEY);
            }
        }

        // 2. Redis 没缓存
        log.info("Redis 未命中，查询 MySQL");

        List<GpuServer> servers =
                gpuServerMapper.findAll();

        // 3. 查询结果写入 Redis
        try {

            String json =
                    objectMapper.writeValueAsString(servers);

            redisTemplate.opsForValue()
                    .set(
                            SERVER_LIST_KEY,
                            json,
                            10,
                            TimeUnit.MINUTES
                    );

        } catch (Exception e) {
            e.printStackTrace();
        }

        return servers;
    }

    public GpuServer getServerById(Long id) {

        String key = SERVER_DETAIL_PREFIX + id;

        String cache = redisTemplate.opsForValue().get(key);

        if ("NULL".equals(cache)) {

            log.info("Redis 命中空值缓存，serverId={}", id);

            throw new BusinessException(
                    404,
                    "服务器不存在"
            );
        }

        if (cache != null) {

            try {

                log.info(
                        "从 Redis 查询服务器详情，serverId={}",
                        id
                );

                return objectMapper.readValue(
                        cache,
                        GpuServer.class
                );

            } catch (Exception e) {

                log.warn(
                        "服务器详情缓存反序列化失败，删除缓存，serverId={}",
                        id,
                        e
                );

                redisTemplate.delete(key);
            }
        }

        log.info(
                "Redis 未命中，查询 MySQL，serverId={}",
                id
        );

        GpuServer server =
                gpuServerMapper.findById(id);

        if (server == null) {

            redisTemplate.opsForValue().set(
                    key,
                    "NULL",
                    2,
                    TimeUnit.MINUTES
            );

            log.info(
                    "服务器不存在，写入空值缓存，serverId={}",
                    id
            );

            throw new BusinessException(
                    404,
                    "服务器不存在"
            );
        }

        try {

            String json =
                    objectMapper.writeValueAsString(server);

            long ttl =
                    ThreadLocalRandom.current()
                            .nextLong(10, 16);

            redisTemplate.opsForValue().set(
                    key,
                    json,
                    ttl,
                    TimeUnit.MINUTES
            );

            log.info(
                    "服务器详情写入 Redis，serverId={}，ttl={}分钟",
                    id,
                    ttl
            );

        } catch (Exception e) {

            log.error(
                    "服务器详情写入 Redis 失败，serverId={}",
                    id,
                    e
            );
        }

        return server;
    }

    public void addServer(GpuServer gpuServer) {

        int result = gpuServerMapper.insert(gpuServer);

        if(result == 0){
            throw new BusinessException(500, "服务器添加失败");
        }

        redisTemplate.delete(SERVER_LIST_KEY);
    }

    public void updateServer(GpuServer gpuServer){

        GpuServer oldServer = gpuServerMapper.findById(gpuServer.getId());

        if(oldServer == null){
            throw new BusinessException(404, "服务器不存在");
        }

        int result = gpuServerMapper.update(gpuServer);

        if(result == 0){
            throw new BusinessException(500, "服务器修改失败");
        }

        redisTemplate.delete(SERVER_LIST_KEY);
        redisTemplate.delete(
                SERVER_DETAIL_PREFIX + gpuServer.getId()
        );
    }

    public void deleteServer(Long id){

        GpuServer server = gpuServerMapper.findById(id);

        if(server == null){
            throw new BusinessException(404, "服务器不存在");
        }

        gpuServerMapper.deleteById(id);

        redisTemplate.delete(SERVER_LIST_KEY);
        redisTemplate.delete(
                SERVER_DETAIL_PREFIX + id
        );
    }
}