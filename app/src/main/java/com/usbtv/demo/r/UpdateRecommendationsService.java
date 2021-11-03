package com.usbtv.demo.r;

import android.app.IntentService;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Intent;

import com.usbtv.demo.MainActivity;
import com.usbtv.demo.R;
import com.usbtv.demo.data.Folder;

import java.io.IOException;
import java.util.List;

public class UpdateRecommendationsService extends IntentService {
    private static final String TAG = "UpdateRecommendationsService";
    private static final int MAX_RECOMMENDATIONS = 3;

    public UpdateRecommendationsService() {
        super("RecommendationService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {


        List<Folder> recommendations = VideoProvider.getMovieList("0");
        if (recommendations == null) return;

        int count = 0;

        try {
            RecommendationBuilder builder = new RecommendationBuilder()
                    .setContext(getApplicationContext())
                    .setSmallIcon(R.drawable.lb_ic_play);

            for (Folder movie : recommendations) {
                //Log.d(TAG, "Recommendation - " + movie.getTitle());

                builder.setBackground(movie.getCoverUrl())
                        .setId(count + 1)
                        .setPriority(MAX_RECOMMENDATIONS - count)
                        .setTitle(movie.getName())
                        .setDescription(movie.getName())
                        .setImage(movie.getCoverUrl())
                        .setIntent(buildPendingIntent(movie))
                        .build();

                if (++count >= MAX_RECOMMENDATIONS) {
                    break;
                }
            }
        } catch (IOException e) {
           // Log.e(TAG, "Unable to update recommendation", e);
        }
    }

    private PendingIntent buildPendingIntent(Folder movie) {
        Intent detailsIntent = new Intent(this, MainActivity.class);
        //detailsIntent.putExtra("Movie", movie);
        detailsIntent.putExtra("Movie",movie.getId());

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(detailsIntent);
        // Ensure a unique PendingIntents, otherwise all recommendations end up with the same
        // PendingIntent
        detailsIntent.setAction(Long.toString(movie.getId()));

        PendingIntent intent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        return intent;
    }
}