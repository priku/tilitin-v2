; Tilitin 2.0 - Inno Setup Script
; Modern Windows installer for Tilitin accounting software

#define MyAppName "Tilitin"
#define MyAppVersion "2.0.4"
#define MyAppPublisher "Tilitin Project"
#define MyAppURL "https://github.com/priku/tilitin-modernized"
#define MyAppExeName "Tilitin 2.0.exe"
#define MyAppDescription "Ilmainen kirjanpito-ohjelma yrityksille ja yhdistyksille"

[Setup]
; NOTE: The value of AppId uniquely identifies this application.
AppId={{A1B2C3D4-E5F6-7890-ABCD-EF1234567890}
AppName={#MyAppName}
AppVersion={#MyAppVersion}
AppVerName={#MyAppName} {#MyAppVersion}
AppPublisher={#MyAppPublisher}
AppPublisherURL={#MyAppURL}
AppSupportURL={#MyAppURL}
AppUpdatesURL={#MyAppURL}/releases
DefaultDirName={autopf}\{#MyAppName}
DefaultGroupName={#MyAppName}
AllowNoIcons=yes
; License file
LicenseFile=..\COPYING
; Output settings
OutputDir=..\dist\installer
OutputBaseFilename=Tilitin-2.0.4-setup
; Compression
Compression=lzma2/ultra64
SolidCompression=yes
LZMAUseSeparateProcess=yes
; Windows version requirements
MinVersion=10.0
; Installation privileges
PrivilegesRequired=lowest
PrivilegesRequiredOverridesAllowed=dialog
; Uninstaller settings
UninstallDisplayIcon={app}\{#MyAppExeName}
UninstallDisplayName={#MyAppName}
; Modern appearance
WizardStyle=modern
WizardSizePercent=120
SetupIconFile=..\src\main\resources\tilitin.ico

[Languages]
Name: "finnish"; MessagesFile: "compiler:Languages\Finnish.isl"
Name: "english"; MessagesFile: "compiler:Default.isl"

[CustomMessages]
finnish.LaunchProgram=Käynnistä {#MyAppName}
finnish.CreateDesktopIcon=Luo pikakuvake työpöydälle
finnish.CreateStartMenuEntry=Luo pikakuvake Käynnistä-valikkoon
english.LaunchProgram=Launch {#MyAppName}
english.CreateDesktopIcon=Create a desktop shortcut
english.CreateStartMenuEntry=Create a Start Menu shortcut

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: unchecked
Name: "startmenuicon"; Description: "{cm:CreateStartMenuEntry}"; GroupDescription: "{cm:AdditionalIcons}"

[Files]
; Main application files (from jPackage output)
Source: "..\dist\windows\Tilitin 2.0\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs
; NOTE: Don't use "Flags: ignoreversion" on any shared system files

[Icons]
Name: "{group}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"
Name: "{group}\{cm:UninstallProgram,{#MyAppName}}"; Filename: "{uninstallexe}"
Name: "{autodesktop}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"; Tasks: desktopicon

[Run]
Filename: "{app}\{#MyAppExeName}"; Description: "{cm:LaunchProgram}"; Flags: nowait postinstall skipifsilent

[Code]
// Custom code for additional functionality

// Etsi ja poista aiempi Tilitin-asennus
function GetUninstallString(): String;
var
  sUnInstPath: String;
  sUnInstallString: String;
begin
  sUnInstPath := 'Software\Microsoft\Windows\CurrentVersion\Uninstall\{#SetupSetting("AppId")}_is1';
  sUnInstallString := '';
  
  // Tarkista HKCU (per-user asennus)
  if not RegQueryStringValue(HKCU, sUnInstPath, 'UninstallString', sUnInstallString) then
    // Tarkista HKLM (kaikille käyttäjille)
    RegQueryStringValue(HKLM, sUnInstPath, 'UninstallString', sUnInstallString);
  
  Result := sUnInstallString;
end;

function IsUpgrade(): Boolean;
begin
  Result := (GetUninstallString() <> '');
end;

function UninstallOldVersion(): Integer;
var
  sUnInstallString: String;
  iResultCode: Integer;
begin
  Result := 0;
  sUnInstallString := GetUninstallString();
  
  if sUnInstallString <> '' then begin
    sUnInstallString := RemoveQuotes(sUnInstallString);
    if Exec(sUnInstallString, '/SILENT /NORESTART /SUPPRESSMSGBOXES', '', SW_HIDE, ewWaitUntilTerminated, iResultCode) then
      Result := 3  // Onnistui
    else
      Result := 2; // Epäonnistui
  end else
    Result := 1;   // Ei löytynyt
end;

function InitializeSetup(): Boolean;
begin
  Result := True;
end;

procedure CurStepChanged(CurStep: TSetupStep);
var
  UninstallResult: Integer;
begin
  if CurStep = ssInstall then
  begin
    // Poista vanha versio ennen uuden asennusta
    if IsUpgrade() then
    begin
      UninstallResult := UninstallOldVersion();
      // Jatka asennusta riippumatta tuloksesta
    end;
  end;
end;
