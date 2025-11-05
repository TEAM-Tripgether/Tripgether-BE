package com.tripgether.web;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Instagram 다운로더 API 서명 생성기
 *
 * sssinstagram.com의 /api/convert 엔드포인트에 필요한 서명을 생성합니다.
 *
 * 알고리즘 (Chrome DevTools 분석 결과):
 * 1. 입력 = URL + Timestamp + SECRET_KEY
 * 2. 서명 = SHA-256(입력)
 *
 * 서명 파라미터:
 * - url: Instagram URL
 * - ts: 현재 타임스탬프 (밀리초)
 * - _ts: 세션 시작 타임스탬프 (밀리초)
 * - _tsc: 요청 카운터 (0부터 시작)
 * - _s: SHA-256 서명 (64자 hex 문자열)
 */
public class SignatureGenerator {

    // Chrome DevTools에서 추출한 비밀키
    // 이 값은 sssinstagram.com의 JavaScript에 하드코딩되어 있음
    private static final String SECRET_KEY = "99ee6bfc1cf8b0893baa4b8fe9e0ec780ce195b01d00019a121a05593ab9b5ee";

    private final Context jsContext;
    private final Value signatureFunction;

    /**
     * 생성자 - GraalVM JavaScript 컨텍스트 초기화
     */
    public SignatureGenerator() {
        // GraalVM JavaScript 엔진 초기화
        this.jsContext = Context.newBuilder("js")
                .allowAllAccess(true)
                .option("js.ecmascript-version", "2022")
                .build();

        // 서명 생성 JavaScript 함수 로드
        // 알고리즘: SHA-256(url + timestamp + secret)
        String signatureScript = """
            function generateSignature(url, ts, secret) {
                // 입력 데이터 결합: URL + Timestamp + Secret
                const input = url + ts + secret;

                // SHA-256 해시 계산 (Java에서 제공)
                return javaHashFunction(input);
            }
            generateSignature;
            """;

        // Java SHA-256 함수를 JavaScript에 바인딩
        jsContext.getBindings("js").putMember("javaHashFunction",
            (java.util.function.Function<String, String>) this::sha256Hash);

        this.signatureFunction = jsContext.eval("js", signatureScript);
    }

    /**
     * 서명 생성 (간단한 버전)
     *
     * @param url Instagram URL
     * @param ts 현재 타임스탬프
     * @return SHA-256 서명 (64자 hex 문자열)
     */
    public String generateSignature(String url, long ts) {
        Value result = signatureFunction.execute(url, ts, SECRET_KEY);
        return result.asString();
    }

    /**
     * SHA-256 해시 계산
     *
     * @param input 입력 문자열
     * @return 64자 hex 문자열
     */
    private String sha256Hash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * 바이트 배열을 hex 문자열로 변환
     *
     * @param bytes 바이트 배열
     * @return hex 문자열
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder(2 * bytes.length);
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    /**
     * 전체 요청 데이터 생성
     *
     * @param url Instagram URL
     * @param sessionTs 세션 시작 타임스탬프
     * @return 서명이 포함된 요청 데이터
     */
    public SignatureData createSignedRequest(String url, long sessionTs) {
        long currentTs = System.currentTimeMillis();
        int requestCounter = 0; // 첫 요청

        String signature = generateSignature(url, currentTs);

        return new SignatureData(url, currentTs, signature, sessionTs, requestCounter);
    }

    /**
     * 전체 요청 데이터 생성 (새 세션)
     *
     * @param url Instagram URL
     * @return 서명이 포함된 요청 데이터
     */
    public SignatureData createSignedRequest(String url) {
        long currentTs = System.currentTimeMillis();
        return createSignedRequest(url, currentTs);
    }

    /**
     * 리소스 정리
     */
    public void close() {
        if (jsContext != null) {
            jsContext.close();
        }
    }

    /**
     * 서명 데이터 클래스
     */
    public static class SignatureData {
        public final String url;
        public final long ts;
        public final String _s;
        public final long _ts;
        public final int _tsc;

        public SignatureData(String url, long ts, String signature, long sessionTs, int requestCounter) {
            this.url = url;
            this.ts = ts;
            this._s = signature;
            this._ts = sessionTs;
            this._tsc = requestCounter;
        }

        @Override
        public String toString() {
            return String.format("SignatureData{url='%s', ts=%d, _s='%s', _ts=%d, _tsc=%d}",
                    url, ts, _s, _ts, _tsc);
        }
    }
}
