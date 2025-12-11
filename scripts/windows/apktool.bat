@echo off
setlocal
set BASENAME=apktool_
chcp 65001 2>nul >nul

set java_exe=java.exe

if defined JAVA_HOME (
set "java_exe=%JAVA_HOME%\bin\java.exe"
)

rem Find the highest version .jar available in the same directory as the script
setlocal EnableDelayedExpansion
pushd "%~dp0"
if exist apktool.jar (
    set BASENAME=apktool
    goto skipversioned
)

set BASENAME=apktool
set max_major=0
set max_minor=0
set max_patch=0

rem Loop through all versioned .jar files matching the basename
for %%F in (%BASENAME%*.jar) do (
    set "filename=%%~nF"
    
    rem Extract version part (apktool-X.Y.Z)
    for /f "tokens=2 delims=_-" %%A in ("!filename!") do (
        for /f "tokens=1,2,3 delims=." %%B in ("%%A") do (
            set "major=%%B"
            set "minor=%%C"
            set "patch=%%D"

            rem Set Default minor/patch to 0
            if "!minor!"=="" set "minor=0"
            if "!patch!"=="" set "patch=0"

            rem Compare major version
            if !major! gtr !max_major! (
                set "max_major=!major!"
                set "max_minor=!minor!"
                set "max_patch=!patch!"
            ) else if !major! == !max_major! (
                rem Compare minor version
                if !minor! gtr !max_minor! (
                    set "max_minor=!minor!"
                    set "max_patch=!patch!"
                ) else if !minor! == !max_minor! (
                    rem Compare patch version
                    if !patch! gtr !max_patch! (
                        set "max_patch=!patch!"
                    )
                )
            )
        )
    )
)

rem Construct full version string
set "max=_!max_major!.!max_minor!.!max_patch!"

:skipversioned
popd
setlocal DisableDelayedExpansion

rem Find out if the commandline is a parameterless .jar or directory, for fast unpack/repack
if "%~1"=="" goto load
if not "%~2"=="" goto load
set ATTR=%~a1
if "%ATTR:~0,1%"=="d" (
    rem Directory, rebuild
    set fastCommand=b
)
if "%ATTR:~0,1%"=="-" if "%~x1"==".apk" (
    rem APK file, unpack
    set fastCommand=d
)

:load
rem Extract -J prefixed JVM options from command line arguments
set "javaOpts=-Xmx6144M -Duser.language=en -Dfile.encoding=UTF8 -Djdk.util.zip.disableZip64ExtraFieldValidation=true -Djdk.nio.zipfs.allowDotZipEntry=true"
set "apktoolArgs="

:parse_args
if "%~1"=="" goto run_apktool
rem Check if argument starts with -J
echo %1 | findstr /B /C:"-J" >nul
if %errorlevel% equ 0 (
    rem Extract JVM option (remove -J prefix, e.g., -JXmx2048M becomes -Xmx2048M)
    rem Use for loop to extract substring without needing delayed expansion
    for /f "usebackq tokens=*" %%a in ('%1') do set "tempArg=%%a"
    call set "opt=%%tempArg:~2%%"
    set "javaOpts=%javaOpts% -%opt%"
    shift
    goto parse_args
) else (
    rem Regular argument - add to apktool args
    set "apktoolArgs=%apktoolArgs% "%~1""
    shift
    goto parse_args
)

:run_apktool
"%java_exe%" -jar %javaOpts% "%~dp0%BASENAME%%max%.jar" %fastCommand% %apktoolArgs%

rem Pause when ran non interactively
for %%i in (%cmdcmdline%) do if /i "%%~i"=="/c" pause & exit /b
