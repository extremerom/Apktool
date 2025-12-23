# Verification Bypass Example

This directory contains an educational example demonstrating how to modify smali code to bypass verification and trial logic in Android applications using Apktool.

## ⚠️ Important Disclaimer

This example is provided for **EDUCATIONAL PURPOSES ONLY**. The purpose is to demonstrate:
- How Apktool can decode and modify Android applications
- Understanding of smali bytecode structure
- Security research and vulnerability analysis
- Legitimate use cases such as debugging your own applications

**DO NOT use these techniques for:**
- Piracy or software license violations
- Unauthorized access to applications
- Any illegal activities

Always respect intellectual property rights and software licenses.

## Files

### MainActivity.smali (Original)
This file contains an Android Activity with:
1. **Verification Logic**: A `verifyInput()` method that checks if user input matches a hardcoded verification code (`VALID123`)
2. **Trial Check**: A `checkTrial()` method that returns `true`, indicating the app is in trial mode
3. **Trial Message**: Shows "Trial version - Limited features" toast when app starts

### MainActivity_modified.smali (Modified)
This file shows the modifications needed to bypass the protection mechanisms:

## Modifications Explained

### 1. Bypass Verification Logic

**Original Code** (lines 20-29 in MainActivity.smali):
```smali
.method public verifyInput(Ljava/lang/String;)Z
    .locals 2
    .param p1, "input"    # Ljava/lang/String;

    .prologue
    .line 20
    iget-object v0, p0, Lcom/example/verifyapp/MainActivity;->verificationCode:Ljava/lang/String;

    .line 21
    .local v0, "validCode":Ljava/lang/String;
    invoke-virtual {v0, p1}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v1

    if-eqz v1, :cond_0

    .line 22
    const/4 v1, 0x1

    return v1

    .line 24
    :cond_0
    const/4 v1, 0x0

    return v1
.end method
```

**Modified Code**:
```smali
.method public verifyInput(Ljava/lang/String;)Z
    .locals 2
    .param p1, "input"    # Ljava/lang/String;

    .prologue
    .line 20
    # MODIFIED: Always return true (1) to bypass verification
    # Any input will now be considered valid
    
    .line 21
    const/4 v1, 0x1

    return v1
.end method
```

**Key Changes**:
- Removed the string comparison logic (`iget-object` and `invoke-virtual`)
- Directly return `true` (value `0x1`) without checking input
- This makes ANY input value accepted as valid

### 2. Remove Trial Logic

**Original Code** (lines 36-38 in MainActivity.smali):
```smali
.method private checkTrial()Z
    .locals 1

    .prologue
    .line 45
    const/4 v0, 0x1

    return v0
.end method
```

**Modified Code**:
```smali
.method private checkTrial()Z
    .locals 1

    .prologue
    .line 45
    # MODIFIED: Always return false (0) to disable trial mode
    const/4 v0, 0x0

    return v0
.end method
```

**Key Changes**:
- Changed return value from `0x1` (true) to `0x0` (false)
- This indicates the app is NOT in trial mode

### 3. Remove Trial Message (Alternative approach)

In `onCreate()`, you can also comment out or remove the trial check entirely:

**Original Code**:
```smali
invoke-direct {p0}, Lcom/example/verifyapp/MainActivity;->checkTrial()Z
move-result v1
if-eqz v1, :cond_0
const-string v1, "Trial version - Limited features"
const/4 v2, 0x1
invoke-static {p0, v1, v2}, Landroid/widget/Toast;->makeText(Landroid/content/Context;Ljava/lang/CharSequence;I)Landroid/widget/Toast;
move-result-object v1
invoke-virtual {v1}, Landroid/widget/Toast;->show()V
:cond_0
```

**Modified Approach**: Simply remove these lines entirely so the trial check is never executed.

## How to Apply These Modifications

1. **Decompile APK with Apktool**:
   ```bash
   apktool d app.apk -o app_decompiled
   ```

2. **Locate the smali files**:
   - Navigate to `app_decompiled/smali/com/example/verifyapp/`
   - Find `MainActivity.smali`

3. **Make the modifications**:
   - Edit the smali file with the changes shown above
   - Be careful to maintain proper smali syntax

4. **Rebuild the APK**:
   ```bash
   apktool b app_decompiled -o app_modified.apk
   ```

5. **Sign the APK** (required for installation):
   ```bash
   # Generate a keystore if you don't have one
   keytool -genkey -v -keystore my-release-key.keystore -alias alias_name -keyalg RSA -keysize 2048 -validity 10000
   
   # Sign the APK
   jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 -keystore my-release-key.keystore app_modified.apk alias_name
   ```

## Understanding Smali Instructions

### Common Instructions Used:
- `const/4 vX, 0x0`: Load constant 0 (false) into register vX
- `const/4 vX, 0x1`: Load constant 1 (true) into register vX
- `const-string vX, "text"`: Load string constant into register vX
- `return vX`: Return value in register vX
- `return-void`: Return from void method
- `invoke-virtual {args}, LClass;->method(params)ReturnType`: Call a virtual method
- `invoke-direct {args}, LClass;->method(params)ReturnType`: Call a direct/private method
- `if-eqz vX, :label`: If register vX equals zero, branch to label
- `move-result vX`: Move method call result into register vX
- `iget-object vX, vY, LClass;->field:LType;`: Get instance field object from vY into vX
- `iput-object vX, vY, LClass;->field:LType;`: Put instance field object from vX into vY's field

### Boolean Values in Smali:
- `false` = `0x0` = `0`
- `true` = `0x1` = `1`

## Testing Your Modifications

After rebuilding and signing:
1. Install the modified APK on your device
2. Open the app
3. Try entering ANY verification code - it should now be accepted
4. The trial message should no longer appear

## Security Implications

This example demonstrates why client-side validation alone is insufficient:
- Never trust client-side checks for security
- Always validate on the server side
- Use proper code obfuscation (ProGuard/R8)
- Implement certificate pinning for sensitive apps
- Use SafetyNet/Play Integrity API
- Consider root/tamper detection

## Further Reading

- [Apktool Documentation](https://apktool.org/docs/the-basics/intro)
- [Smali Language Reference](https://github.com/JesusFreke/smali/wiki)
- [Android Security Best Practices](https://developer.android.com/topic/security/best-practices)
