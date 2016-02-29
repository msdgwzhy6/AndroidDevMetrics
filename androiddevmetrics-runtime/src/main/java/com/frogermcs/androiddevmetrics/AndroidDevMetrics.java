package com.frogermcs.androiddevmetrics;

import android.app.Application;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.frogermcs.androiddevmetrics.internal.metrics.ActivityLaunchMetrics;
import com.frogermcs.androiddevmetrics.internal.ui.ActivitiesMetricsFragment;
import com.frogermcs.androiddevmetrics.aspect.Dagger2GraphAnalyzer;
import com.frogermcs.androiddevmetrics.internal.metrics.ChoreographerMetrics;
import com.frogermcs.androiddevmetrics.internal.metrics.InitManager;
import com.frogermcs.androiddevmetrics.internal.ui.MetricsActivity;


/**
 * Created by Miroslaw Stanek on 25.01.2016.
 */
public class AndroidDevMetrics {
    private static int WARNING_1_LIMIT_MILLIS = 30;
    private static int WARNING_2_LIMIT_MILLIS = 50;
    private static int WARNING_3_LIMIT_MILLIS = 100;

    static volatile AndroidDevMetrics singleton;

    private Context context;
    private int warningLevel1, warningLevel2, warningLevel3;
    private boolean enableAcitivtyMetrics;
    private boolean showNotification;
    private boolean enableDagger2Metrics;

    public static AndroidDevMetrics DEBUG_initWith(Context context) {
        Builder androidDevMetricsBuilder = new Builder(context)
                .enableActivityMetrics(true)
                .enableDagger2Metrics(true)
                .showNotification(true);

        return initWith(androidDevMetricsBuilder);
    }

    public static AndroidDevMetrics initWith(Context context) {
        return initWith(new AndroidDevMetrics.Builder(context).build());
    }

    public static AndroidDevMetrics initWith(Builder builder) {
        return initWith(builder.build());
    }

    public static AndroidDevMetrics initWith(AndroidDevMetrics androidDevMetrics) {
        if (singleton == null) {
            synchronized (AndroidDevMetrics.class) {
                if (singleton == null) {
                    setAndroidDevMetrics(androidDevMetrics);
                }
            }
        }

        return singleton;
    }

    private static void setAndroidDevMetrics(AndroidDevMetrics androidDevMetrics) {
        singleton = androidDevMetrics;
        singleton.setupCapturing();
    }

    public static AndroidDevMetrics singleton() {
        if (singleton == null) {
            throw new IllegalStateException("Must Initialize Dagger2Metrics before using singleton()");
        } else {
            return singleton;
        }
    }

    AndroidDevMetrics(Context context) {
        this.context = context;
    }

    public int warningLevel1() {
        return warningLevel1;
    }

    public int warningLevel2() {
        return warningLevel2;
    }

    public int warningLevel3() {
        return warningLevel3;
    }

    private void setupCapturing() {
        Dagger2GraphAnalyzer.setEnabled(enableDagger2Metrics);

        InitManager.getInstance().initializedMetrics.clear();

        if (enableAcitivtyMetrics) {
            ActivityLaunchMetrics activityLaunchMetrics = ActivityLaunchMetrics.getInstance();
            ((Application) context.getApplicationContext()).registerActivityLifecycleCallbacks(activityLaunchMetrics);
            ChoreographerMetrics.getInstance().start();
        }

        if (showNotification) {
            showNotification();
        }
    }

    private void showNotification() {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_timeline_white_18dp)
                .setContentTitle(context.getString(R.string.adm_name))
                .setContentText(context.getString(R.string.adm_notification_content))
                .setAutoCancel(false);

        Intent resultIntent = new Intent(context, MetricsActivity.class);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify("AndroidDevMetrics".hashCode(), mBuilder.build());
    }

    public static class Builder {
        private final Context context;
        private int dagger2WarningLevel1 = WARNING_1_LIMIT_MILLIS;
        private int dagger2WarningLevel2 = WARNING_2_LIMIT_MILLIS;
        private int dagger2WarningLevel3 = WARNING_3_LIMIT_MILLIS;
        private boolean enableAcitivtyMetrics = false;
        private boolean showNotification = true;
        private boolean enableDagger2Metrics = true;

        public Builder(Context context) {
            if (context == null) {
                throw new IllegalArgumentException("Context must not be null.");
            } else {
                this.context = context.getApplicationContext();
            }
        }

        public Builder dagger2WarningLevelsMs(int warning1, int warning2, int warning3) {
            if (warning1 > warning2 || warning2 > warning3) {
                throw new IllegalArgumentException("Warning levels should be ascending");
            } else {
                this.dagger2WarningLevel1 = warning1;
                this.dagger2WarningLevel2 = warning2;
                this.dagger2WarningLevel3 = warning3;
            }

            return this;
        }

        public Builder enableActivityMetrics(boolean enable) {
            this.enableAcitivtyMetrics = enable;
            return this;
        }

        public Builder showNotification(boolean show) {
            this.showNotification = show;
            return this;
        }

        public Builder enableDagger2Metrics(boolean enable) {
            this.enableDagger2Metrics = enable;
            return this;
        }

        private AndroidDevMetrics build() {
            AndroidDevMetrics androidDevMetrics = new AndroidDevMetrics(context);
            androidDevMetrics.warningLevel1 = this.dagger2WarningLevel1;
            androidDevMetrics.warningLevel2 = this.dagger2WarningLevel2;
            androidDevMetrics.warningLevel3 = this.dagger2WarningLevel3;
            androidDevMetrics.enableAcitivtyMetrics = this.enableAcitivtyMetrics;
            androidDevMetrics.showNotification = this.showNotification;
            androidDevMetrics.enableDagger2Metrics = this.enableDagger2Metrics;
            return androidDevMetrics;
        }
    }


}
