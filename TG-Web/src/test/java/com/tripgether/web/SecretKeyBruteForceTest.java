package com.tripgether.web;

import okhttp3.*;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 무차별 대입 방식으로 SECRET_KEY 찾기
 *
 * 전략:
 * 1. app.js에서 모든 64자 hex 문자열 추출
 * 2. 알려진 요청 데이터로 각 후보 테스트
 * 3. 일치하는 서명을 생성하는 SECRET_KEY 찾기
 */
public class SecretKeyBruteForceTest {

    // 알려진 요청 데이터 (analyze_signature.py에서)
    private static class KnownRequest {
        String url;
        long ts;
        long _ts;
        int _tsc;
        String expectedSignature;

        KnownRequest(String url, long ts, long _ts, int _tsc, String expectedSig) {
            this.url = url;
            this.ts = ts;
            this._ts = _ts;
            this._tsc = _tsc;
            this.expectedSignature = expectedSig;
        }
    }

    private static final KnownRequest REQUEST1 = new KnownRequest(
            "https://www.instagram.com/p/DQHEMGPEqWK/?img_index=3&igsh=MWU5YTM3b2JpZzJp",
            1762275012493L,
            1761979938888L,
            0,
            "3b5cded7def76fc0752b1f1c4aab36635afa80d67756e2b8b96feed1dd2a5b10"
    );

    private static final KnownRequest REQUEST2 = new KnownRequest(
            "https://www.instagram.com/p/DQHEMGPEqWK/?img_index=3&igsh=MWU5YTM3b2JpZzJp",
            1762276899799L,
            1761979938888L,
            0,
            "6e243ef16779656119c7b8196c88673d988292ea4e84a8fb2f76c655b8995d33"
    );

    @Test
    void testBruteForceSecretKey() throws Exception {
        System.out.println("=" + "=".repeat(69));
        System.out.println("🔍 SECRET_KEY 무차별 대입 테스트");
        System.out.println("=" + "=".repeat(69));

        // Step 1: JavaScript 다운로드
        System.out.println("\n📥 Step 1: Downloading JavaScript...");
        String jsContent = downloadJavaScript();
        System.out.println("Downloaded " + jsContent.length() + " bytes");

        // Step 2: 모든 64자 hex 문자열 추출
        System.out.println("\n🔍 Step 2: Extracting hex candidates...");
        Set<String> hexCandidates = extractHexStrings(jsContent);
        System.out.println("Found " + hexCandidates.size() + " unique 64-char hex strings");

        if (hexCandidates.isEmpty()) {
            System.out.println("⚠️  No hex strings found. SECRET_KEY might be obfuscated differently.");
            return;
        }

        // Step 3: 각 후보를 테스트
        System.out.println("\n🧪 Step 3: Testing each candidate...");
        Context jsContext = Context.newBuilder("js")
                .allowAllAccess(true)
                .build();

        // SHA-256 함수 정의
        String sha256Function = """
                function sha256(input) {
                    var bytes = java.lang.String(input).getBytes("UTF-8");
                    var digest = java.security.MessageDigest.getInstance("SHA-256");
                    var hashBytes = digest.digest(bytes);
                    var hexString = "";
                    for (var i = 0; i < hashBytes.length; i++) {
                        var hex = (hashBytes[i] & 0xff).toString(16);
                        if (hex.length === 1) hexString += "0";
                        hexString += hex;
                    }
                    return hexString;
                }

                function testSignature(url, ts, secret) {
                    return sha256(url + ts + secret);
                }
                """;

        jsContext.eval("js", sha256Function);
        Value testFunction = jsContext.getBindings("js").getMember("testSignature");

        int tested = 0;
        for (String candidate : hexCandidates) {
            tested++;
            if (tested % 10 == 0) {
                System.out.print(".");
                if (tested % 100 == 0) {
                    System.out.println(" [" + tested + "/" + hexCandidates.size() + "]");
                }
            }

            // Request 1로 테스트
            String generated1 = testFunction.execute(REQUEST1.url, REQUEST1.ts, candidate).asString();

            if (generated1.equals(REQUEST1.expectedSignature)) {
                System.out.println("\n\n🎉🎉🎉 SECRET_KEY FOUND! 🎉🎉🎉");
                System.out.println("Secret: " + candidate);
                System.out.println("\n✅ Request 1 verification:");
                System.out.println("Generated: " + generated1);
                System.out.println("Expected:  " + REQUEST1.expectedSignature);
                System.out.println("Match: ✅");

                // Request 2로도 검증
                String generated2 = testFunction.execute(REQUEST2.url, REQUEST2.ts, candidate).asString();
                System.out.println("\n✅ Request 2 verification:");
                System.out.println("Generated: " + generated2);
                System.out.println("Expected:  " + REQUEST2.expectedSignature);
                System.out.println("Match: " + (generated2.equals(REQUEST2.expectedSignature) ? "✅" : "❌"));

                if (generated2.equals(REQUEST2.expectedSignature)) {
                    System.out.println("\n✅✅✅ BOTH REQUESTS VERIFIED! This is the correct SECRET_KEY! ✅✅✅");
                    jsContext.close();
                    return;
                }
            }
        }

        System.out.println("\n\n❌ SECRET_KEY not found in extracted hex strings");
        System.out.println("This means the key is either:");
        System.out.println("1. Generated dynamically (not hardcoded)");
        System.out.println("2. Obfuscated in a non-hex format");
        System.out.println("3. Split across multiple variables");
        System.out.println("4. Stored in a different file");

        jsContext.close();
    }

