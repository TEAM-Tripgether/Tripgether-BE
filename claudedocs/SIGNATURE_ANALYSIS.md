# Signature Generation Analysis

## Captured Data

### Request 1
```json
{
  "url": "https://www.instagram.com/p/DQHEMGPEqWK/?img_index=3&igsh=MWU5YTM3b2JpZzJp",
  "ts": 1762275012493,
  "_ts": 1761979938888,
  "_tsc": 0,
  "_s": "3b5cded7def76fc0752b1f1c4aab36635afa80d67756e2b8b96feed1dd2a5b10"
}
```

### Request 2
```json
{
  "url": "https://www.instagram.com/p/DQHEMGPEqWK/?img_index=3&igsh=MWU5YTM3b2JpZzJp",
  "ts": 1762275183201,
  "_ts": 1761979938888,
  "_tsc": 0,
  "_s": "bd6de958606c56cfd9bbe61f4989079fc9e3bc6414a90ebf8c5fbdf02b3d551a"
}
```

## Analysis

### Parameters
- **url**: Instagram post URL (same in both requests)
- **ts**: Current timestamp in milliseconds (changes each request)
  - Request 1: `1762275012493`
  - Request 2: `1762275183201`
  - Difference: ~171 seconds
- **_ts**: Session start timestamp (same in both requests)
  - Value: `1761979938888`
  - This is set when page loads
- **_tsc**: Request counter (same = 0, likely increments)
  - Value: `0` (first request in session)
- **_s**: Signature - 64 character hex string = SHA-256 hash
  - Request 1: `3b5cded7def76fc0752b1f1c4aab36635afa80d67756e2b8b96feed1dd2a5b10`
  - Request 2: `bd6de958606c56cfd9bbe61f4989079fc9e3bc6414a90ebf8c5fbdf02b3d551a`

### Signature Pattern

The `_s` is a **SHA-256 hash** (256 bits = 64 hex characters).

Possible inputs to the hash function:
1. `url + ts + _ts + _tsc + SECRET_KEY`
2. `url + ts + _ts + _tsc` (ordered concatenation)
3. `JSON.stringify({url, ts, _ts, _tsc}) + SECRET_KEY`

The secret key is unknown, but we can try common patterns.

## Hypothesis Testing

### Hypothesis 1: Simple Concatenation
```
Input: url + ts + _ts + _tsc
SHA256: hash(input)
```

### Hypothesis 2: With Separator
```
Input: url + "|" + ts + "|" + _ts + "|" + _tsc
SHA256: hash(input)
```

### Hypothesis 3: With Secret Key
```
Input: url + ts + _ts + _tsc + "SECRET_KEY"
SHA256: hash(input)
```

### Hypothesis 4: Different Order
```
Input: ts + url + _ts + _tsc
SHA256: hash(input)
```

## Next Steps

1. Test each hypothesis with Java SHA-256
2. Compare generated hash with captured `_s` value
3. If no match, try variations:
   - Different parameter orders
   - With/without URL encoding
   - With/without separators
   - Different secret keys

## Implementation Strategy

Since we don't know the exact secret key, we have two options:

### Option 1: Reverse Engineer (Hard)
- Try to find the secret key by testing various combinations
- May require extensive trial and error

### Option 2: Extract from Browser (Easier)
- Use Chrome DevTools to intercept the signature generation function
- Set breakpoint in JavaScript and extract the exact algorithm
- Convert the JavaScript implementation to Java

### Option 3: Use GraalVM (Current Approach)
- Execute the actual JavaScript code from sssinstagram.com
- Extract the signature generation function
- Run it in Java using GraalVM

## Test Results

**All 10 input combinations tested - ALL FAILED:**
- url + ts + _ts + _tsc: ❌
- url + "|" + ts + "|" + _ts + "|" + _tsc: ❌
- ts + url + _ts + _tsc: ❌
- url + ts: ❌
- url + _ts + ts + _tsc: ❌
- _ts + url + ts + _tsc: ❌
- url + " " + ts + " " + _ts + " " + _tsc: ❌
- url only: ❌
- ts + _ts + _tsc + url: ❌
- url + ":" + ts + ":" + _ts + ":" + _tsc: ❌

**Conclusion**: Signature includes SECRET KEY or uses complex algorithm

## JavaScript Analysis Findings

**Key Code Found in link.chunk.js:**
```javascript
// Signature calculation (line before assignment):
lZKDtpj(""
  .concat(typeof eLRTOci == "string" ? eLRTOci : JSON.stringify(eLRTOci))
  .concat(Ejs2juf)  // ts value
  .concat(vNl6BM))  // unknown value

// Then assigns result:
Kz3sfR[0xd8] = Kz3sfR[0x95]  // _s = result from above

// Final object:
{
  ts: timestamp,
  _ts: sessionTimestamp,
  _tsc: counter,
  _s: signature
}
```

**Function `lZKDtpj`**: Heavily obfuscated hashing function
**Unknown Variable `vNl6BM`**: Additional input to signature (SECRET KEY?)

## Recommended Approach

**Use Chrome Console to extract the ACTUAL signature generation:**

1. Open https://sssinstagram.com in Chrome DevTools
2. Set breakpoint when `lZKDtpj` is called
3. Step through to see what `vNl6BM` contains
4. Extract the complete signature algorithm including secret
5. Convert to Java or execute via GraalVM
