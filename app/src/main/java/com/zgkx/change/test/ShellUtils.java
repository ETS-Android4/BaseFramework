package com.zgkx.change.test;

import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author chtj
 * create by chtj on 2019-8-6
 * desc:adb Shell相关工具类
 * Command执行结果 CommandResult
 * <p>
 * 使用方式如下:
 * ShellUtils.CommandResult commandResult=ShellUtils.execCommand("reboot",true);//adb执行重启
 * 理论上commandResult.result=0时执行是成功的
 * Log.d(TAG, "commandResult errMeg="+commandResult.errorMsg+",result="+commandResult.result+",successMsg="+commandResult.successMsg);
 * <p>
 * 这里是常用的方法
 * --执行命令一条 {@link #execCommand(String command, boolean isRoot)}
 * --执行命令-多条 {@link #execCommand(String[] commands, boolean isRoot)}
 * --检查Root权限 {@link #isCheckRoot()}
 */
public class ShellUtils {
    private static final String TAG = "ShellUtils";

    public final static String COMMAND_SU = "su";
    public final static String COMMAND_SH = "sh";
    public final static String COMMAND_EXIT = "exit\n";
    public final static String COMMAND_LINE_END = "\n";

    /**
     * 检查是否拥有root权限
     * 执行shell adb 命令很多需要root权限
     * @return
     */
    public static boolean isCheckRoot() {
        File f = null;
        final String kSuSearchPaths[] = {"/system/bin/", "/system/xbin/", "/system/sbin/", "/sbin/", "/vendor/bin/"};
        try {
            for (int i = 0; i < kSuSearchPaths.length; i++) {
                f = new File(kSuSearchPaths[i] + "su");
                if (f != null && f.exists()) {
                    Log.d(TAG, "find su in : " + kSuSearchPaths[i]);
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Command执行结果
     */
    public static class CommandResult {
        public int result = -1;
        public String errorMsg;
        public String successMsg;
    }

    /**
     * 执行命令一条
     */
    public static CommandResult execCommand(String command, boolean isRoot) {
        String[] commands = {command};
        return execCommand(commands, isRoot);
    }

    /**
     * 执行命令-多条
     */
    public static CommandResult execCommand(String[] commands, boolean isRoot) {
        CommandResult commandResult = new CommandResult();
        if (commands == null || commands.length == 0) return commandResult;
        Process process = null;
        DataOutputStream os = null;
        BufferedReader successResult = null;
        BufferedReader errorResult = null;
        StringBuilder successMsg  = new StringBuilder();;
        StringBuilder errorMsg = new StringBuilder();
        try {
            process = Runtime.getRuntime().exec(isRoot ? COMMAND_SU : COMMAND_SH);
            os = new DataOutputStream(process.getOutputStream());
            for (String command : commands) {
                if (command != null) {
                    os.write(command.getBytes());
                    os.writeBytes(COMMAND_LINE_END);
                    os.flush();
                }
            }
            os.writeBytes(COMMAND_EXIT);
            os.flush();
            commandResult.result = process.waitFor();
            successResult = new BufferedReader(new InputStreamReader(process.getInputStream()));
            errorResult = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String s;
            while ((s = successResult.readLine()) != null) successMsg.append(s);
            while ((s = errorResult.readLine()) != null) errorMsg.append(s);
            commandResult.successMsg = successMsg.toString();
            commandResult.errorMsg = errorMsg.toString();
        } catch (IOException e) {
            errorMsg.append(e.getMessage());
            commandResult.errorMsg = errorMsg.toString();
        } catch (Exception e) {
            errorMsg.append(e.getMessage());
            commandResult.errorMsg = errorMsg.toString();
        } finally {
            try {
                if (os != null) os.close();
                if (successResult != null) successResult.close();
                if (errorResult != null) errorResult.close();
            } catch (IOException e) {
                String errmsg = e.getMessage();
                if (errmsg != null) {
                    Log.e(TAG, errmsg);
                } else {
                    e.printStackTrace();
                }
            }
            if (process != null) process.destroy();
        }
        return commandResult;
    }
}