[Link to live](https://mermaid.live/edit#pako:eNq1Vttu2zAM_RVDTw6Q-QP8UGC9DAiQFAOCbS9-4SQm0WpLnkQHy4r--yhfkrS108Zd_GBAEnlIHl6kRyGtQpEKj78rNBJvNawdFJmJ-CvBkZa6BEPR3R-rKv-tVEC4RLfVEl8LfQX5AGtcgOG_W7BG_qbU4PnMrOyNLUqgRgQk6S1bH3al5yD6dHXVt59G0iEv7y3plZZA2po4SZIplGXerufaU-L1X5ychu-LOo3imfmFMuBMotLZrVb4-Tn2i7CGyes76bGcRmukmfEEeY6qPfMNRG5tySTXW3VcK-vuQG7Ssyx0sW0h152BWFPSAt9DgdPOSqsyOT-EI5pCDcRH6CPQ5lAZuZkZQkNfrOu8Hg3accBhu9A0nphrdIX2nh32CdnAbzwOlP09YNU4XI9hxw_w2uT1mUqX2vOpOphueN8vm8Rye5zLf25BzeEn5vHHqiLg3KKXTpd1p37YtY5wUOqIvohsvfKH9twT7JtxYB390LSJZZhMDq938WSc5UYJjTqlPaz-ws03cC6cmpkMORkDcxjyTbNas_6OLuTihi8mni0R-GPZ0W3FQ2VJ1l1gXLUTd2krJ_E_DKxugnejKsC8G_J0pfMwCSUOfffQvoLqin91DXLdX-8uW-i913SvrwrfcWseCfW-GMRUFNxEoBW_fx6DTiZogwVmgp0WCtxDJjLzxHJQkV3ujBQpuQqnoqqR2reSSFeQe95FpbnCFs2Dqn5XPf0DFVFQDA)

```mermaid
sequenceDiagram
    participant ExodusUpdateService
    participant PackageManagerModule
    participant PackageManager
    participant PackageInfoCompat
    activate ExodusUpdateService
    ExodusUpdateService ->> ExodusUpdateService: createNotification(...,applicationList.size)
    ExodusUpdateService ->> PackageManagerModule: (Injection) provideApplicationList
    activate PackageManagerModule
    PackageManagerModule ->> PackageManager: getInstalledPackages
    loop packageList.forEach:
    PackageManagerModule ->> PackageManagerModule: validPackage(it.packageName, packageManager)
    PackageManagerModule ->> PackageManager: getApplicationInfo(packageName)
    PackageManagerModule ->> PackageManager: getLaunchIntentForPackage(packageName)
    PackageManagerModule ->> PackageManagerModule: it.requestedPermissions.toList()
    PackageManagerModule ->> PackageManagerModule: getPermissionList(appPerms, packageManager)
    loop permissionList.forEach
    PackageManagerModule ->> PackageManager: getPermissionInfo(permissionName, ...)
    PackageManagerModule ->> PackageManager: loadLabel(packageManager)
    PackageManagerModule ->> PackageManager: loadDescription(permissionName, ...)
    PackageManagerModule ->> PackageManagerModule: add permission to permsList
    loop permsList.sortWith(compareBy())
    PackageManagerModule ->> PackageManagerModule: 
    end
    PackageManagerModule -->> PackageManagerModule: permsList
    end
    PackageManagerModule ->> PackageManager: loadLabel(packageManager)
    PackageManagerModule ->> PackageManager: loadIcon(packageManager)
    PackageManagerModule ->> PackageInfoCompat: getLongVersionCode(it as PackageInfo)
    PackageManagerModule ->> PackageManagerModule: getAppStore(it.packageName, packageManager)
    PackageManagerModule ->> PackageManager: getInstallSourceInfo(packageName)
    PackageManagerModule ->> PackageManager: getInstallerPackageName(packageName)
    PackageManagerModule -->> PackageManagerModule: add app to applicationList
    end
    loop applicationList.sortBy
    PackageManagerModule ->> PackageManagerModule: 
    end
    PackageManagerModule -->> ExodusUpdateService: applicationList
    deactivate PackageManagerModule
    deactivate ExodusUpdateService    
```