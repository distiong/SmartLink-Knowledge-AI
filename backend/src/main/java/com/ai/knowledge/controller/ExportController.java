package com.ai.knowledge.controller;

import com.ai.knowledge.entity.SysUser;
import com.ai.knowledge.repository.SysUserRepository;
import com.ai.knowledge.security.JwtAuthenticationFilter;
import com.ai.knowledge.service.ExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/export")
@CrossOrigin(origins = "*")
public class ExportController {

    @Autowired
    private ExportService exportService;

    @Autowired
    private SysUserRepository userRepository;

    @GetMapping("/chat/excel")
    public ResponseEntity<byte[]> exportChatToExcel() {
        try {
            JwtAuthenticationFilter.UserInfo currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.badRequest().build();
            }

            SysUser user = userRepository.findById(currentUser.getUserId()).orElse(null);
            boolean isAdmin = user != null && user.getUserType() == 1;

            byte[] data = exportService.exportToExcel(currentUser.getUserId(), isAdmin);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "chat_records.xlsx");
            headers.setContentLength(data.length);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(data);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/chat/word")
    public ResponseEntity<byte[]> exportChatToWord() {
        try {
            JwtAuthenticationFilter.UserInfo currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.badRequest().build();
            }

            SysUser user = userRepository.findById(currentUser.getUserId()).orElse(null);
            boolean isAdmin = user != null && user.getUserType() == 1;

            byte[] data = exportService.exportToWord(currentUser.getUserId(), isAdmin);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "chat_records.docx");
            headers.setContentLength(data.length);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(data);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/chat/pdf")
    public ResponseEntity<byte[]> exportChatToPdf() {
        try {
            JwtAuthenticationFilter.UserInfo currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.badRequest().build();
            }

            SysUser user = userRepository.findById(currentUser.getUserId()).orElse(null);
            boolean isAdmin = user != null && user.getUserType() == 1;

            byte[] data = exportService.exportToPdf(currentUser.getUserId(), isAdmin);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "chat_records.pdf");
            headers.setContentLength(data.length);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(data);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    private JwtAuthenticationFilter.UserInfo getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof JwtAuthenticationFilter.UserInfo) {
            return (JwtAuthenticationFilter.UserInfo) authentication.getPrincipal();
        }
        return null;
    }
}
