; Script generated by the Inno Setup Script Wizard.
; SEE THE DOCUMENTATION FOR DETAILS ON CREATING INNO SETUP SCRIPT FILES!

[Setup]
AppName=Greenfoot
AppVerName=Greenfoot 1.4.6
AppPublisher=Deakin University
AppPublisherURL=http://www.greenfoot.org
AppSupportURL=http://www.greenfoot.org
AppUpdatesURL=http://www.greenfoot.org
UninstallFilesDir={app}\uninst
DefaultDirName={sd}\Greenfoot
DefaultGroupName=Greenfoot
Compression=bzip/9
OutputBaseFilename=Greenfoot-windows-1.4.6
OutputDir=.

[Messages]
SetupWindowTitle=Greenfoot Installer
SetupAppTitle=Greenfoot Installer
SetupLdrStartupMessage=This installer will install %1. Do you wish to continue?
WelcomeLabel1=Welcome to the [name] Installer
WelcomeLabel2=This installer will install [name/ver] on your computer.%n%nIt is strongly recommended that you close all other applications you have running before continuing. This will help prevent any conflicts during the installation process.

[Tasks]
Name: "desktopicon"; Description: "Create a &desktop icon"; GroupDescription: "Additional icons:"; MinVersion: 4,4

[Icons]
Name: "{group}\Greenfoot"; Filename: "{app}\greenfoot.exe"; WorkingDir: "{app}"
Name: "{userdesktop}\Greenfoot"; Filename: "{app}\greenfoot.exe"; WorkingDir: "{app}"; Tasks: desktopicon
Name: "{group}\Select VM"; Filename: "{app}\greenfoot.exe"; WorkingDir: "{app}"; Parameters: "/select";
Name: "{app}\Select VM"; Filename: "{app}\greenfoot.exe"; WorkingDir: "{app}"; Parameters: "/select";

[InstallDelete]
Type: files; Name: "{app}\lib\extensions\submission.jar"

[Files]
Source: "..\install_tmp\*.*"; DestDir: "{app}"; Flags: recursesubdirs ignoreversion
Source: "..\winlaunch\gflaunch.exe"; DestDir: "{app}"; DestName: "greenfoot.exe"
Source: "..\winlaunch\Greenfoot-README.TXT"; DestDir: "{app}"; DestName: "README.TXT"

[Run]
Filename: "{app}\README.TXT"; Description: "View the README file"; Flags: postinstall shellexec skipifsilent
Filename: "{app}\greenfoot.exe"; WorkingDir: "{app}"; Parameters: "/select"; Description: "Launch Greenfoot"; Flags: postinstall nowait skipifsilent unchecked

