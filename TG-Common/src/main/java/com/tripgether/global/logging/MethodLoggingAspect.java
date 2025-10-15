package com.tripgether.global.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

@Slf4j
@Aspect
@Component
public class MethodLoggingAspect {

    private final ThreadLocal<StringBuilder> flowBuilder = ThreadLocal.withInitial(StringBuilder::new);
    private final ThreadLocal<Integer> depth = ThreadLocal.withInitial(() -> 0);

    /**
     * Controller, Service, Repository 메소드 통합 로깅
     */
    @Around("execution(* com.tripgether.domain.*.controller.*.*(..)) || " +
            "execution(* com.tripgether.domain.*.service.*.*(..)) || " +
            "execution(* com.tripgether.domain.*.repository.*.*(..))")
    public Object logMethods(ProceedingJoinPoint joinPoint) throws Throwable {

        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        String layer = determineLayer(className);

        int currentDepth = depth.get();
        StringBuilder flow = flowBuilder.get();

        StopWatch stopWatch = new StopWatch();

        try {
            // 시작 로그
            if (currentDepth == 0) {
                flow.setLength(0);
                log.info("🚀 {} {}.{}()", getLayerIcon(layer), className, methodName);
                flow.append(className).append(".").append(methodName);
            } else {
                String indent = "  ".repeat(currentDepth);
                log.info("{}↳ {} {}.{}()", indent, getLayerIcon(layer), className, methodName);
                flow.append(" → ").append(className).append(".").append(methodName);
            }

            depth.set(currentDepth + 1);
            stopWatch.start();

            Object result = joinPoint.proceed();

            stopWatch.stop();
            long executionTime = stopWatch.getTotalTimeMillis();

            // 완료 로그
            String indent = "  ".repeat(currentDepth);
            if (executionTime > 100) {
                log.warn("{}⚡ {} {}.{}() 완료 [{}ms] 🐌", indent, getLayerIcon(layer), className, methodName, executionTime);
            } else {
                log.info("{}✅ {} {}.{}() 완료 [{}ms]", indent, getLayerIcon(layer), className, methodName, executionTime);
            }

            // 최상위에서 전체 흐름 출력
            if (currentDepth == 0) {
                log.info("📋 실행 흐름: {}", flow.toString());
                log.info("═".repeat(80));
            }

            return result;

        } catch (Exception e) {
            stopWatch.stop();
            String indent = "  ".repeat(currentDepth);
            log.error("{}❌ {} {}.{}() 실패 [{}ms]: {}",
                    indent, getLayerIcon(layer), className, methodName,
                    stopWatch.getTotalTimeMillis(), e.getMessage());

            if (currentDepth == 0) {
                log.error("💥 에러 흐름: {}", flow.toString());
                log.error("═".repeat(80));
            }

            throw e;
        } finally {
            depth.set(currentDepth);
            if (currentDepth == 0) {
                depth.remove();
                flowBuilder.remove();
            }
        }
    }

    /**
     * 클래스명으로 계층 판단
     */
    private String determineLayer(String className) {
        if (className.contains("Controller")) return "CONTROLLER";
        if (className.contains("Service")) return "SERVICE";
        if (className.contains("Repository") || className.contains("Proxy")) return "REPOSITORY";
        return "UNKNOWN";
    }

    /**
     * 계층별 아이콘 반환
     */
    private String getLayerIcon(String layer) {
        return switch (layer) {
            case "CONTROLLER" -> "🎯";
            case "SERVICE" -> "⚙️";
            case "REPOSITORY" -> "💾";
            default -> "📦";
        };
    }

    /**
     * @LogExecutionTime 어노테이션 처리
     */
    @Around("@annotation(logExecutionTime)")
    public Object logAnnotatedMethods(ProceedingJoinPoint joinPoint, LogExecutionTime logExecutionTime) throws Throwable {
        String description = logExecutionTime.description().isEmpty() ?
                joinPoint.getSignature().getName() : logExecutionTime.description();
        long threshold = logExecutionTime.threshold();

        StopWatch stopWatch = new StopWatch();

        try {
            log.info("⏱️ {} 시작", description);

            stopWatch.start();
            Object result = joinPoint.proceed();
            stopWatch.stop();

            long executionTime = stopWatch.getTotalTimeMillis();
            if (executionTime > threshold) {
                log.warn("🐌 {} 완료 [{}ms] (임계값: {}ms 초과)", description, executionTime, threshold);
            } else {
                log.info("⚡ {} 완료 [{}ms]", description, executionTime);
            }

            return result;

        } catch (Exception e) {
            stopWatch.stop();
            log.error("💥 {} 실패 [{}ms]: {}", description, stopWatch.getTotalTimeMillis(), e.getMessage());
            throw e;
        }
    }
}
