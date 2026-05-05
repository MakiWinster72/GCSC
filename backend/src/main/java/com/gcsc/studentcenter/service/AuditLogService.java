package com.gcsc.studentcenter.service;

import com.gcsc.studentcenter.entity.AuditLog;
import com.gcsc.studentcenter.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class AuditLogService {

  private static final Logger logger = LoggerFactory.getLogger(AuditLogService.class);

  private final AuditLogRepository auditLogRepository;

  public AuditLogService(AuditLogRepository auditLogRepository) {
    this.auditLogRepository = auditLogRepository;
  }

  @Transactional
  public AuditLog log(String username, String action, String detail, String ipAddress) {
    AuditLog entry = new AuditLog(username, action, detail, ipAddress, LocalDateTime.now());
    AuditLog saved = auditLogRepository.save(entry);
    logger.info("AUDIT [{}] {}: {} (IP: {})", username, action, detail, ipAddress);
    return saved;
  }

  @Transactional(readOnly = true)
  public Map<String, Object> listPaginated(int page, int size, String search) {
    Pageable pageable = PageRequest.of(page - 1, size);
    String searchParam = (search != null && !search.isBlank()) ? search.trim() : null;
    var result = auditLogRepository.findPaginated(searchParam, pageable);
    Map<String, Object> response = new LinkedHashMap<>();
    response.put("data", result.getContent());
    response.put("total", result.getTotalElements());
    response.put("page", page);
    response.put("size", size);
    response.put("pages", result.getTotalPages());
    return response;
  }

  public String resolveIpAddress(HttpServletRequest request) {
    String xForwardedFor = request.getHeader("X-Forwarded-For");
    if (xForwardedFor != null && !xForwardedFor.isBlank()) {
      return xForwardedFor.split(",")[0].trim();
    }
    String xRealIp = request.getHeader("X-Real-IP");
    if (xRealIp != null && !xRealIp.isBlank()) {
      return xRealIp;
    }
    return request.getRemoteAddr();
  }
}
