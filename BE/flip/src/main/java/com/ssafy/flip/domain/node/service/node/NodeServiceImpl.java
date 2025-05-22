package com.ssafy.flip.domain.node.service.node;

import com.ssafy.flip.domain.node.entity.Node;
import com.ssafy.flip.domain.node.repository.node.NodeRepository;
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
public class NodeServiceImpl implements NodeService {

    private final NodeRepository nodeRepository;
    private final Map<Integer, Node> nodeCache = new ConcurrentHashMap<>();

    @Override
    public List<Node> findAll() {
        return nodeRepository.findAll();
    }

    @Override
    public Optional<Node> findById(Integer nodeId) {
        return nodeRepository.findById(nodeId);
    }

    @Override
    public Node getNode(Integer nodeId) {
        return nodeRepository.findById(nodeId)
                .orElseThrow();
    }

    @PostConstruct
    public void init() {
        // Node를 DB에서 전부 불러오기
        List<Node> nodeList = nodeRepository.findAll(); // JPA로 전부 조회

        nodeList.parallelStream().forEach(node -> {
            try {
                nodeCache.put(node.getNodeId(), node);
            } catch (Exception e) {
                log.warn("❗ 노드 캐시 적재 실패: ID = {}, 에러 = {}", node.getNodeId(), e.getMessage());
            }
        });

        System.out.println("✅ Node 캐시 초기화 완료 (DB): " + nodeCache.size() + "개");
    }


    @Override
    public Node getNodeFromCache(int nodeId) {
        Node node = nodeCache.get(nodeId);
        if (node == null) throw new IllegalStateException("❌ Node 캐시에 없음: " + nodeId);
        return node;
    }

}
