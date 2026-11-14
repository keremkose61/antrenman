if (!([Security.Principal.WindowsPrincipal][Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)) {
    Start-Process PowerShell -Verb RunAs "-NoProfile -ExecutionPolicy Bypass -Command `"cd '$pwd'; & '$PSCommandPath'`"";
    exit;
}

Write-Host "WAMP Server için Port 80 Windows Güvenlik Duvarı İzni Ekleniyor..."
netsh advfirewall firewall show rule name="WAMP Server Port 80" >$null 2>&1
if ($LASTEXITCODE -ne 0) {
    netsh advfirewall firewall add rule name="WAMP Server Port 80" dir=in action=allow protocol=TCP localport=80
    Write-Host "Kural başarıyla eklendi!" -ForegroundColor Green
} else {
    Write-Host "Kural zaten mevcut." -ForegroundColor Yellow
}

Write-Host "Devam etmek için bir tuşa basın..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
