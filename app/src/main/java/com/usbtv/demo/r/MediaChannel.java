package com.usbtv.demo.r;

import java.util.ArrayList;
import java.util.List;

public class MediaChannel {
    private final String mName;
    private final String mDescription;
    private final String mMediaUri;
    private final String mBgImage;
    private final String mTitle;
    private List<MediaProgram> mPrograms;
    private boolean mChannelPublished;
    private long mChannelId;
    private boolean isDefault;

    MediaChannel(String name, List<MediaProgram> programs, boolean isDefault) {
        mName = name;
        mTitle = "playlist title";
        mDescription = "playlist description";
        mMediaUri = "dsf";
        mBgImage = "asdf";
        mPrograms = programs;
        this.isDefault = isDefault;
    }

    public String getName() {
        return mName;
    }

    public String getDescription() {
        return mDescription;
    }


    public void setChannelPublishedId(long channelId) {
        this.mChannelId = channelId;
    }

    public List<MediaProgram> getMediaPrograms() {
        return mPrograms;
    }


    public String getMediaUri() {
        return mMediaUri;
    }

    public String getBgImage() {
        return mBgImage;
    }

    public String getTitle() {
        return mTitle;
    }

    public List<MediaProgram> getPrograms() {
        return mPrograms;
    }

    public void setPrograms(List<MediaProgram> mPrograms) {
        this.mPrograms = mPrograms;
    }

    public boolean isChannelPublished() {
        return mChannelPublished;
    }

    public void setChannelPublished(boolean mChannelPublished) {
        this.mChannelPublished = mChannelPublished;
    }

    public long getChannelId() {
        return mChannelId;
    }

    public void setChannelId(long mChannelId) {
        this.mChannelId = mChannelId;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }
}



