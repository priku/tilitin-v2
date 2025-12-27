; Tilitin 2.0 - Inno Setup Script
; Modern Windows installer for Tilitin accounting software

#define MyAppName "Tilitin 2.0"
#define MyAppVersion "2.0.1"
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
OutputBaseFilename=Tilitin-2.0.1-setup
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

function InitializeSetup(): Boolean;
begin
  Result := True;
end;

procedure CurStepChanged(CurStep: TSetupStep);
begin
  if CurStep = ssPostInstall then
  begin
    // Post-installation tasks can be added here
  end;
end;
