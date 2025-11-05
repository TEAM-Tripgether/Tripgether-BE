package com.tripgether.web;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 서명 알고리즘 역공학 테스트
 *
 * 캡처한 실제 데이터를 사용하여 올바른 입력 조합 방식을 찾습니다.
 */
public class SignatureAlgorithmTest {

    // 캡처한 실제 데이터
    private static final String URL = "https://www.instagram.com/p/DQHEMGPEqWK/?img_index=3&igsh=MWU5YTM3b2JpZzJp";
    private static final long TS = 1762275012493L;
    private static final long SESSION_TS = 1761979938888L;
    private static final int COUNTER = 0;
    private static final String EXPECTED_SIGNATURE = "3b5cded7def76fc0752b1f1c4aab36635afa80d67756e2b8b96feed1dd2a5b10";

    @Test
    void testVariousInputCombinations() {
        System.out.println("=== Testing Various Input Combinations ===\n");

        // 조합 1: url + ts + _ts + _tsc
        testCombination("url + ts + _ts + _tsc",
                URL + TS + SESSION_TS + COUNTER);

        // 조합 2: url + "|" + ts + "|" + _ts + "|" + _tsc
        testCombination("url + \"|\" + ts + \"|\" + _ts + \"|\" + _tsc",
                URL + "|" + TS + "|" + SESSION_TS + "|" + COUNTER);

        // 조합 3: ts + url + _ts + _tsc
        testCombination("ts + url + _ts + _tsc",
                TS + URL + SESSION_TS + COUNTER);

        // 조합 4: url + ts (only)
        testCombination("url + ts",
                URL + TS);

        // 조합 5: url + _ts + ts + _tsc
        testCombination("url + _ts + ts + _tsc",
                URL + SESSION_TS + TS + COUNTER);

        // 조합 6: _ts + url + ts + _tsc
        testCombination("_ts + url + ts + _tsc",
                SESSION_TS + URL + TS + COUNTER);

        // 조합 7: url + " " + ts + " " + _ts + " " + _tsc (공백 구분)
        testCombination("url + \" \" + ts + \" \" + _ts + \" \" + _tsc",
                URL + " " + TS + " " + SESSION_TS + " " + COUNTER);

        // 조합 8: url만
        testCombination("url only",
                URL);

        // 조합 9: ts + _ts + _tsc + url
        testCombination("ts + _ts + _tsc + url",
                TS + "_" + SESSION_TS + "_" + COUNTER + "_" + URL);

        // 조합 10: url + ":" + ts + ":" + _ts + ":" + _tsc
        testCombination("url + \":\" + ts + \":\" + _ts + \":\" + _tsc",
                URL + ":" + TS + ":" + SESSION_TS + ":" + COUNTER);
    }

    private void testCombination(String description, String input) {
        String signature = sha256Hash(input);
        boolean matches = signature.equals(EXPECTED_SIGNATURE);

        System.out.printf("%-50s: %s%n", description, matches ? "✅ MATCH!" : "❌");
        if (matches) {
            System.out.println("  Input: " + input);
            System.out.println("  Signature: " + signature);
        }
    }

    private String sha256Hash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

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
     * 두 번째 요청 데이터로 검증
     */
    @Test
    void testSecondRequest() {
        System.out.println("\n=== Testing Second Request ===\n");

        String url = "https://www.instagram.com/p/DQHEMGPEqWK/?img_index=3&igsh=MWU5YTM3b2JpZzJp";
        long ts = 1762275183201L;
        long sessionTs = 1761979938888L;
        int counter = 0;
        String expectedSignature = "bd6de958606c56cfd9bbe61f4989079fc9e3bc6414a90ebf8c5fbdf02b3d551a";

        // 여러 조합 테스트
        System.out.println("Expected: " + expectedSignature);

        String[] combinations = {
            url + ts + sessionTs + counter,
            url + "|" + ts + "|" + sessionTs + "|" + counter,
            ts + url + sessionTs + counter,
            url + " " + ts + " " + sessionTs + " " + counter,
            url + ":" + ts + ":" + sessionTs + ":" + counter
        };

        for (int i = 0; i < combinations.length; i++) {
            String signature = sha256Hash(combinations[i]);
            System.out.printf("Combination %d: %s%n", i + 1,
                    signature.equals(expectedSignature) ? "✅ MATCH!" : "❌ " + signature);
        }
    }

    /**
     * URL 파라미터를 JSON 형식으로 테스트
     */
    @Test
    void testJsonFormatCombinations() {
        System.out.println("\n=== Testing JSON Format Combinations ===\n");

        // JSON 형식: {"url":"...","ts":...,"_ts":...,"_tsc":...}
        String jsonInput = String.format(
            "{\"url\":\"%s\",\"ts\":%d,\"_ts\":%d,\"_tsc\":%d}",
            URL, TS, SESSION_TS, COUNTER
        );

        testCombination("JSON format", jsonInput);
        testCombination("JSON without spaces", jsonInput.replace(" ", ""));
    }
}
