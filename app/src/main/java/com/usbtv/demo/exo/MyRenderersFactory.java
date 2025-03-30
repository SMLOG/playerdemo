package com.usbtv.demo.exo;

import android.content.Context;
import android.os.Looper;

import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.DefaultRenderersFactory;
import androidx.media3.exoplayer.Renderer;
import androidx.media3.exoplayer.text.TextOutput;
import androidx.media3.exoplayer.text.TextRenderer;

import java.util.ArrayList;

@UnstableApi
public class MyRenderersFactory extends DefaultRenderersFactory {

    /**
     * @param context A {@link Context}.
     */
    public MyRenderersFactory(Context context) {
        super(context);
    }

    protected void buildTextRenderers(
            Context context,
            TextOutput output,
            Looper outputLooper,
            @DefaultRenderersFactory.ExtensionRendererMode int extensionRendererMode,
            ArrayList<Renderer> out) {
        TextRenderer textRenderer = new TextRenderer(output, outputLooper);
        textRenderer.experimentalSetLegacyDecodingEnabled(true);
        out.add(textRenderer);
    }
}
