package com.fhc.laser_monitor_sw_android_rtsp_app.utils;


import com.blankj.utilcode.util.ShellUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.blankj.utilcode.util.Utils;

/**
 * @ClassName: AdbUtil
 * @Description:
 * @Author: Lix
 * @CreateDate: 2020/6/8
 * @Version: 1.0
 */
public class AdbUtil {
    //通过命令cat /sys/class/net/eth0/carrier，如果插有网线的话，读取到的值是1，否则为0
    //通过命令ip route list table 0，查询网关
    public ShellUtils.CommandResult send(String s, boolean isRoot, boolean isNeedMsg) {
        return ShellUtils.execCmd(s, isRoot, isNeedMsg);
    }

    public ShellUtils.CommandResult send(String s, Boolean isRoot) {
        return ShellUtils.execCmd(s, isRoot, true);
    }

    public ShellUtils.CommandResult send(String s) {
        return ShellUtils.execCmd(s, true, true);
    }

    public void sendEthernetIp() {
        String s = "ifconfig eth0 192.168.137.99 broadcast 192.168.137.255 netmask 255.255.255.0";

        ShellUtils.execCmdAsync(s, true, true, new Utils.Consumer<ShellUtils.CommandResult>() {
            @Override
            public void accept(ShellUtils.CommandResult commandResult) {
//                if ("0".equals(commandResult.successMsg)) {
                ToastUtils.showShort("有线模式正在配置中请耐心等待...");
//                } else {
//                    ToastUtils.showShort("开启有线模式失败,请重新尝试开启");
//                }
            }
        });
    }
}
