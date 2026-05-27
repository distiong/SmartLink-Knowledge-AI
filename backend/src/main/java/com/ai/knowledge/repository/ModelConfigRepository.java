package com.ai.knowledge.repository;

import com.ai.knowledge.entity.ModelConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ModelConfigRepository extends JpaRepository<ModelConfig, Long> {

    List<ModelConfig> findByEnabled(Integer enabled);

    Optional<ModelConfig> findByModelName(String modelName);

    boolean existsByModelName(String modelName);
}
