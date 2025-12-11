# AAPT2 Build Workflow

This document describes how to use the `build-aapt2.yml` GitHub Actions workflow to compile aapt2_64 binaries for Apktool.

## Overview

The workflow automates the process described in `INTERNAL.md` for building modified aapt2 binaries from AOSP source code. It builds binaries for:

- **Linux** (aapt2, aapt2_64)
- **Windows** (aapt2.exe, aapt2_64.exe) - cross-compiled from Linux
- **macOS** (aapt2_64)

## Requirements

⚠️ **WARNING**: This workflow requires:
- ~150-250GB of disk space
- Several hours of build time (up to 12 hours per platform)
- Significant CPU and memory resources

## How to Use

### Manual Trigger (Recommended)

1. Go to the **Actions** tab in the GitHub repository
2. Select **Build AAPT2 Binaries** from the workflow list
3. Click **Run workflow**
4. Configure the inputs:
   - **Android branch**: The AOSP branch to build (default: `android-16-release`)
   - **Apktool branch**: The modified frameworks/base branch (default: `apktool-9.0.0`)
5. Click **Run workflow** to start

### Workflow Inputs

| Input | Description | Default | Required |
|-------|-------------|---------|----------|
| `android_branch` | Android/AOSP branch to checkout | `android-16-release` | Yes |
| `apktool_branch` | Apktool frameworks/base branch from iBotPeaches/platform_frameworks_base | `apktool-9.0.0` | Yes |

### Available Branches

According to `INTERNAL.md`:

- **AOSP**: Currently using `android-16-release`
- **Apktool frameworks/base**: Branch naming follows Android version
  - Older versions use underscore: `apktool_7.1` (for Android 7.1)
  - Newer versions use hyphen: `apktool-9.0.0` (for Android 9.0 Pie)
  - Current default: `apktool-9.0.0`

**Note**: The naming convention changed at Android 7.1. Check the [platform_frameworks_base repository](https://github.com/iBotPeaches/platform_frameworks_base) for available branches.

## Build Process

The workflow performs the following steps for each platform:

1. **Maximize disk space** (Linux/Windows only)
   - Removes unnecessary software to free up disk space
   - Configures swap space

2. **Install dependencies**
   - Build tools (gcc, make, etc.)
   - Python 3
   - OpenJDK 11

3. **Set up repo tool**
   - Downloads Google's repo tool for AOSP

4. **Initialize AOSP repository**
   - Uses partial clone to reduce download size
   - Syncs only current branch

5. **Sync AOSP sources**
   - Downloads required components (frameworks/base, build system, etc.)
   - Uses parallel jobs for faster sync

6. **Checkout modified frameworks/base**
   - Fetches Apktool's modified frameworks/base
   - Applies changes that disable optimizations and lessen aapt rules

7. **Apply platform-specific patches**
   - macOS: Adds SDK version to supported list if needed

8. **Build aapt2**
   - Sources build environment
   - Selects build target
   - Compiles aapt2

9. **Strip binaries**
   - Removes debug symbols to reduce size

10. **Verify static linking**
    - Uses `ldd` (Linux/Windows) or `otool -L` (macOS)
    - Ensures no shared dependencies

11. **Upload artifacts**
    - Stores built binaries as workflow artifacts

## Outputs

After a successful workflow run, you can download the built binaries from the workflow run page:

- `aapt2-linux` - Linux binaries (aapt2, aapt2_64)
- `aapt2-windows` - Windows binaries (aapt2.exe, aapt2_64.exe)
- `aapt2-macos` - macOS binary (aapt2_64)

## Deployment

Once you have the built binaries:

1. **Verify static linking**
   ```bash
   # Linux/Windows
   ldd aapt2_64
   
   # macOS
   otool -L aapt2_64
   ```
   
   The output should show minimal dependencies (ideally none, or only system libraries).

2. **Test the binary**
   ```bash
   ./aapt2_64 version
   ```

3. **Deploy to Apktool**
   - Copy binaries to `brut.apktool/apktool-lib/src/main/resources/prebuilt/`
     - Linux: `linux/aapt2`
     - Windows: `windows/aapt2.exe`
     - macOS: `macosx/aapt2`

## Troubleshooting

### Build fails due to disk space

The workflow uses `easimon/maximize-build-space` action to free up disk space on GitHub runners. If builds still fail:

- Consider reducing the number of synced repositories in the workflow
- Use more aggressive partial clone settings
- Build platforms separately instead of in parallel

### Build fails on macOS SDK version

The workflow attempts to automatically detect and add the macOS SDK version. If this fails:

1. Check the SDK detection logic in the "Apply macOS SDK patch" step
2. Manually update `build/soong/cc/config/darwin_host.go` if needed

### Build times out

The workflow has a 12-hour timeout per job. AOSP builds can take a long time:

- Ensure sufficient runner resources
- Consider splitting the build into multiple stages
- Use cached AOSP source if building repeatedly

## Schedule (Optional)

The workflow includes a commented-out schedule trigger:

```yaml
# schedule:
#   - cron: '0 0 1 * *'  # Monthly on the 1st
```

Uncomment this to automatically build binaries on a schedule. Note that scheduled builds will consume significant GitHub Actions minutes.

## References

- `INTERNAL.md` - Detailed build instructions
- [iBotPeaches/platform_frameworks_base](https://github.com/iBotPeaches/platform_frameworks_base) - Modified frameworks/base
- [AOSP Download Guide](https://source.android.com/source/downloading.html)
- [AOSP Build Guide](https://source.android.com/source/building.html)
