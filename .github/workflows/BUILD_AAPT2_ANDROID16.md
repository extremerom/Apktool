# Building aapt2_64 for Android 16

This document explains how to use the GitHub Actions workflow to build aapt2_64 binaries for Android 16.

## Overview

The `build-aapt2-android16.yml` workflow automates the process described in `INTERNAL.md` for building modified aapt2 binaries for Apktool from AOSP (Android Open Source Project) source code.

## Workflow Features

- **Automated AOSP Download**: Uses repo tool to download AOSP with optimizations (partial clone, current branch only)
- **Multi-Platform Builds**: Builds for Linux, Windows, and macOS in separate jobs
- **Modified frameworks/base**: Automatically integrates Apktool's modified frameworks/base from iBotPeaches/platform_frameworks_base
- **macOS SDK Patching**: Automatically detects and patches macOS SDK version compatibility
- **Binary Verification**: Checks that binaries are statically linked
- **Artifact Packaging**: Combines all platform binaries into a single downloadable package

## Prerequisites

### Important: Disk Space Requirements

⚠️ **CRITICAL**: AOSP requires **150-250GB** of disk space. Standard GitHub-hosted runners only have ~14GB available.

**Options:**
1. **Self-hosted runners**: Set up self-hosted runners with sufficient disk space
2. **Modify workflow**: Adapt the workflow to use cloud storage or build specific components
3. **Split builds**: Run builds separately with cleanup between platforms

### Repository Access

The workflow needs access to:
- AOSP repositories at `https://android.googlesource.com`
- Modified frameworks/base at `https://github.com/iBotPeaches/platform_frameworks_base`

## How to Use

### 1. Trigger the Workflow

The workflow uses `workflow_dispatch`, meaning it must be manually triggered:

1. Go to the **Actions** tab in your GitHub repository
2. Select **"Build aapt2_64 for Android 16"** from the workflows list
3. Click **"Run workflow"**
4. Configure the inputs:
   - **Apktool frameworks/base branch**: Branch name from iBotPeaches/platform_frameworks_base (e.g., `apktool-16.0`)
   - **AOSP tag/branch**: AOSP branch or tag to use (default: `android-16-release`)
5. Click **"Run workflow"** to start

### 2. Monitor Progress

The workflow has three jobs:

1. **build-linux-windows**: Builds Linux and Windows binaries (~2-4 hours depending on runner)
2. **build-macos**: Builds macOS binaries (~2-4 hours depending on runner)
3. **verify-and-package**: Combines and packages all binaries

Each job can be monitored individually in the Actions UI.

### 3. Download Artifacts

Once the workflow completes, artifacts will be available:

- **aapt2-linux-android16**: Linux binaries (aapt2, aapt2_64)
- **aapt2-windows-android16**: Windows binaries (aapt2.exe, aapt2_64.exe)
- **aapt2-macos-android16**: macOS binary (aapt2_64)
- **aapt2-android16-all-platforms**: Combined tarball with all platforms
- **aapt2-android16-package**: Unpacked directory structure with README

Download the artifacts from the workflow run summary page.

## Workflow Configuration

### Input Parameters

| Parameter | Description | Default | Required |
|-----------|-------------|---------|----------|
| `apktool_branch` | Branch from iBotPeaches/platform_frameworks_base to use | `apktool-16.0` | Yes |
| `android_tag` | AOSP branch or tag to build from | `android-16-release` | Yes |

### Environment Variables

The workflow sets these environment variables:
- `AOSP_BRANCH`: AOSP branch to sync
- `APKTOOL_BRANCH`: Apktool frameworks/base branch to checkout

## Build Process

### Linux/Windows Build (build-linux-windows job)

1. Install build dependencies (build-essential, flex, bison, etc.)
2. Install repo tool
3. Initialize AOSP repository with `--partial-clone` for efficiency
4. Sync repository with `-c` (current branch only)
5. Replace frameworks/base with Apktool's modified version
6. Build aapt2 using `lunch` and `m aapt2`
7. Strip binaries to reduce size
8. Verify binaries are statically linked using `ldd`
9. Upload artifacts

### macOS Build (build-macos job)

1. Install Xcode command line tools and dependencies
2. Install repo tool
3. Initialize and sync AOSP repository
4. Replace frameworks/base with Apktool's modified version
5. **Apply macOS SDK patch**: Automatically detect SDK version and patch `darwin_host.go`
6. Set `ANDROID_JAVA_HOME` environment variable
7. Build aapt2
8. Strip binary
9. Verify binary using `otool -L`
10. Upload artifact

### Verify and Package (verify-and-package job)

1. Download all platform artifacts
2. Organize into directory structure
3. Create README with build information
4. Package into tarball
5. Upload combined package

## Verification

After downloading the binaries, verify they are statically linked:

### Linux
```bash
ldd aapt2_64
# Should show "not a dynamic executable" or minimal dependencies
```

### macOS
```bash
otool -L aapt2_64
# Should show only system libraries (libc++, libsystem, etc.)
```

### Windows
Use Dependency Walker or similar tools to check for minimal dependencies.

## Troubleshooting

### Disk Space Issues

If builds fail due to disk space:
- Use self-hosted runners with larger disks
- Consider building platforms separately
- Clean up between builds

### SDK Version Issues (macOS)

The workflow automatically patches the SDK version. If this fails:
- Check the macOS SDK version installed on the runner
- Manually verify the patch in `build/soong/cc/config/darwin_host.go`

### Build Failures

If the build fails:
- Check the job logs for specific error messages
- Verify the AOSP branch and Apktool branch exist
- Ensure network connectivity to AOSP repositories

### Missing Binaries

If binaries are not found after build:
- Check the build logs for errors during compilation
- Verify the output paths match AOSP's structure
- Check if the `lunch` command selected the correct target

## Integration with Apktool

To use the built binaries with Apktool:

1. Download the `aapt2-android16-all-platforms` artifact
2. Extract the tarball
3. Copy binaries to Apktool's prebuilt directory:
   ```bash
   cp linux/aapt2_64 /path/to/apktool/brut.apktool/apktool-lib/src/main/resources/prebuilt/linux/
   cp windows/aapt2_64.exe /path/to/apktool/brut.apktool/apktool-lib/src/main/resources/prebuilt/windows/
   cp macos/aapt2_64 /path/to/apktool/brut.apktool/apktool-lib/src/main/resources/prebuilt/macosx/
   ```

## References

- **INTERNAL.md**: Original build instructions in this repository
- **AOSP Build Guide**: https://source.android.com/source/building
- **Repo Tool**: https://source.android.com/source/downloading
- **iBotPeaches/platform_frameworks_base**: https://github.com/iBotPeaches/platform_frameworks_base

## Notes

- This workflow is designed for building aapt2 for Android 16 specifically
- For other Android versions, adjust the `android_tag` and `apktool_branch` inputs
- Build times vary significantly based on runner performance and network speed
- The first sync of AOSP will take considerable time due to the large download size

## Support

For issues with:
- **The workflow itself**: Open an issue in this repository
- **Apktool modifications**: See iBotPeaches/platform_frameworks_base
- **AOSP build issues**: Consult AOSP documentation

## License

This workflow is part of the Apktool project and follows the same license.
