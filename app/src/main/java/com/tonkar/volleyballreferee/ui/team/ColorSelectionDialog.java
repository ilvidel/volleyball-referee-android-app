package com.tonkar.volleyballreferee.ui.team;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.ui.util.UiUtils;

import androidx.appcompat.app.AlertDialog;

public abstract class ColorSelectionDialog {

    private AlertDialog mAlertDialog;

    protected ColorSelectionDialog(LayoutInflater layoutInflater, Context context, String title, String[] colorsStr, int selectedColor) {
        int[] colors = new int[colorsStr.length];
        for (int index = 0; index < colorsStr.length; index++) {
            colors[index] = Color.parseColor(colorsStr[index]);
        }

        int pixels = context.getResources().getDimensionPixelSize(R.dimen.default_margin_size);
        final GridView gridView = new GridView(context);
        gridView.setNumColumns(5);
        gridView.setHorizontalSpacing(pixels);
        gridView.setVerticalSpacing(pixels);
        gridView.setGravity(Gravity.CENTER);
        gridView.setPadding(pixels, 2 * pixels, pixels, 2 * pixels);
        ColorSelectionAdapter colorSelectionAdapter = new ColorSelectionAdapter(layoutInflater, context, colors, selectedColor) {
            @Override
            public void onColorSelected(int selectedColor) {
                ColorSelectionDialog.this.onColorSelected(selectedColor);

                if (mAlertDialog != null) {
                    mAlertDialog.dismiss();
                }
            }
        };
        gridView.setAdapter(colorSelectionAdapter);

        final AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AppTheme_Dialog);
        builder.setTitle(title).setView(gridView);
        builder.setNegativeButton(android.R.string.no, (dialog, which) -> {});

        mAlertDialog = builder.create();
    }

    public void show() {
        if (mAlertDialog != null) {
            mAlertDialog.show();
        }
    }

    public abstract void onColorSelected(int selectedColor);

    private abstract class ColorSelectionAdapter extends BaseAdapter {

        private final LayoutInflater mLayoutInflater;
        private final Context        mContext;
        private final int            mSelectedColor;
        private final int[]          mColors;

        ColorSelectionAdapter(LayoutInflater layoutInflater, Context context, int[] colors, int selectedColor) {
            mLayoutInflater = layoutInflater;
            mContext = context;
            mSelectedColor = selectedColor;
            mColors = colors;
        }

        @Override
        public int getCount() {
            return mColors.length;
        }

        @Override
        public Object getItem(int index) {
            return null;
        }

        @Override
        public long getItemId(int index) {
            return 0;
        }

        @Override
        public View getView(int index, View convertView, ViewGroup viewGroup) {
            FloatingActionButton button;

            if (convertView == null) {
                button = (FloatingActionButton) mLayoutInflater.inflate(R.layout.color_item, null);
                int pixels = mContext.getResources().getDimensionPixelSize(R.dimen.form_button_size);
                button.setLayoutParams(new GridView.LayoutParams(pixels, pixels));
            } else {
                button = (FloatingActionButton) convertView;
            }

            final int color = mColors[index];
            button.setImageResource(color == mSelectedColor ? R.drawable.ic_check : 0);
            UiUtils.colorTeamIconButton(mContext, color, button);
            button.setOnClickListener(fab -> onColorSelected(color));
            return button;
        }

        public abstract void onColorSelected(int selectedColor);
    }
}
