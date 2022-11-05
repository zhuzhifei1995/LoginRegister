adb shell setprop noah.adb.shell.lock 000123f172853
adb shell setprop noah.adb.shell youxuepai
adb shell setprop noah.adb.shell.lock false
adb root
adb shell "echo 0 > /proc/rockchip/mount_flag"
#adb remount
adb shell setprop persist.sys.noah.adb.senable 1
adb shell setprop persist.noah.sysui.d.nohide 1
adb shell setprop noah.pmc.enable 1
adb shell setprop noah.pmc.installapk 1
adb shell setprop noah.pmc.factoryinstallapk 0
adb shell getprop noah.pmc.enable
adb shell getprop noah.pmc.installapk
adb shell getprop ro.noah.apkinstall
adb shell getprop noah.pmc.factoryinstallapk



