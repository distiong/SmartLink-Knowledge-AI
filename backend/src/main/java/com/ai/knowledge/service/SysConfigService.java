package com.ai.knowledge.service;

import com.ai.knowledge.entity.SysConfig;
import com.ai.knowledge.repository.SysConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class SysConfigService {

    @Autowired
    private SysConfigRepository configRepository;

    public List<SysConfig> getAllConfigs() {
        return configRepository.findAll();
    }

    public Optional<SysConfig> getConfigByKey(String key) {
        return configRepository.findByConfigKey(key);
    }

    public String getConfigValue(String key, String defaultValue) {
        return configRepository.findByConfigKey(key)
                .map(SysConfig::getConfigValue)
                .orElse(defaultValue);
    }

    public SysConfig createConfig(SysConfig config) {
        if (configRepository.existsByConfigKey(config.getConfigKey())) {
            throw new RuntimeException("配置键已存在");
        }
        return configRepository.save(config);
    }

    public SysConfig updateConfig(String key, String value) {
        SysConfig config = configRepository.findByConfigKey(key)
                .orElseThrow(() -> new RuntimeException("配置不存在"));
        config.setConfigValue(value);
        return configRepository.save(config);
    }

    public SysConfig updateConfig(SysConfig config) {
        SysConfig existing = configRepository.findByConfigKey(config.getConfigKey())
                .orElseThrow(() -> new RuntimeException("配置不存在"));
        existing.setConfigValue(config.getConfigValue());
        existing.setConfigDesc(config.getConfigDesc());
        return configRepository.save(existing);
    }

    public void deleteConfig(String key) {
        SysConfig config = configRepository.findByConfigKey(key)
                .orElseThrow(() -> new RuntimeException("配置不存在"));
        configRepository.delete(config);
    }
}
