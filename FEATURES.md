# New Features in Apktool

## Multi-Format Bundle Support

Apktool now supports decoding multiple APK bundle formats in addition to standard APK files:

### Supported Bundle Formats
- **APKM** (APK Mirroring format)
- **APKS** (Android App Bundle split APKs)
- **XAPK** (Extended APK format)

### How It Works
Bundle formats are essentially ZIP files containing multiple APK files. Apktool automatically:
1. Detects bundle format files based on file extension
2. Extracts all APK files from the bundle
3. Identifies the main/base APK
4. Decodes the main APK as usual
5. Saves split APKs to a `splits/` directory for reference

### Usage
Simply provide a bundle file to the decode command:

```bash
apktool d myapp.apkm
apktool d myapp.apks -o output_dir
apktool d myapp.xapk
```

The tool will automatically detect the bundle format and process it accordingly.

### Output Structure
When decoding a bundle, you'll get:
- Standard decoded APK structure from the main/base APK
- `bundle_extracted/` - Temporary extraction directory (removed unless verbose mode)
- `splits/` - Contains other split APKs from the bundle

## DEX Deobfuscation

Apktool now includes basic deobfuscation capabilities for smali files to help reverse engineering obfuscated applications.

### Features
- Detects obfuscated class names (single-letter classes)
- Detects obfuscated method names (single-letter methods)
- Detects obfuscated field names (single-letter fields)
- Renames obfuscated identifiers to more readable names

### How It Works
The deobfuscator:
1. Analyzes smali files after disassembly
2. Identifies likely obfuscated names (very short, single-letter identifiers)
3. Creates a mapping of old names to new readable names
4. Applies the renaming across all smali files

### Usage
Add the `--deobfuscate` flag when decoding:

```bash
apktool d -v --deobfuscate obfuscated-app.apk
```

### Example
Before deobfuscation:
```smali
.class public La;
.field private a:I
.method public a()V
```

After deobfuscation:
```smali
.class public LDeobfClass1;
.field private field1:I
.method public method1()V
```

### Notes
- Deobfuscation is basic and uses heuristics
- Not all obfuscated code will be detected
- Some false positives may occur with legitimate single-letter identifiers
- For full deobfuscation, consider specialized tools like JADX

## Advanced Options

### Decode Options
```
--deobfuscate              Apply basic deobfuscation to smali files
```

### Bundle Support
Bundle support is automatic and requires no special flags. The tool detects bundle formats by file extension.

## Examples

### Decode an APKM bundle with deobfuscation
```bash
apktool d --deobfuscate myapp.apkm -o myapp_decoded
```

### Decode an APKS bundle with verbose output
```bash
apktool d -v myapp.apks
```

### Decode a regular APK with deobfuscation
```bash
apktool d --deobfuscate obfuscated.apk
```

## Compatibility

These features are compatible with all existing Apktool functionality:
- All standard decode options work with bundles
- Deobfuscation works alongside all other decode options
- Build functionality remains unchanged

## Technical Details

### Bundle Format Support
- Implemented in `BundleDecoder.java`
- Uses standard ZIP extraction
- Automatically identifies base APK using naming conventions
- Falls back to first APK if base cannot be identified

### Deobfuscation
- Implemented in `DexDeobfuscator.java`
- Works on smali files (post-disassembly)
- Uses pattern matching to detect obfuscation
- Maintains consistency across all smali files
- Safe: does not modify original APK, only decoded output

## Future Enhancements

Potential future improvements:
- Integration with full JADX decompiler
- More sophisticated obfuscation detection
- String deobfuscation
- Control flow deobfuscation
- Custom deobfuscation rule files
- Bundle building support