    private String downloadJavaScript() throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://sssinstagram.com/js/app.js")
                .addHeader("User-Agent", "Mozilla/5.0")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to download: " + response.code());
            }
            return response.body().string();
        }
    }

    private Set<String> extractHexStrings(String content) {
        Set<String> results = new HashSet<>();

        // Pattern: 64 consecutive hex characters (with or without quotes)
        Pattern pattern = Pattern.compile("[\"']?([0-9a-f]{64})[\"']?");
        Matcher matcher = pattern.matcher(content);

        while (matcher.find()) {
            String hex = matcher.group(1);
            results.add(hex);
        }

        System.out.println("\nSample candidates:");
        int count = 0;
        for (String hex : results) {
            if (count++ < 5) {
                System.out.println("  " + hex.substring(0, 20) + "...");
            }
        }

        return results;
    }

    @Test
    void testWithHardcodedSecret() throws Exception {
        System.out.println("🧪 Testing with previously known SECRET_KEY...");

        String SECRET_KEY = "99ee6bfc1cf8b0893baa4b8fe9e0ec780ce195b01d00019a121a05593ab9b5ee";

        Context jsContext = Context.newBuilder("js").allowAllAccess(true).build();
        String sha256Function = """
                function sha256(input) {
                    var bytes = java.lang.String(input).getBytes("UTF-8");
                    var digest = java.security.MessageDigest.getInstance("SHA-256");
                    var hashBytes = digest.digest(bytes);
                    var hexString = "";
                    for (var i = 0; i < hashBytes.length; i++) {
                        var hex = (hashBytes[i] & 0xff).toString(16);
                        if (hex.length === 1) hexString += "0";
                        hexString += hex;
                    }
                    return hexString;
                }
                """;

        jsContext.eval("js", sha256Function);
        Value sha256Func = jsContext.getBindings("js").getMember("sha256");

        String input1 = REQUEST1.url + REQUEST1.ts + SECRET_KEY;
        String result1 = sha256Func.execute(input1).asString();

        System.out.println("\nRequest 1:");
        System.out.println("Input: " + REQUEST1.url + " + " + REQUEST1.ts + " + " + SECRET_KEY.substring(0, 20) + "...");
        System.out.println("Generated: " + result1);
        System.out.println("Expected:  " + REQUEST1.expectedSignature);
        System.out.println("Match: " + (result1.equals(REQUEST1.expectedSignature) ? "✅" : "❌"));

        jsContext.close();
    }
}
