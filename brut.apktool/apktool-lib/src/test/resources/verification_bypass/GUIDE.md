# Step-by-Step Guide: Bypassing Verification in Smali

## Overview
This guide demonstrates how to modify smali code to bypass verification and trial logic in Android applications. This is for **educational and legitimate purposes only** such as debugging your own applications or security research.

## Problem: Verification Fails with Any Input

**Symptom**: When you enter any value in the verification field, it always shows "verify fail"

**Cause**: The app has hardcoded verification logic that checks input against a specific value

**Solution**: Modify the smali code to accept any input as valid

## Step 1: Decompile the APK

```bash
apktool d your_app.apk -o app_decompiled
```

This will create a directory `app_decompiled` with all the decompiled resources and smali code.

## Step 2: Locate the Verification Code

Navigate to the smali directory:
```bash
cd app_decompiled/smali
```

Look for files related to verification. Common names:
- `MainActivity.smali`
- `LoginActivity.smali`
- `VerificationHelper.smali`
- `LicenseChecker.smali`

Use grep to search:
```bash
grep -r "verify" .
grep -r "license" .
grep -r "trial" .
```

## Step 3: Understand the Verification Logic

### Example 1: Simple String Comparison

**Original Code:**
```smali
.method public verifyInput(Ljava/lang/String;)Z
    .locals 2
    .param p1, "input"

    # Get the valid code
    iget-object v0, p0, Lcom/example/MainActivity;->verificationCode:Ljava/lang/String;
    
    # Compare input with valid code
    invoke-virtual {v0, p1}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
    move-result v1
    
    # If equal, return true
    if-eqz v1, :cond_0
    const/4 v1, 0x1
    return v1
    
    # Otherwise return false
    :cond_0
    const/4 v1, 0x0
    return v1
.end method
```

### Example 2: License Check

**Original Code:**
```smali
.method public static isValidLicense(Ljava/lang/String;)Z
    .locals 2
    
    const-string v0, "ABC123XYZ789"
    invoke-virtual {v0, p0}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
    move-result v1
    return v1
.end method
```

## Step 4: Modify the Code

### Modification 1: Always Return True

Replace the entire verification logic with a simple return true statement:

**Modified Code:**
```smali
.method public verifyInput(Ljava/lang/String;)Z
    .locals 2
    .param p1, "input"

    # Always return true - any input is valid
    const/4 v1, 0x1
    return v1
.end method
```

**What changed:**
- Removed `iget-object` (getting the valid code)
- Removed `invoke-virtual` (comparing strings)
- Removed conditional logic (`if-eqz`, `:cond_0`)
- Directly return `0x1` (true)

### Modification 2: Bypass License Check

**Modified Code:**
```smali
.method public static isValidLicense(Ljava/lang/String;)Z
    .locals 2
    
    # Always valid - ignore input
    const/4 v1, 0x1
    return v1
.end method
```

## Step 5: Remove Trial Logic

### Find Trial Check

Look for methods like:
```smali
.method private checkTrial()Z
    .locals 1
    
    const/4 v0, 0x1
    return v0
.end method
```

Or expiration checks:
```smali
.method public static checkExpiration()Z
    .locals 4
    
    invoke-static {}, Ljava/lang/System;->currentTimeMillis()J
    move-result-wide v0
    
    const-wide v2, 0x18ba8f00400L
    cmp-long v2, v0, v2
    if-gez v2, :cond_0
    
    const/4 v2, 0x1
    return v2
    
    :cond_0
    const/4 v2, 0x0
    return v2
.end method
```

### Modify Trial Check

**Option A - Return False (Not in trial):**
```smali
.method private checkTrial()Z
    .locals 1
    
    # Return false - not in trial mode
    const/4 v0, 0x0
    return v0
.end method
```

**Option B - Remove Trial Message:**
In `onCreate()`, find and remove these lines:
```smali
# Remove or comment out:
invoke-direct {p0}, Lcom/example/MainActivity;->checkTrial()Z
move-result v1
if-eqz v1, :cond_0
const-string v1, "Trial version"
# ... rest of toast message code
:cond_0
```

### Modify Expiration Check

**Always return false (never expired):**
```smali
.method public static checkExpiration()Z
    .locals 4
    
    # Never expired
    const/4 v2, 0x0
    return v2
.end method
```

## Step 6: Rebuild the APK

```bash
cd ..
apktool b app_decompiled -o app_modified.apk
```

## Step 7: Sign the APK

### Generate a Keystore (first time only):
```bash
keytool -genkey -v -keystore my-key.keystore -alias my_alias -keyalg RSA -keysize 2048 -validity 10000
```

### Sign the APK:
```bash
jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 -keystore my-key.keystore app_modified.apk my_alias
```

Or use apksigner (recommended):
```bash
apksigner sign --ks my-key.keystore --out app_modified_signed.apk app_modified.apk
```

## Step 8: Install and Test

```bash
adb install app_modified_signed.apk
```

### Test Results:
1. ✅ Any verification code should now be accepted
2. ✅ Trial message should no longer appear
3. ✅ App should work with full features

## Common Patterns to Look For

### Pattern 1: Hardcoded Keys
```smali
const-string v0, "VALID_KEY"
invoke-virtual {v0, p1}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
```
**Fix**: Return true immediately

### Pattern 2: Time-based Checks
```smali
invoke-static {}, Ljava/lang/System;->currentTimeMillis()J
```
**Fix**: Return false (not expired) or remove check

### Pattern 3: Boolean Flags
```smali
iget-boolean v0, p0, Lclass;->isTrial:Z
if-eqz v0, :cond_0
```
**Fix**: Change field value or skip check

### Pattern 4: Server Verification (Cannot bypass locally)
```smali
invoke-virtual {v0}, Lcom/example/Api;->verifyLicense()Z
```
**Note**: If verification is done server-side, local modifications won't work

## Smali Instruction Quick Reference

| Instruction | Meaning |
|-------------|---------|
| `const/4 vX, 0x0` | Set register vX to 0 (false) |
| `const/4 vX, 0x1` | Set register vX to 1 (true) |
| `return vX` | Return value in register vX |
| `return-void` | Return from void method |
| `if-eqz vX, :label` | If vX equals zero, jump to label |
| `if-nez vX, :label` | If vX not equal to zero, jump to label |
| `goto :label` | Unconditional jump to label |
| `invoke-virtual` | Call virtual method |
| `invoke-direct` | Call direct/private method |
| `invoke-static` | Call static method |

## Tips

1. **Back up original APK** before making changes
2. **Test incrementally** - make one change at a time
3. **Keep original code** as comments for reference
4. **Check logcat** for errors: `adb logcat`
5. **Use proper indentation** to maintain readability
6. **Validate smali syntax** before rebuilding

## Troubleshooting

### APK won't rebuild
- Check for syntax errors in smali files
- Make sure all method end with `.end method`
- Check for balanced `.prologue` and `.line` directives

### APK won't install
- Make sure it's properly signed
- Uninstall old version first: `adb uninstall com.package.name`
- Check certificate compatibility

### App crashes
- Check logcat for errors
- Make sure you didn't remove necessary code
- Verify register usage (locals count)

## Legal and Ethical Considerations

✅ **Legitimate uses:**
- Debugging your own applications
- Security research and vulnerability testing
- Educational purposes
- Testing on apps you have permission to modify

❌ **Illegal uses:**
- Bypassing paid software licenses
- Distributing modified apps
- Copyright infringement
- Unauthorized access to services

Always respect intellectual property rights and use these techniques responsibly.
