### Apktool
_This is the repository for Apktool. The website is at the [apktool.org](https://github.com/iBotPeaches/apktool.org) repository._

[![CI](https://github.com/iBotPeaches/Apktool/actions/workflows/build.yml/badge.svg)](https://github.com/iBotPeaches/Apktool/actions/workflows/test.yml)
[![Software License](https://img.shields.io/badge/license-Apache%202.0-brightgreen.svg)](https://github.com/iBotPeaches/Apktool/blob/master/LICENSE.md)

Apktool is a tool for reverse engineering third-party, closed, binary, Android apps. It can decode resources to nearly original form and rebuild them after making some modifications; it makes it possible to debug smali code step-by-step. It also makes working with apps easier thanks to project-like file structure and automation of some repetitive tasks such as building apk, etc.

Apktool is **NOT** intended for piracy and other non-legal uses. It could be used for localizing and adding features, adding support for custom platforms, and other GOOD purposes. Just try to be fair with the authors of an app, that you use and probably like.

### Features

- **Decoding resources** to nearly original form
- **Rebuilding** decoded resources back to binary APK/JAR
- **Smali Debugging** (with smali or JesusFreke's project)
- **Additional DEX Analysis Tools** (NEW) - Run dex2jar, jadx, smali/baksmali, and Androguard analysis tools with `--use-analysis-tools` option

#### Using Additional Analysis Tools

When decoding an APK, you can now run additional analysis tools automatically:

```bash
apktool d --use-analysis-tools your-app.apk
```

This will create a `tools/` directory containing results from:
- **dex2jar**: DEX to JAR conversion
- **jadx**: Java decompilation
- **smali/baksmali**: Analysis summary (already integrated in main decode)
- **Androguard**: Advanced security and flow analysis

Tools are optional and only run if installed on your system. See the generated `tools/README.md` for more information.

### Branches
- `main` - In-development Apktool 3.x branch
- `2.x` - Maintenance branch for Apktool 2.x releases

#### Support
- [Project Page](https://apktool.org)
- [#apktool on libera.chat](https://web.libera.chat)

#### Security Vulnerabilities

If you discover a security vulnerability within Apktool, please send an e-mail to Connor Tumbleson at connor.tumbleson(at)gmail.com. All security vulnerabilities will be promptly addressed.

#### Links
- [Downloads](https://bitbucket.org/iBotPeaches/apktool/downloads)
- [Downloads Mirror](https://connortumbleson.com/apktool)
- [How to Build](https://apktool.org/docs/build)
- [Documentation](https://apktool.org/wiki/the-basics/intro)
- [Bug Reports](https://github.com/iBotPeaches/Apktool/issues)
- [Changelog/Information](https://apktool.org/blog)
- [XDA Post](https://forum.xda-developers.com/t/util-dec-2-2020-apktool-tool-for-reverse-engineering-apk-files.1755243/)
- [Source (GitHub)](https://github.com/iBotPeaches/Apktool)
- [Source (Bitbucket)](https://bitbucket.org/iBotPeaches/apktool/)


## Sponsors

Special thanks goes to the following sponsors:

### Sourcetoad
[Sourcetoad](https://sourcetoad.com/) is an award-winning software and app development firm committed to the co-creation of technology solutions that solve complex business problems, delight users, and help our clients achieve their goals.

<a href="https://www.sourcetoad.com" alt="Sourcetoad">
    <picture>
        <img src="https://github.com/ibotpeaches/apktool/raw/master/.github/assets/sponsors/sourcetoad-horizontal.svg">
    </picture>
</a>

### Emerge Tools

[Emerge Tools](https://www.emergetools.com) is a suite of revolutionary products designed to supercharge mobile apps and the teams that build them.

<a href="https://www.emergetools.com" alt="Emerge Tools">
    <picture>
        <source media="(prefers-color-scheme: dark)" srcset="https://github.com/ibotpeaches/apktool/raw/master/.github/assets/sponsors/emerge-tools-vertical-white.svg">
        <source media="(prefers-color-scheme: light)" srcset="https://github.com/ibotpeaches/apktool/raw/master/.github/assets/sponsors/emerge-tools-vertical-black.svg">
        <img src="https://github.com/ibotpeaches/apktool/raw/master/.github/assets/sponsors/emerge-tools-vertical-black.svg">
    </picture>
</a>
