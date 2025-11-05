package com.tripgether.web;

import okhttp3.*;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 세션 기반 Instagram 다운로더
 *
 * 전략: 브라우저처럼 세션을 유지하면서 JavaScript 변수 추출
 * 1. sssinstagram.com 메인 페이지 로드 (쿠키/세션 획득)
 * 2. HTML/JavaScript에서 SECRET_KEY 추출 시도
 * 3. 추출한 SECRET_KEY로 서명 생성
 * 4. 동일 세션으로 API 호출
 */
public class SessionBasedInstagramDownloader {

    private final OkHttpClient client;
    private final CookieJar cookieJar;
    private String secretKey = null;

    public SessionBasedInstagramDownloader() {
        // 쿠키를 자동으로 유지하는 CookieJar 설정
        this.cookieJar = new CookieJar() {
            private final java.util.HashMap<String, java.util.List<Cookie>> cookieStore = new java.util.HashMap<>();

            @Override
            public void saveFromResponse(HttpUrl url, java.util.List<Cookie> cookies) {
                cookieStore.put(url.host(), cookies);
            }

            @Override
            public java.util.List<Cookie> loadForRequest(HttpUrl url) {
                java.util.List<Cookie> cookies = cookieStore.get(url.host());
                return cookies != null ? cookies : new java.util.ArrayList<>();
            }
        };

        this.client = new OkHttpClient.Builder()
                .cookieJar(cookieJar)
                .followRedirects(true)
                .followSslRedirects(true)
                .build();
    }

    /**
     * 방법 1: HTML/JavaScript에서 SECRET_KEY 추출 시도
     */
    public boolean initializeSession() throws IOException {
        System.out.println("🔄 세션 초기화 중...");

        Request request = new Request.Builder()
                .url("https://sssinstagram.com")
                .addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36")
                .addHeader("Accept", "text/html,application/xhtml+xml,application/xml")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                System.out.println("❌ 메인 페이지 로드 실패: " + response.code());
                return false;
            }

            String html = response.body().string();
            System.out.println("✅ 메인 페이지 로드 성공 (쿠키 획득)");

            // JavaScript에서 SECRET_KEY 추출 시도
            secretKey = extractSecretKeyFromHtml(html);

            if (secretKey != null) {
                System.out.println("✅ SECRET_KEY 추출 성공: " + secretKey.substring(0, 20) + "...");
                return true;
            } else {
                System.out.println("⚠️  HTML에서 SECRET_KEY를 찾지 못함");

                // JavaScript 파일들 확인 (간단한 정규식으로)
                Pattern scriptPattern = Pattern.compile("<script[^>]+src=[\"']([^\"']+)[\"']");
                Matcher scriptMatcher = scriptPattern.matcher(html);
                System.out.println("  JavaScript 파일들:");
                while (scriptMatcher.find()) {
                    System.out.println("  - " + scriptMatcher.group(1));
                }

                return false;
            }
        }
    }

    /**
     * HTML/JavaScript에서 SECRET_KEY 패턴 찾기
     */
    private String extractSecretKeyFromHtml(String html) {
        // 패턴 1: 변수 할당 형태
        // var SECRET_KEY = "99ee6bfc..."
        Pattern pattern1 = Pattern.compile("SECRET_KEY\\s*[=:]\\s*[\"']([0-9a-f]{64})[\"']");
        Matcher matcher1 = pattern1.matcher(html);
        if (matcher1.find()) {
            return matcher1.group(1);
        }

        // 패턴 2: 객체 속성 형태
        // {secretKey: "99ee6bfc..."}
        Pattern pattern2 = Pattern.compile("[\"']?secretKey[\"']?\\s*:\\s*[\"']([0-9a-f]{64})[\"']");
        Matcher matcher2 = pattern2.matcher(html);
        if (matcher2.find()) {
            return matcher2.group(2);
        }

        // 패턴 3: 64자 hex 문자열 (매우 일반적)
        Pattern pattern3 = Pattern.compile("[\"']([0-9a-f]{64})[\"']");
        Matcher matcher3 = pattern3.matcher(html);
        if (matcher3.find()) {
            String candidate = matcher3.group(1);
            // 너무 많을 수 있으므로 첫 번째만 시도
            System.out.println("  - 후보 SECRET_KEY 발견: " + candidate.substring(0, 20) + "...");
            return candidate;
        }

        return null;
    }

    /**
     * 방법 2: 브라우저 행동 모방 - 실제 다운로드 시도
     */
    public String downloadWithBrowserBehavior(String instagramUrl) throws IOException {
        if (secretKey == null) {
            throw new IllegalStateException("먼저 initializeSession()을 호출하세요");
        }

        System.out.println("\n📥 다운로드 시도 중...");
        System.out.println("  Instagram URL: " + instagramUrl);

        // SignatureGenerator로 서명 생성
        SignatureGenerator generator = new SignatureGenerator();
        long ts = System.currentTimeMillis();

        // SECRET_KEY를 동적으로 설정할 수 있도록 수정 필요
        // 현재는 하드코딩된 값 사용
        String signature = generator.generateSignature(instagramUrl, ts);

        // API 요청
        String json = String.format(
            "{\"url\":\"%s\",\"ts\":%d,\"_ts\":%d,\"_tsc\":0,\"_s\":\"%s\"}",
            instagramUrl, ts, ts, signature
        );

        RequestBody body = RequestBody.create(
            json,
            MediaType.get("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url("https://sssinstagram.com/api/convert")
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36")
                .addHeader("Accept", "application/json")
                .addHeader("Origin", "https://sssinstagram.com")
                .addHeader("Referer", "https://sssinstagram.com/")
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "null";

            System.out.println("  상태 코드: " + response.code());
            System.out.println("  응답: " + responseBody);

            if (response.isSuccessful()) {
                System.out.println("✅ 다운로드 성공!");
                return responseBody;
            } else {
                System.out.println("❌ 다운로드 실패");
                return null;
            }
        }
    }

    /**
     * 방법 3: JavaScript 파일 직접 다운로드 및 분석
     */
    public boolean fetchAndAnalyzeJavaScript() throws IOException {
        System.out.println("\n🔍 JavaScript 파일 분석 중...");

        // 알려진 JavaScript 파일들
        String[] jsFiles = {
            "https://sssinstagram.com/assets/app.js",
            "https://sssinstagram.com/assets/link.chunk.js",
            "https://sssinstagram.com/js/app.js",
            "https://sssinstagram.com/js/main.js"
        };

        for (String jsUrl : jsFiles) {
            try {
                Request request = new Request.Builder()
                        .url(jsUrl)
                        .addHeader("User-Agent", "Mozilla/5.0")
                        .build();

                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    String jsContent = response.body().string();
                    System.out.println("✅ JavaScript 파일 다운로드 성공: " + jsUrl);
                    System.out.println("   크기: " + jsContent.length() + " bytes");

                    // SECRET_KEY 패턴 찾기
                    String foundKey = extractSecretKeyFromHtml(jsContent);
                    if (foundKey != null) {
                        secretKey = foundKey;
                        System.out.println("✅ SECRET_KEY 발견!");
                        return true;
                    }
                }
            } catch (IOException e) {
                System.out.println("⚠️  " + jsUrl + " 접근 실패");
            }
        }

        return false;
    }

    public String getSecretKey() {
        return secretKey;
    }
}
