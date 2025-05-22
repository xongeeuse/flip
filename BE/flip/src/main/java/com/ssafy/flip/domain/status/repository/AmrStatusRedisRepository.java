package com.ssafy.flip.domain.status.repository;

import com.ssafy.flip.domain.status.entity.AmrStatusRedis;
import org.springframework.data.repository.CrudRepository;

public interface AmrStatusRedisRepository extends CrudRepository<AmrStatusRedis, String> {
}
