package com.ai.knowledge.service;

import com.ai.knowledge.entity.ModelConfig;
import com.ai.knowledge.repository.ModelConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ModelConfigService {

    @Autowired
    private ModelConfigRepository modelConfigRepository;

    public List<ModelConfig> getAllModels() {
        return modelConfigRepository.findAll();
    }

    public List<ModelConfig> getEnabledModels() {
        return modelConfigRepository.findByEnabled(1);
    }

    public ModelConfig getModelById(Long id) {
        return modelConfigRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("模型配置不存在"));
    }

    public Optional<ModelConfig> getModelByName(String modelName) {
        return modelConfigRepository.findByModelName(modelName);
    }

    public ModelConfig createModel(ModelConfig model) {
        if (modelConfigRepository.existsByModelName(model.getModelName())) {
            throw new RuntimeException("模型名称已存在");
        }
        return modelConfigRepository.save(model);
    }

    public ModelConfig updateModel(Long id, ModelConfig model) {
        ModelConfig existing = getModelById(id);
        existing.setModelName(model.getModelName());
        existing.setApiKey(model.getApiKey());
        existing.setApiUrl(model.getApiUrl());
        existing.setEnabled(model.getEnabled());
        return modelConfigRepository.save(existing);
    }

    public void deleteModel(Long id) {
        modelConfigRepository.deleteById(id);
    }

    public ModelConfig toggleModel(Long id) {
        ModelConfig model = getModelById(id);
        model.setEnabled(model.getEnabled() == 1 ? 0 : 1);
        return modelConfigRepository.save(model);
    }
}
