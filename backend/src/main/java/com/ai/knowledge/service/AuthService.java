package com.ai.knowledge.service;

import com.ai.knowledge.entity.SysUser;
import com.ai.knowledge.repository.SysUserRepository;
import com.ai.knowledge.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {
    
    @Autowired
    private SysUserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    public Map<String, Object> login(String username, String password) {
        SysUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        if (user.getStatus() == 0) {
            throw new RuntimeException("账号已被禁用");
        }
        
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("密码错误");
        }
        
        String token = JwtUtil.generateToken(user.getId(), user.getUsername(), user.getUserType());
        
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("nickname", user.getNickname());
        result.put("userType", user.getUserType());
        result.put("roleId", user.getRoleId());
        
        return result;
    }
    
    public Map<String, Object> register(String username, String password, String nickname, Long createUser) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("用户名已存在");
        }
        
        SysUser user = new SysUser();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setNickname(nickname);
        user.setRoleId(3L); // 默认普通访客角色
        user.setUserType(2);
        user.setStatus(1);
        user.setCreateUser(createUser);
        
        userRepository.save(user);
        
        Map<String, Object> result = new HashMap<>();
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        
        return result;
    }
    
    public SysUser getCurrentUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }
}
