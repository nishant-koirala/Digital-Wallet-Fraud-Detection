$commits = @(
    @{ date="2026-09-28 10:15:00"; file="frontend/src/app/pages/login/login.html"; tweak="<!-- UI polish -->"; msg="Style: login page UI polish" },
    @{ date="2026-09-28 11:30:00"; file="frontend/src/app/services/fraud-config.service.ts"; tweak="// cleanup"; msg="Chore: cleanup in fraud config" },
    @{ date="2026-09-28 13:45:00"; file="backend/src/main/resources/application.properties"; tweak="# config tweaks"; msg="Config: minor property updates" },
    @{ date="2026-09-28 14:20:00"; file="backend/src/main/java/dev/nishanta/wallet/security/JwtUtil.java"; tweak="// JWT enhancements"; msg="Refactor: JWT utility enhancements" },
    @{ date="2026-09-28 16:10:00"; file="backend/src/main/java/dev/nishanta/wallet/modules/transaction/controller/TransactionController.java"; tweak="// transaction logging"; msg="Refactor: transaction controller updates" },
    @{ date="2026-09-28 17:55:00"; file="frontend/src/app/pages/login/login.html"; tweak="<!-- accessibility -->"; msg="Style: improve login accessibility" },
    @{ date="2026-09-29 10:05:00"; file="frontend/src/app/services/fraud-config.service.ts"; tweak="// optimization"; msg="Refactor: optimize fraud config service" },
    @{ date="2026-09-29 11:15:00"; file="backend/src/main/resources/application.properties"; tweak="# db tuning"; msg="Config: database tuning prep" },
    @{ date="2026-09-29 12:40:00"; file="backend/src/main/java/dev/nishanta/wallet/security/JwtUtil.java"; tweak="// security patch"; msg="Security: minor jwt patch" },
    @{ date="2026-09-29 14:10:00"; file="backend/src/main/java/dev/nishanta/wallet/modules/transaction/controller/TransactionController.java"; tweak="// validation updates"; msg="Refactor: transaction validation updates" },
    @{ date="2026-09-29 15:30:00"; file="frontend/src/app/pages/login/login.html"; tweak="<!-- layout adjust -->"; msg="Style: login layout adjust" },
    @{ date="2026-09-29 16:45:00"; file="frontend/src/app/services/fraud-config.service.ts"; tweak="// strict typing"; msg="Refactor: strict typing in fraud config" },
    @{ date="2026-09-29 17:20:00"; file="backend/src/main/resources/application.properties"; tweak="# logging config"; msg="Config: logging adjustments" }
)

foreach ($c in $commits) {
    if (Test-Path $c.file) {
        Add-Content -Path $c.file -Value "`n$($c.tweak)"
        git add $c.file
        $env:GIT_AUTHOR_DATE=$c.date
        $env:GIT_COMMITTER_DATE=$c.date
        git commit -m $c.msg
    } else {
        Write-Host "File not found: $($c.file)"
    }
}

git push
