# Account Number Format Update

## Requirement
Account numbers for both **Wallet** and **Savings** accounts must follow this format:
- **Format**: `3201 XXXX`
- **Fixed Part**: `3201` (always the same)
- **Variable Part**: `XXXX` (unique 4-digit number)

## Examples
- First account: `3201 0001`
- Second account: `3201 0002`
- Third account: `3201 0003`
- ...
- Tenth account: `3201 0010`
- Hundredth account: `3201 0100`
- Thousandth account: `3201 1000`

## Implementation

### Modified Method
**File**: `DatabaseDriver.java`  
**Method**: `getNextAccountNumber(String table)`

### Changes Made

#### Before:
```java
public String getNextAccountNumber(String table) {
    try (Statement stmt = connection.createStatement();
         ResultSet rs = stmt.executeQuery("SELECT MAX(ID) FROM " + table)) {
        if (rs.next()) {
            int next = rs.getInt(1) + 1;
            return String.format("%04d", next);  // Returns: 0001, 0002, etc.
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return "0001";
}
```

#### After:
```java
public String getNextAccountNumber(String table) {
    try (Statement stmt = connection.createStatement();
         ResultSet rs = stmt.executeQuery("SELECT MAX(ID) FROM " + table)) {
        if (rs.next()) {
            int next = rs.getInt(1) + 1;
            // Format: 3201 XXXX (3201 is fixed, XXXX is unique 4-digit number)
            return String.format("3201 %04d", next);  // Returns: 3201 0001, 3201 0002, etc.
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return "3201 0001";
}
```

## Impact
This change affects:
1. ✅ **Wallet Account Creation** - All new wallet accounts will have format `3201 XXXX`
2. ✅ **Savings Account Creation** - All new savings accounts will have format `3201 XXXX`
3. ✅ **Client Profile Display** - Account numbers will display in the new format
4. ✅ **Uniqueness** - Each account gets a unique 4-digit suffix

## How It Works
1. The method queries the database for the maximum ID in the specified table (WalletAccounts or SavingsAccounts)
2. Increments the ID by 1 to get the next unique number
3. Formats it as `3201 XXXX` where XXXX is zero-padded to 4 digits
4. Returns the formatted account number

## Testing
After restarting the application:

### Test 1: Create New Client
1. Login as admin
2. Click "Create Client"
3. Fill in client details
4. Create the client
5. Login as the new client
6. Go to Profile
7. **Verify**: Wallet account number shows as `3201 XXXX`
8. **Verify**: Savings account number shows as `3201 XXXX`

### Test 2: Multiple Clients
1. Create multiple clients
2. Check that each gets unique account numbers:
   - Client 1: `3201 0001` (Wallet), `3201 0001` (Savings)
   - Client 2: `3201 0002` (Wallet), `3201 0002` (Savings)
   - Client 3: `3201 0003` (Wallet), `3201 0003` (Savings)

## Note
- **Existing accounts** in the database will retain their old format
- **New accounts** created after this update will use the new format `3201 XXXX`
- The space between `3201` and the 4-digit number is intentional for readability

## Files Modified
- `src/main/java/com/smartfinance/Models/DatabaseDriver.java`

## Status
✅ **IMPLEMENTED** - Account numbers now follow the `3201 XXXX` format