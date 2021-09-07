package com.usbtv.demo;

import android.app.Activity;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.usbtv.demo.data.Folder;
import com.usbtv.demo.data.VFile;

import java.sql.SQLException;
import java.util.List;

import app.com.tvrecyclerview.FocusHighlightHelper;
import app.com.tvrecyclerview.GridObjectAdapter;
import app.com.tvrecyclerview.OnChildSelectedListener;
import app.com.tvrecyclerview.OnChildViewHolderSelectedListener;
import app.com.tvrecyclerview.RowItem;
import app.com.tvrecyclerview.VerticalGridView;


public class RegularVerticalActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regular_vertical);
        VerticalGridView gridView = findViewById(R.id.id_grid_vertical);



        gridView.addItemDecoration(new SpaceItemDecoration());
        gridView.setNumColumns(3);
        GridObjectAdapter adapter = new GridObjectAdapter(new RegularVerticalPresenter(this));
        gridView.setFocusZoomFactor(FocusHighlightHelper.ZOOM_FACTOR_SMALL);

        try {
            List<Folder> list = App.getHelper().getDao(Folder.class).queryForAll();

            gridView.setAdapter(adapter);
            for (int i = 0; i < list.size(); i++) {

                adapter.add(list.get(i));
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }




    }

    protected static class SpaceItemDecoration extends RecyclerView.ItemDecoration {
        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            outRect.right = 30;
            outRect.bottom = 30;
        }
    }
}
