package com.usbtv.demo.r;

import android.os.Parcel;
import android.os.Parcelable;

public class MediaProgram implements Parcelable {
    private final String mMediaProgramId;
    private final String mContentId;
    private final String mTitle;
    private final String mDescription;
    private final String mBgImageUrl;
    private final String mCardImageUrl;
    private final String mMediaUrl;
    private final String mPreviewMediaUrl;
    private final String mCategory;
    private long mProgramId;
    private int mViewCount;

    MediaProgram(String title, String description, String bgImageUrl, String cardImageUrl,
                 String category, String mediaProgramId,long programId, String contentId) {
        mMediaProgramId = mediaProgramId;
        mContentId = contentId;
        mTitle = title;
        mDescription = description;
        mBgImageUrl = bgImageUrl;
        mCardImageUrl = cardImageUrl;
        mMediaUrl = "";
        mPreviewMediaUrl = "";
        mCategory = category;
        this.mProgramId = programId;
    }


    protected MediaProgram(Parcel in) {
        mMediaProgramId = in.readString();
        mContentId = in.readString();
        mTitle = in.readString();
        mDescription = in.readString();
        mBgImageUrl = in.readString();
        mCardImageUrl = in.readString();
        mMediaUrl = in.readString();
        mPreviewMediaUrl = in.readString();
        mCategory = in.readString();
        mProgramId = in.readLong();
        mViewCount = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mMediaProgramId);
        dest.writeString(mContentId);
        dest.writeString(mTitle);
        dest.writeString(mDescription);
        dest.writeString(mBgImageUrl);
        dest.writeString(mCardImageUrl);
        dest.writeString(mMediaUrl);
        dest.writeString(mPreviewMediaUrl);
        dest.writeString(mCategory);
        dest.writeLong(mProgramId);
        dest.writeInt(mViewCount);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<MediaProgram> CREATOR = new Creator<MediaProgram>() {
        @Override
        public MediaProgram createFromParcel(Parcel in) {
            return new MediaProgram(in);
        }

        @Override
        public MediaProgram[] newArray(int size) {
            return new MediaProgram[size];
        }
    };

    public String getMediaProgramId() {
        return mMediaProgramId;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getDescription() {
        return mDescription;
    }

    public String getCardImageUrl() {
        return mCardImageUrl;
    }

    public String getPreviewMediaUrl() {
        return mPreviewMediaUrl;
    }

    public void setProgramId(long parseId) {
        this.mProgramId =parseId;
    }

    public String getContentId() {
        return mContentId;
    }

    public long getProgramId() {
        return mProgramId;
    }
}