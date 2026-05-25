package com.ai.knowledge.service;

import com.ai.knowledge.entity.SysUser;
import com.ai.knowledge.repository.SysUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UserService {
    
    @Autowired
    private SysUserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    public List<SysUser> getAllUsers() {
        return userRepository.findAll();
    }
    
    public SysUser getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }
    
    public SysUser createUser(SysUser user, Long createUser) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setCreateUser(createUser);
        user.setStatus(1);
        
        return userRepository.save(user);
    }
    
    public SysUser updateUser(Long id, SysUser updateUser) {
        SysUser user = getUserById(id);
        
        if (updateUser.getNickname() != null) {
            user.setNickname(updateUser.getNickname());
        }
        if (updateUser.getRoleId() != null) {
            user.setRoleId(updateUser.getRoleId());
        }
        if (updateUser.getStatus() != null) {
            user.setStatus(updateUser.getStatus());
        }
        
        return userRepository.save(user);
    }
    
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
    
    public void resetPassword(Long id, String newPassword) {
        SysUser user = getUserById(id);
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
    
    public void updateStatus(Long id, Integer status) {
        SysUser user = getUserById(id);
        user.setStatus(status);
        userRepository.save(user);
    }
}
