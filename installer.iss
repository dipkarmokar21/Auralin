[Setup]
AppName=Auralin Music Player
AppVersion=1.0
AppPublisher=Dip Karmokar
DefaultDirName={autopf}\AuralinPlayer
DefaultGroupName=Auralin Music Player
OutputDir=installer-output
OutputBaseFilename=AuralinPlayerSetup
Compression=lzma
SolidCompression=yes
WizardStyle=modern
LicenseFile=LICENSE.txt
SetupIconFile=AuralinPlayer.ico
UninstallDisplayIcon={app}\AuralinPlayer.ico

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"

[Files]
Source: "AuralinPlayer.exe"; DestDir: "{app}"; Flags: ignoreversion
Source: "AuralinPlayer.ico"; DestDir: "{app}"; Flags: ignoreversion
Source: "runtime\*"; DestDir: "{app}\runtime"; Flags: ignoreversion recursesubdirs createallsubdirs
Source: "D:\javafx\javafx-sdk-25.0.2\bin\*.dll"; DestDir: "{app}\runtime\bin"; Flags: ignoreversion

[Icons]
Name: "{group}\Auralin Music Player"; Filename: "{app}\AuralinPlayer.exe"; IconFilename: "{app}\AuralinPlayer.ico"
Name: "{commondesktop}\Auralin Music Player"; Filename: "{app}\AuralinPlayer.exe"; IconFilename: "{app}\AuralinPlayer.ico"

[Registry]
; Register app
Root: HKCR; Subkey: "AuralinPlayer"; ValueType: string; ValueName: ""; ValueData: "Auralin Music Player"; Flags: uninsdeletekey
Root: HKCR; Subkey: "AuralinPlayer\DefaultIcon"; ValueType: string; ValueName: ""; ValueData: "{app}\AuralinPlayer.exe,0"
Root: HKCR; Subkey: "AuralinPlayer\shell\open\command"; ValueType: string; ValueName: ""; ValueData: """{app}\AuralinPlayer.exe"" ""%1"""

; Associate with .mp3
Root: HKCR; Subkey: ".mp3\OpenWithProgids"; ValueType: string; ValueName: "AuralinPlayer"; ValueData: ""; Flags: uninsdeletevalue

; Register in "Open with" list
Root: HKLM; Subkey: "SOFTWARE\RegisteredApplications"; ValueType: string; ValueName: "Auralin Music Player"; ValueData: "Software\Clients\Media\AuralinPlayer\Capabilities"; Flags: uninsdeletevalue
Root: HKLM; Subkey: "SOFTWARE\Clients\Media\AuralinPlayer\Capabilities"; ValueType: string; ValueName: "ApplicationName"; ValueData: "Auralin Music Player"
Root: HKLM; Subkey: "SOFTWARE\Clients\Media\AuralinPlayer\Capabilities"; ValueType: string; ValueName: "ApplicationDescription"; ValueData: "A modern music player"
Root: HKLM; Subkey: "SOFTWARE\Clients\Media\AuralinPlayer\Capabilities\FileAssociations"; ValueType: string; ValueName: ".mp3"; ValueData: "AuralinPlayer"

; Friendly name in "Open with" menu
Root: HKCR; Subkey: "Applications\AuralinPlayer.exe"; ValueType: string; ValueName: "FriendlyAppName"; ValueData: "Auralin Music Player"
Root: HKCR; Subkey: "Applications\AuralinPlayer.exe\shell\open\command"; ValueType: string; ValueName: ""; ValueData: """{app}\AuralinPlayer.exe"" ""%1"""

[Run]
Filename: "{app}\AuralinPlayer.exe"; Description: "Launch Auralin Music Player"; Flags: nowait postinstall skipifsilent
