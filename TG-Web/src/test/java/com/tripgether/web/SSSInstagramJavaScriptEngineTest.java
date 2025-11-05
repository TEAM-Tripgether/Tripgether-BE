package com.tripgether.web;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import okhttp3.*;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * GraalVM JavaScript 엔진 테스트: JavaScript 다운로드 및 서명 생성 함수 실행
 *
 * ✅ 목적:
 *    - sssinstagram.com의 JavaScript 파일을 다운로드
 *    - GraalVM을 사용하여 Java 내에서 JavaScript 실행
 *    - 동적 서명(_s) 생성 함수 호출
 *    - 생성된 서명으로 API 호출 성공 검증
 *
 * 📋 실행 플로우:
 *    1. 페이지 HTML 다운로드 → JavaScript 파일 URL 추출
 *    2. JavaScript 파일 다운로드
 *    3. GraalVM Context 생성 및 JavaScript 로드
 *    4. 서명 생성 함수 호출
 *    5. 생성된 서명으로 /api/convert 호출 테스트
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SSSInstagramJavaScriptEngineTest {

    private OkHttpClient client;
    private Gson gson;
    private Context jsContext;

    private static final String BASE_URL = "https://sssinstagram.com";
    private static final String PAGE_URL = BASE_URL + "/ko";
    private static final String INSTAGRAM_POST_URL = "https://www.instagram.com/p/DQHEMGPEqWK/?img_index=3&igsh=MWU5YTM3b2JpZzJp";

    @BeforeAll
    void setupClass() {
        System.out.println("=================================================");
        System.out.println("🚀 GraalVM JavaScript 엔진 테스트 초기화");
        System.out.println("=================================================\n");

        gson = new GsonBuilder().setPrettyPrinting().create();

        // OkHttpClient 초기화
        client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .followRedirects(true)
                .followSslRedirects(true)
                .build();

        System.out.println("✅ OkHttpClient 초기화 완료");
        System.out.println("✅ Gson 초기화 완료\n");
    }

    @Test
    @Order(1)
    @DisplayName("Step 1: 페이지 HTML 다운로드 및 JavaScript URL 추출")
    void testDownloadPageHtml() throws IOException {
        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("📍 Step 1: 페이지 HTML 다운로드");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

        Request request = new Request.Builder()
                .url(PAGE_URL)
                .get()
                .addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36")
                .build();

        try (Response response = client.newCall(request).execute()) {
            String htmlContent = response.body().string();

            System.out.println("✅ 페이지 다운로드 성공");
            System.out.println("📄 HTML 크기: " + htmlContent.length() + " bytes");

            // JavaScript 파일 URL 추출 (예: <script src="/static/js/main.abc123.js">)
            // 실제 구현 시 정규식 또는 HTML 파서 사용
            System.out.println("\n🔍 JavaScript 파일 URL 추출 중...");

            // 간단한 패턴 매칭 (실제로는 HTML 파서 사용 권장)
            if (htmlContent.contains("<script") && htmlContent.contains("src=")) {
                System.out.println("✅ JavaScript 파일 참조 발견");
                // 실제 URL 추출은 다음 단계에서 구현
            } else {
                System.out.println("⚠️  JavaScript 파일 참조를 찾을 수 없습니다");
            }

            Assertions.assertEquals(200, response.code(), "페이지 다운로드 실패");
        }
    }

    @Test
    @Order(2)
    @DisplayName("Step 2: GraalVM JavaScript 엔진 초기화 및 간단한 실행 테스트")
    void testGraalVMJavaScriptEngine() {
        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("📍 Step 2: GraalVM JavaScript 엔진 테스트");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

        // GraalVM Context 생성
        jsContext = Context.newBuilder("js")
                .allowAllAccess(true)
                .option("js.ecmascript-version", "2022")
                .build();

        System.out.println("✅ GraalVM JavaScript Context 생성 완료");

        // 간단한 JavaScript 실행 테스트
        System.out.println("\n🧪 JavaScript 실행 테스트:");

        // Test 1: 간단한 계산
        Value result1 = jsContext.eval("js", "2 + 3");
        System.out.println("   Test 1 - 2 + 3 = " + result1.asInt());
        Assertions.assertEquals(5, result1.asInt());

        // Test 2: 함수 정의 및 호출
        jsContext.eval("js", "function add(a, b) { return a + b; }");
        Value addFunction = jsContext.getBindings("js").getMember("add");
        Value result2 = addFunction.execute(10, 20);
        System.out.println("   Test 2 - add(10, 20) = " + result2.asInt());
        Assertions.assertEquals(30, result2.asInt());

        // Test 3: 객체 생성 및 접근
        jsContext.eval("js", "var obj = { name: 'test', value: 42 };");
        Value obj = jsContext.getBindings("js").getMember("obj");
        System.out.println("   Test 3 - obj.name = " + obj.getMember("name").asString());
        System.out.println("   Test 3 - obj.value = " + obj.getMember("value").asInt());
        Assertions.assertEquals("test", obj.getMember("name").asString());

        System.out.println("\n✅ GraalVM JavaScript 엔진 정상 작동 확인");
    }

    @Test
    @Order(3)
    @DisplayName("Step 3: SHA-256 해시 함수 시뮬레이션 테스트")
    void testJavaScriptHashFunction() {
        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("📍 Step 3: JavaScript 해시 함수 시뮬레이션");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

        // 실제 sssinstagram.com의 서명 생성 로직을 시뮬레이션
        // (실제 구현은 브라우저에서 JavaScript를 추출한 후 적용)

        String mockSignatureFunction = """
            function generateSignature(url, ts, _ts, _tsc) {
                // 이것은 시뮬레이션입니다.
                // 실제 서명 생성 로직은 sssinstagram.com의 JavaScript에서 가져와야 합니다.

                // 간단한 해시 시뮬레이션 (실제로는 복잡한 알고리즘)
                var input = url + ts + _ts + _tsc + "secret_key";
                var hash = 0;
                for (var i = 0; i < input.length; i++) {
                    var char = input.charCodeAt(i);
                    hash = ((hash << 5) - hash) + char;
                    hash = hash & hash; // Convert to 32bit integer
                }

                // 64자 hex 문자열로 변환 (실제로는 SHA-256 사용)
                var hexHash = Math.abs(hash).toString(16).padStart(16, '0');
                return hexHash.repeat(4).substring(0, 64);
            }
            """;

        jsContext.eval("js", mockSignatureFunction);
        System.out.println("✅ 서명 생성 함수 로드 완료");

        // 함수 호출 테스트
        Value generateSignature = jsContext.getBindings("js").getMember("generateSignature");

        String testUrl = INSTAGRAM_POST_URL;
        long testTs = System.currentTimeMillis();
        long test_Ts = 1761979938888L;
        int test_Tsc = 0;

        Value signature = generateSignature.execute(testUrl, testTs, test_Ts, test_Tsc);
        String signatureStr = signature.asString();

        System.out.println("\n📊 생성된 서명:");
        System.out.println("   입력 URL: " + testUrl);
        System.out.println("   입력 ts: " + testTs);
        System.out.println("   입력 _ts: " + test_Ts);
        System.out.println("   입력 _tsc: " + test_Tsc);
        System.out.println("   출력 서명: " + signatureStr);

        // 서명 길이 검증 (SHA-256 hex는 64자)
        Assertions.assertEquals(64, signatureStr.length(), "서명 길이가 64자가 아닙니다");
        System.out.println("\n✅ 서명 생성 함수 정상 작동 (시뮬레이션)");
    }

    @Test
    @Order(4)
    @DisplayName("Step 4: 실제 API 호출 플로우 시뮬레이션")
    void testCompleteAPIFlowWithJavaScript() throws IOException {
        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("📍 Step 4: 전체 API 플로우 (JavaScript 서명 포함)");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

        // Step 1: GET /msec
        System.out.println("🔹 Step 4-1: msec 값 조회");
        Request msecRequest = new Request.Builder()
                .url(BASE_URL + "/msec")
                .get()
                .addHeader("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36")
                .build();

        Double msecValue;
        try (Response msecResponse = client.newCall(msecRequest).execute()) {
            String msecBody = msecResponse.body().string();
            JsonObject msecJson = gson.fromJson(msecBody, JsonObject.class);
            msecValue = msecJson.get("msec").getAsDouble();
            System.out.println("   ✅ msec 값: " + msecValue);
        }

        // Step 2: JavaScript로 서명 생성
        System.out.println("\n🔹 Step 4-2: JavaScript로 서명 생성");
        long ts = (long) (msecValue * 1000);
        long _ts = 1761979938888L;
        int _tsc = 0;

        Value generateSignature = jsContext.getBindings("js").getMember("generateSignature");
        Value signatureValue = generateSignature.execute(INSTAGRAM_POST_URL, ts, _ts, _tsc);
        String signature = signatureValue.asString();

        System.out.println("   ✅ 생성된 서명: " + signature);

        // Step 3: POST /api/convert (시뮬레이션된 서명으로 테스트)
        System.out.println("\n🔹 Step 4-3: /api/convert API 호출");
        JsonObject payload = new JsonObject();
        payload.addProperty("url", INSTAGRAM_POST_URL);
        payload.addProperty("ts", ts);
        payload.addProperty("_s", signature);
        payload.addProperty("_ts", _ts);
        payload.addProperty("_tsc", _tsc);

        RequestBody requestBody = RequestBody.create(
                gson.toJson(payload),
                MediaType.parse("application/json; charset=utf-8")
        );

        Request convertRequest = new Request.Builder()
                .url(BASE_URL + "/api/convert")
                .post(requestBody)
                .addHeader("accept", "application/json, text/plain, */*")
                .addHeader("content-type", "application/json")
                .addHeader("origin", BASE_URL)
                .addHeader("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36")
                .build();

        try (Response convertResponse = client.newCall(convertRequest).execute()) {
            System.out.println("   📊 응답 코드: " + convertResponse.code());
            String convertBody = convertResponse.body().string();
            System.out.println("   📦 응답 본문 (일부): " + convertBody.substring(0, Math.min(200, convertBody.length())));

            // 현재는 시뮬레이션된 서명이므로 401 에러가 예상됨
            if (convertResponse.code() == 401) {
                System.out.println("\n⚠️  예상된 401 에러 (시뮬레이션된 서명 사용)");
                System.out.println("   💡 다음 단계: 실제 JavaScript 파일에서 서명 생성 로직 추출 필요");
            } else if (convertResponse.code() == 200) {
                System.out.println("\n✅ 성공! 서명이 올바르게 생성되었습니다");
            }
        }
    }

    @AfterAll
    void tearDown() {
        System.out.println("\n=================================================");
        System.out.println("🏁 테스트 종료");
        System.out.println("=================================================");

        if (jsContext != null) {
            jsContext.close();
            System.out.println("✅ GraalVM JavaScript Context 정리 완료");
        }

        if (client != null) {
            client.dispatcher().executorService().shutdown();
            client.connectionPool().evictAll();
            System.out.println("✅ OkHttpClient 리소스 정리 완료");
        }

        System.out.println("\n📖 다음 단계:");
        System.out.println("   1. 브라우저 개발자 도구로 JavaScript 파일 다운로드");
        System.out.println("   2. 서명 생성 함수 찾기 (검색 키워드: '_s', 'signature', 'hash')");
        System.out.println("   3. 해당 함수를 위 테스트의 generateSignature()에 복사");
        System.out.println("   4. 테스트 재실행하여 200 OK 응답 확인");
    }
}
