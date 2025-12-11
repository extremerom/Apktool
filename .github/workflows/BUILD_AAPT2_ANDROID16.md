# Building aapt2_64 for Android 16 and Apktool

This document explains how to use the GitHub Actions workflow to build aapt2_64 binaries for Android 16 and compile Apktool with the newly built binaries.

## Overview

The `build-aapt2-android16.yml` workflow automates the process described in `INTERNAL.md` for building modified aapt2 binaries for Apktool from AOSP (Android Open Source Project) source code, and then uses those binaries to build Apktool.

## Workflow Features

- **Automated AOSP Download**: Uses repo tool to download AOSP with optimizations (partial clone, current branch only)
- **Linux-only Build**: Builds aapt2 and aapt2_64 for Linux x86_64
- **Modified frameworks/base**: Automatically integrates Apktool's modified frameworks/base from iBotPeaches/platform_frameworks_base
- **Binary Verification**: Checks that binaries are statically linked
- **Apktool Build**: Compiles Apktool using the newly built aapt2_64 binaries
- **Automatic Release**: Creates a GitHub release with all artifacts using the TEST token

## Prerequisites

### Important: Disk Space Requirements

⚠️ **CRITICAL**: AOSP requires **150-250GB** of disk space. Standard GitHub-hosted runners only have ~14GB available.

**Options:**
1. **Self-hosted runners**: Set up self-hosted runners with sufficient disk space
2. **Modify workflow**: Adapt the workflow to use cloud storage or build specific components

### Repository Access

The workflow needs access to:
- AOSP repositories at `https://android.googlesource.com`
- Modified frameworks/base at `https://github.com/iBotPeaches/platform_frameworks_base`

### Secrets

The workflow requires the `TEST` secret to be configured in the repository settings for creating releases.

## How to Use

### 1. Trigger the Workflow

The workflow uses `workflow_dispatch`, meaning it must be manually triggered:

1. Go to the **Actions** tab in your GitHub repository
2. Select **"Build aapt2_64 for Android 16"** from the workflows list
3. Click **"Run workflow"**
4. Configure the inputs:
   - **Apktool frameworks/base branch**: Branch name from iBotPeaches/platform_frameworks_base (default: `apktool-3.0.x`)
   - **AOSP tag/branch**: AOSP branch or tag to use (default: `android-16-release`)
5. Click **"Run workflow"** to start

### 2. Monitor Progress

The workflow has one main job that:

1. Builds aapt2 and aapt2_64 for Linux (~2-4 hours depending on runner)
2. Copies the binaries to Apktool's prebuilt directory
3. Builds Apktool using the new binaries
4. Creates a GitHub release with all artifacts

Progress can be monitored in the Actions UI.

### 3. Download Artifacts

Once the workflow completes, a new release will be created automatically with:

- **aapt2**: Linux aapt2 binary
- **aapt2_64**: Linux aapt2_64 binary
- **apktool-*.jar**: Apktool JAR file built with the new aapt2_64

Download the artifacts from the Releases page.

## Workflow Configuration

### Input Parameters

| Parameter | Description | Default | Required |
|-----------|-------------|---------|----------|
| `apktool_branch` | Branch from iBotPeaches/platform_frameworks_base to use | `apktool-3.0.x` | Yes |
| `android_tag` | AOSP branch or tag to build from | `android-16-release` | Yes |

### Environment Variables

The workflow sets these environment variables:
- `AOSP_BRANCH`: AOSP branch to sync
- `APKTOOL_BRANCH`: Apktool frameworks/base branch to checkout

## Build Process

### Single Unified Job (build-aapt2-and-apktool)

1. Install build dependencies (build-essential, flex, bison, etc.)
2. Install repo tool
3. Initialize AOSP repository with `--partial-clone` for efficiency
4. Sync repository with `-c` (current branch only)
5. Replace frameworks/base with Apktool's modified version
6. Build aapt2 using `lunch` and `m aapt2`
7. Strip binaries to reduce size
8. Verify binaries are statically linked using `ldd`
9. Copy binaries to Apktool's prebuilt/linux directory
10. Build Apktool using Gradle (build, shadowJar, proguard)
11. Prepare release artifacts (aapt2, aapt2_64, apktool jar)
12. Create GitHub release with all artifacts

## Verification

After the build completes, the workflow automatically verifies that binaries are statically linked:

### Linux
```bash
ldd aapt2_64
# Should show "not a dynamic executable" or minimal dependencies
```

## Releases

The workflow automatically creates a GitHub release with:
- Tag name: `android-16-{build_number}`
- Release name: `Apktool with aapt2_64 for Android 16 (Build {build_number})`
- Files:
  - `aapt2` - Linux aapt2 binary
  - `aapt2_64` - Linux aapt2_64 binary  
  - `apktool-*.jar` - Apktool JAR built with the new binaries

## Troubleshooting

### Disk Space Issues

If builds fail due to disk space:
- Use self-hosted runners with larger disks (minimum 300GB recommended)
- Clean up between builds if running multiple times

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

### Release Creation Failures

If release creation fails:
- Verify the `TEST` secret is configured in repository settings
- Check that the token has permissions to create releases
- Ensure the tag doesn't already exist

## Notes

- This workflow is designed for building aapt2 for Android 16 specifically
- For other Android versions, adjust the `android_tag` and `apktool_branch` inputs
- Build times vary significantly based on runner performance and network speed
- The first sync of AOSP will take considerable time due to the large download size
- Only Linux binaries are built; macOS and Windows builds have been removed for simplicity

## Support

For issues with:
- **The workflow itself**: Open an issue in this repository
- **Apktool modifications**: See iBotPeaches/platform_frameworks_base
- **AOSP build issues**: Consult AOSP documentation

## License

This workflow is part of the Apktool project and follows the same license.
