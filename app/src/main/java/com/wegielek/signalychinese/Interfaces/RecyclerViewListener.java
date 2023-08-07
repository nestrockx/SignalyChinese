package com.wegielek.signalychinese.Interfaces;

import android.view.View;

public interface RecyclerViewListener {
    void onItemReleased(int position);
    void onItemPressed(View itemView);
    void onItemCanceled(View itemView);
}
