package com.usbtv.demo.r;


import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.DrawableRes;
import androidx.annotation.WorkerThread;
import androidx.tvprovider.media.tv.Channel;
import androidx.tvprovider.media.tv.ChannelLogoUtils;
import androidx.tvprovider.media.tv.PreviewProgram;
import androidx.tvprovider.media.tv.TvContractCompat;

import com.usbtv.demo.App;
import com.usbtv.demo.MainActivity;
import com.usbtv.demo.R;

import java.util.List;


/**
 * Created by rogera on 2017/12/30.
 */
public class MediaTVProvider {

    private static final String TAG = "MediaTVProvider";

    private static final String SCHEME = "tvmediachannels";
    private static final String APPS_LAUNCH_HOST = "com.google.android.tvmediachannels";
    private static final String PLAY_MEDIA_ACTION_PATH = "startApp";
    private static final String START_APP_ACTION_PATH = "startApp";

    private static final Uri PREVIEW_PROGRAMS_CONTENT_URI =
            Uri.parse("content://android.media.tv/preview_program");

    static private String createInputId(Context context) {
        ComponentName cName = new ComponentName(context, MainActivity.class.getName());
        return TvContractCompat.buildInputId(cName);
    }

    @WorkerThread
    static long addChannel(Context context, MediaChannel mediaChannel) {
        String channelInputId = createInputId(context);
        Channel channel = new Channel.Builder()
                .setDisplayName(mediaChannel.getName())
                .setDescription(mediaChannel.getDescription())
                .setType(TvContractCompat.Channels.TYPE_PREVIEW)
                .setInputId(channelInputId)
                .setAppLinkIntentUri(Uri.parse(SCHEME + "://" + APPS_LAUNCH_HOST
                        + "/" + START_APP_ACTION_PATH))
                .setInternalProviderId("" + mediaChannel.getChannelId())
                .build();

        Uri channelUri = context.getContentResolver().insert(TvContractCompat.Channels.CONTENT_URI,
                channel.toContentValues());
        if (channelUri == null || channelUri.equals(Uri.EMPTY)) {
            Log.e(TAG, "addChannel Insert channel failed");
            return 0;
        }
        long channelId = ContentUris.parseId(channelUri);
        mediaChannel.setChannelPublishedId(channelId);

        writeChannelLogo(context, channelId, R.mipmap.ic_launcher);

        List<MediaProgram> programs = mediaChannel.getMediaPrograms();

        int weight = programs.size();
        for (int i = 0; i < programs.size(); ++i, --weight) {
            MediaProgram mp = programs.get(i);
            final String mediaProgramId = mp.getMediaProgramId();
            final String contentId = mp.getContentId();


            Intent intent = new Intent();
            intent.setClass(App.getInstance().getApplicationContext(), MainActivity.class);
            //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("Movie", mp.getProgramId());
            //String intentUri = intent.toUri(Intent.URI_INTENT_SCHEME);


            PreviewProgram program = new PreviewProgram.Builder()
                    .setChannelId(channelId)
                    .setTitle(mp.getTitle())
                    .setDescription(mp.getDescription())
                    .setPosterArtUri(Uri.parse(mp.getCardImageUrl()==null?"":mp.getCardImageUrl()))
                    //.setIntentUri(Uri.parse(SCHEME + "://" + APPS_LAUNCH_HOST + "/" + PLAY_MEDIA_ACTION_PATH + "/" + mediaProgramId))
                    .setIntent(intent)
                    //.setPreviewVideoUri(Uri.parse(mp.getPreviewMediaUrl()))
                    .setInternalProviderId(mediaProgramId)
                    .setContentId(contentId)
                    .setWeight(weight)
                    .setType(TvContractCompat.PreviewPrograms.TYPE_CLIP)
                    .build();

            Uri programUri = context.getContentResolver().insert(PREVIEW_PROGRAMS_CONTENT_URI,
                    program.toContentValues());
            if (programUri == null || programUri.equals(Uri.EMPTY)) {
                Log.e(TAG, "addChannel Insert program failed");
            } else {
                mp.setProgramId(ContentUris.parseId(programUri));
            }
            if(mediaChannel.isDefault())
                TvContractCompat.requestChannelBrowsable(context, channelId);

        }
        return channelId;
    }

