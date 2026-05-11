@echo off
for /d %%i in (*) do (
    if exist "%%i\gradle.lockfile" (
        echo Running dependencies task for %%i
        gradlew.bat :%%i:dependencies --write-locks
    )
)
