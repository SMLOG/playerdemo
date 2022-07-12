package com.usbtv.demo.r;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.core.app.NotificationCompat;

import com.usbtv.demo.R;

import java.io.IOException;

public class RecommendationBuilder {
    private String mTitle;
    private String mDescription;
    private String mImageUri;
    private String mBackgroundUri;
    private Context mContext;
    private int mPriority;
    private Bitmap mImage;
    private int mSmallIcon;
    private PendingIntent mIntent;
    private Bundle extras;
    private int id;

    public RecommendationBuilder setTitle(String title) {
        this.mTitle = title;
        return this;
    }

    public RecommendationBuilder setDescription(String description) {
        this.mDescription = description;
        return this;
    }

    public RecommendationBuilder setImage(String uri) {
        this.mImageUri = uri;
        return this;
    }

    public RecommendationBuilder setBackground(String uri) {
        this.mBackgroundUri = uri;
        return this;
    }
    public RecommendationBuilder setContext(Context context) {
        this.mContext = context;
        return this;
    }
    public RecommendationBuilder setPriority(int priority) {
        this.mPriority = priority;
        return this;
    }
    public RecommendationBuilder setIntent(PendingIntent intent) {
        this.mIntent = intent;
        return this;
    }
    public RecommendationBuilder setImage(Bitmap image) {
        this.mImage = image;
        return this;
    }

    public RecommendationBuilder setSmallIcon(int smallIcon) {
        this.mSmallIcon = smallIcon;
        return this;
    }
    public RecommendationBuilder setExtras(Bundle extras) {
        this.extras = extras;
        return this;
    }
    public Notification build() throws IOException {

        Notification notification = new NotificationCompat.BigPictureStyle(
                new NotificationCompat.Builder(mContext)
                        .setContentTitle(mTitle)
                        .setContentText(mDescription)
                        .setPriority(mPriority)
                        .setLocalOnly(true)
                        .setOngoing(true)
                        .setColor(mContext.getResources().getColor(R.color.bg_gray))
                        .setCategory(Notification.CATEGORY_RECOMMENDATION)
                        .setLargeIcon(mImage)
                        .setSmallIcon(mSmallIcon)
                        .setContentIntent(mIntent)
                        .setExtras(extras))
                .build();

        return notification;
    }

    public RecommendationBuilder setId(int id) {
        this.id = id;
        return this;
    }

    public int getId() {
        return id;
    }
}
