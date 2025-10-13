@echo off
echo ========================================
echo Database Reset Utility
echo ========================================
echo.
echo This will delete the mazebank.db file
echo WARNING: All data will be lost!
echo.
pause
echo.

if exist "mazebank.db" (
    del "mazebank.db"
    echo ✅ Database file deleted successfully!
    echo.
    echo The application will create a fresh database on next startup.
) else (
    echo ℹ️ Database file not found in current directory.
    echo It may be located elsewhere or already deleted.
)

echo.
pause