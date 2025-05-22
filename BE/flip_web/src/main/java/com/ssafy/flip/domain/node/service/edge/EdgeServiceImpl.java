package com.ssafy.flip.domain.node.service.edge;

import com.ssafy.flip.domain.node.entity.Edge;
import com.ssafy.flip.domain.node.repository.edge.EdgeRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class EdgeServiceImpl implements EdgeService {

    private final EdgeRepository edgeRepository;
    Map<String, String> edgeKeyToId = new ConcurrentHashMap<>();

    @Override
    public List<Edge> findAll() {
        return edgeRepository.findAll();
    }

    @Override
    public Optional<Edge> findById(Integer edgeId) {
        return edgeRepository.findById(edgeId);
    }

    @Override
    public Edge getEdge(Integer edgeId) {
        return edgeRepository.findById(edgeId)
                .orElseThrow();
    }

    @PostConstruct
    public void init() {
        List<Edge> edgeList = edgeRepository.findAll();

        edgeList.parallelStream().forEach(edge -> {
            try {
                int node1 = Math.min(edge.getNode1().getNodeId(), edge.getNode2().getNodeId());
                int node2 = Math.max(edge.getNode1().getNodeId(), edge.getNode2().getNodeId());
                String key = node1 + "-" + node2;

                edgeKeyToId.put(key, String.valueOf(edge.getEdgeId()));
            } catch (Exception e) {
                log.warn("❗ 엣지 캐시 적재 실패: 에러 = {}", e.getMessage());
            }
        });

        log.info("✅ Node1-Node2 → EdgeID 캐시 초기화 완료: {}개", edgeKeyToId.size());
    }

    @Override
    public Map<String, String> getEdgeKeyToIdMap() {
        return edgeKeyToId;
    }
}
