package com.hscmt.common.comp;

import com.hscmt.simulation.common.jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AuditingComp implements AuditorAware<String> {

    private final JwtTokenProvider provider;

    @Override
    public Optional<String> getCurrentAuditor() {

        // 2) 웹 요청이 있으면 Authorization 헤더에서 추출 (없으면 건너뜀)
        String fromRequest = currentUserFromRequest();
        if (fromRequest != null) {
            return Optional.of(fromRequest);
        }

        // 3) 배치/Quartz/비동기 등 요청/보안컨텍스트가 없을 때
        return Optional.of("BATCH_SYSTEM");
    }

    private String currentUserFromRequest() {
        try {
            RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
            if (!(attrs instanceof ServletRequestAttributes sra)) {
                return null;
            }
            HttpServletRequest request = sra.getRequest();
            String header = request.getHeader("Authorization");
            if (header == null || header.isBlank()) {
                return null;
            }
            String sub = provider.getSubject(header);
            return (sub == null || sub.isBlank()) ? null : sub;
        } catch (IllegalStateException ignore) {
            // No thread-bound request (배치/Quartz 등) → 조용히 무시
            return null;
        }
    }
}