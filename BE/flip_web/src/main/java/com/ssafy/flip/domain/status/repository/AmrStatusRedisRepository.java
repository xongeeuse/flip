package com.ssafy.flip.domain.status.repository;

import com.ssafy.flip.domain.status.entity.AmrStatusRedis;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

@EnableRedisRepositories
public interface AmrStatusRedisRepository extends CrudRepository<AmrStatusRedis, String> {
    List<AmrStatusRedis> findAll();
}