    @WorkerThread
    static void deleteChannel(Context context, long channelId) {
        int rowsDeleted = context.getContentResolver().delete(
                TvContractCompat.buildChannelUri(channelId), null, null);
        if (rowsDeleted < 1) {
            Log.e(TAG, "Delete channel failed");
        }
    }

    @WorkerThread
    public static void deleteProgram(Context context, MediaProgram program) {
        deleteProgram(context, program.getProgramId());
    }

    @WorkerThread
    static void deleteProgram(Context context, long programId) {
        int rowsDeleted = context.getContentResolver().delete(
                TvContractCompat.buildPreviewProgramUri(programId), null, null);
        if (rowsDeleted < 1) {
            Log.e(TAG, "Delete program failed");
        }
    }

    /**
     * Writes a drawable as the channel logo.
     *
     * @param channelId  identifies the channel to write the logo.
     * @param drawableId resource to write as the channel logo. This must be a bitmap and not, say
     *                   a vector drawable.
     */
    @WorkerThread
    static private void writeChannelLogo(Context context, long channelId,
                                         @DrawableRes int drawableId) {
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), drawableId);
        ChannelLogoUtils.storeChannelLogo(context, channelId, bitmap);
    }

    @WorkerThread
    static void updateMediaProgram(Context context, MediaProgram mediaProgram) {
        long programId = mediaProgram.getProgramId();
        Uri programUri = TvContractCompat.buildPreviewProgramUri(programId);
        try (Cursor cursor = context.getContentResolver().query(programUri, null, null, null,
                null)) {
            if (!cursor.moveToFirst()) {
                Log.e(TAG, "Update program failed");
            }
            PreviewProgram porgram = PreviewProgram.fromCursor(cursor);
            PreviewProgram.Builder builder = new PreviewProgram.Builder(porgram)
                    .setTitle(mediaProgram.getTitle());

            int rowsUpdated = context.getContentResolver().update(programUri,
                    builder.build().toContentValues(), null, null);
            if (rowsUpdated < 1) {
                Log.e(TAG, "Update program failed");
            }
        }
    }

    static void publishProgram(Context context, MediaProgram mediaProgram, long channelId, int weight) {
        final String mediaProgramId = mediaProgram.getMediaProgramId();

        PreviewProgram program = new PreviewProgram.Builder()
                .setChannelId(channelId)
                .setTitle(mediaProgram.getTitle())
                .setDescription(mediaProgram.getDescription())
                .setPosterArtUri(Uri.parse(mediaProgram.getCardImageUrl()))
                .setIntentUri(Uri.parse(SCHEME + "://" + APPS_LAUNCH_HOST
                        + "/" + PLAY_MEDIA_ACTION_PATH + "/" + mediaProgramId))
                .setPreviewVideoUri(Uri.parse(mediaProgram.getPreviewMediaUrl()))
                .setInternalProviderId(mediaProgramId)
                .setWeight(weight)
                .setType(TvContractCompat.PreviewPrograms.TYPE_MOVIE)
                .build();

        Uri programUri = context.getContentResolver().insert(PREVIEW_PROGRAMS_CONTENT_URI,
                program.toContentValues());
        if (programUri == null || programUri.equals(Uri.EMPTY)) {
            Log.e(TAG, "Insert program failed");
            return;
        }
        mediaProgram.setProgramId(ContentUris.parseId(programUri));
    }

    @WorkerThread
    static void setProgramViewCount(Context context, long programId, int numberOfViews) {
        Uri programUri = TvContractCompat.buildPreviewProgramUri(programId);
        try (Cursor cursor = context.getContentResolver().query(programUri, null, null, null,
                null)) {
            if (!cursor.moveToFirst()) {
                return;
            }
            PreviewProgram existingProgram = PreviewProgram.fromCursor(cursor);
            PreviewProgram.Builder builder = new PreviewProgram.Builder(existingProgram)
                    .setInteractionCount(numberOfViews)
                    .setInteractionType(TvContractCompat.PreviewProgramColumns
                            .INTERACTION_TYPE_VIEWS);
            int rowsUpdated = context.getContentResolver().update(
                    TvContractCompat.buildPreviewProgramUri(programId),
                    builder.build().toContentValues(), null, null);
            if (rowsUpdated != 1) {
                Log.e(TAG, "Update program failed");
            }
        }
    }
}
