# Quick Fix Guide - Foreign Key Error

## Error You're Seeing
```
org.sqlite.SQLiteException: [SQLITE_ERROR] SQL error or missing database 
(foreign key mismatch - "ClientReports" referencing "Clients")
```

## ✅ Fix Applied
I've removed the foreign key constraint from the `ClientReports` table in `DatabaseDriver.java`.

## 🔧 What You Need to Do

### Option 1: Delete Existing Database (Recommended)
This is the cleanest solution if you don't have important data:

1. **Close the application** completely
2. **Find the database file**: Look for `mazebank.db` in:
   - Project root directory: `C:/Users/ashis/OneDrive/Documents/GitHub/wealthwise final old/`
   - Or run the application once to see where it creates the file
3. **Delete** `mazebank.db`
4. **Restart the application** - it will create a fresh database with the correct schema

### Option 2: Use the Batch Script
I've created a helper script:

1. Close the application
2. Double-click `DELETE_DATABASE.bat` in the project root
3. Follow the prompts
4. Restart the application

### Option 3: Manual SQL Fix (Advanced)
If you want to keep existing data:

1. Open the database with a SQLite browser
2. Run: `DROP TABLE IF EXISTS ClientReports;`
3. Restart the application

## 🧪 After the Fix

### Test the Application:
1. ✅ Application starts without errors
2. ✅ Login as admin works
3. ✅ Create client works (with new account format `3201 XXXX`)
4. ✅ Login as client works
5. ✅ Client can submit issue reports
6. ✅ Admin can view client reports

## 📋 Summary of All Changes Made

### 1. Fixed CreateClient Bug
- **File**: `CreateClientController.java`
- **Issue**: Field name mismatch (`savings_balance` vs `account_balance`)
- **Status**: ✅ Fixed

### 2. Updated Account Number Format
- **File**: `DatabaseDriver.java`
- **Change**: Account numbers now use format `3201 XXXX`
- **Status**: ✅ Implemented

### 3. Fixed Foreign Key Error
- **File**: `DatabaseDriver.java`
- **Change**: Removed foreign key constraint from `ClientReports` table
- **Status**: ✅ Fixed

## 🎯 Next Steps
1. Delete the database file (if it exists)
2. Restart the application
3. Test all features
4. Create a new client and verify account numbers show as `3201 XXXX`
5. Test the reports feature

## ❓ If You Still See Errors
If the error persists after deleting the database:
1. Make sure the application is completely closed
2. Check if there are multiple `mazebank.db` files
3. Rebuild the project in IntelliJ (Build → Rebuild Project)
4. Clear IntelliJ's cache (File → Invalidate Caches → Invalidate and Restart)

## 📞 Need Help?
If you encounter any issues, let me know and I'll help troubleshoot!