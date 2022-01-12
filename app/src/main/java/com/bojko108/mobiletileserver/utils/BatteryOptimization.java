package com.bojko108.mobiletileserver.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

/**
 * Get a dialog that informs the user to disable battery optimization for your app.
 * <p>
 * Use the dialog like that:
 * final AlertDialog dialog = BatteryOptimizationUtil.getBatteryOptimizationDialog(context);
 * if(dialog != null) dialog.show();
 * <p>
 * Alter the dialog texts so that they fit your needs. You can provide additional actions that
 * should be performed if the positive or negative button are clicked by using the provided method:
 * getBatteryOptimizationDialog(Context, OnBatteryOptimizationAccepted, OnBatteryOptimizationCanceled)
 * <p>
 * Source: https://gist.github.com/moopat/e9735fa8b5cff69d003353a4feadcdbc
 * <p>
 *
 * @author Markus Deutsch @moopat
 */
public class BatteryOptimization {

    public static void openSettings(Context context) {
        /*
         * If there is no resolvable component return right away. We do not use
         * isBatteryOptimizationAvailable() for this check in order to avoid checking for
         * resolvable components twice.
         */
        final ComponentName componentName = getResolveableComponentName(context);
        if (componentName == null) return;

        final Intent intent = new Intent();
        intent.setComponent(componentName);
        context.startActivity(intent);
    }

    /**
     * Get the battery optimization dialog.
     * By default the dialog will send the user to the relevant activity if the positive button is
     * clicked, and closes the dialog if the negative button is clicked.
     *
     * @param context Context
     * @return the dialog or null if battery optimization is not available on this device
     */
    @Nullable
    public static AlertDialog getBatteryOptimizationDialog(final Context context) {
        return getBatteryOptimizationDialog(context,
                "Open Battery Optimization", "This app needs to be removed from battery optimizations",
                "Cancel", "Ok",
                null, null);
    }

    /**
     * Get the battery optimization dialog.
     * By default the dialog will send the user to the relevant activity if the positive button is
     * clicked, and closes the dialog if the negative button is clicked. Callbacks can be provided
     * to perform additional actions on either button click.
     *
     * @param context          Context
     * @param positiveCallback additional callback for the positive button. can be null.
     * @param negativeCallback additional callback for the negative button. can be null.
     * @return the dialog or null if battery optimization is not available on this device
     */
    @Nullable
    public static AlertDialog getBatteryOptimizationDialog(
            final Context context,
            final String title,
            final String message,
            final String negativeButtonText,
            final String positiveButtonText,
            @Nullable final OnBatteryOptimizationAccepted positiveCallback,
            @Nullable final OnBatteryOptimizationCanceled negativeCallback) {
        /*
         * If there is no resolvable component return right away. We do not use
         * isBatteryOptimizationAvailable() for this check in order to avoid checking for
         * resolvable components twice.
         */
        final ComponentName componentName = getResolveableComponentName(context);
        if (componentName == null) return null;

        return new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setNegativeButton(negativeButtonText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (negativeCallback != null)
                            negativeCallback.onBatteryOptimizationCanceled();
                    }
                })
                .setPositiveButton(positiveButtonText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (positiveCallback != null)
                            positiveCallback.onBatteryOptimizationAccepted();

                        final Intent intent = new Intent();
                        intent.setComponent(componentName);
                        context.startActivity(intent);
                    }
                }).create();
    }

    /**
     * Find out if battery optimization settings are available on this device.
     *
     * @param context Context
     * @return true if battery optimization is available
     */
    public static boolean isBatteryOptimizationAvailable(final Context context) {
        return getResolveableComponentName(context) != null;
    }

    @Nullable
    private static ComponentName getResolveableComponentName(final Context context) {
        for (ComponentName componentName : getComponentNames()) {
            final Intent intent = new Intent();
            intent.setComponent(componentName);
            if (context.getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null)
                return componentName;
        }
        return null;
    }

    /**
     * Get a list of all known ComponentNames that provide battery optimization on different
     * devices.
     * Based on Shivam Oberoi's answer on StackOverflow: https://stackoverflow.com/a/48166241/2143225
     *
     * @return list of ComponentName
     */
    private static List<ComponentName> getComponentNames() {
        final List<ComponentName> names = new ArrayList<>();
        names.add(new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity"));
        names.add(new ComponentName("com.letv.android.letvsafe", "com.letv.android.letvsafe.AutobootManageActivity"));
        names.add(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"));
        names.add(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity"));
        names.add(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.appcontrol.activity.StartupAppControlActivity"));
        names.add(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity"));
        names.add(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.startupapp.StartupAppListActivity"));
        names.add(new ComponentName("com.oppo.safe", "com.oppo.safe.permission.startup.StartupAppListActivity"));
        names.add(new ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity"));
        names.add(new ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager"));
        names.add(new ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"));
        names.add(new ComponentName("com.samsung.android.lool", "com.samsung.android.sm.ui.battery.BatteryActivity"));
        names.add(new ComponentName("com.htc.pitroad", "com.htc.pitroad.landingpage.activity.LandingPageActivity"));
        names.add(new ComponentName("com.asus.mobilemanager", "com.asus.mobilemanager.MainActivity"));

        return names;
    }

    public interface OnBatteryOptimizationAccepted {

        /**
         * Called if the user clicks the "OK" button of the battery optimization dialog. This does
         * not mean that the user has performed the necessary steps to exclude the app from
         * battery optimizations.
         */
        void onBatteryOptimizationAccepted();

    }

    public interface OnBatteryOptimizationCanceled {

        /**
         * Called if the user clicks the "Cancel" button of the battery optimization dialog.
         */
        void onBatteryOptimizationCanceled();

    }

}
