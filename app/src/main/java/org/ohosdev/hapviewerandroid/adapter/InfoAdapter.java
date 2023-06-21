package org.ohosdev.hapviewerandroid.adapter;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import org.ohosdev.hapviewerandroid.R;
import org.ohosdev.hapviewerandroid.model.HapInfo;

public class InfoAdapter extends RecyclerView.Adapter<InfoAdapter.ViewHolder> {

    private static final int MAX_ITEMS = 6;
    private final LayoutInflater layoutInflater;
    private final String unknownString;
    @NonNull
    private HapInfo info = new HapInfo();

    public InfoAdapter(Context context) {
        super();
        layoutInflater = LayoutInflater.from(context);
        unknownString = context.getString(android.R.string.unknownName);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(layoutInflater.inflate(R.layout.info_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        switch (position) {
            case 0:
                holder.setName(R.string.info_appName);
                holder.setContent(info.init ? unknownString : info.appName);
                break;
            case 1:
                holder.setName(R.string.info_appPackageName);
                holder.setContent(info.init ? unknownString : info.packageName);
                break;
            case 2:
                holder.setName(R.string.info_versionName);
                holder.setContent(info.init ? unknownString : info.versionName);
                break;
            case 3:
                holder.setName(R.string.info_versionCode);
                holder.setContent(info.init ? unknownString : info.versionCode);
                break;
            case 4:
                holder.setName(R.string.info_targetAPI);
                holder.setContent(info.init ? unknownString : String.format("API %s (%s)", info.targetAPIVersion, info.apiReleaseType));
                break;
            case 5:
                holder.setName(R.string.info_tech);
                holder.setContent(info.init ? unknownString : info.getTechDesc(holder.context));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return MAX_ITEMS;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setInfo(@NonNull HapInfo info) {
        this.info = info;
        notifyDataSetChanged();
    }


    static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final TextView textView;
        private final Context context;
        @NonNull
        private String name = "";
        @NonNull
        private String content = "";

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textView);
            itemView.setOnClickListener(this);
            context = itemView.getContext();
        }

        private void refresh() {
            textView.setText(String.format("%s: %s", name, content));
        }

        public void setName(@Nullable String name) {
            if (name == null || name.equals(this.name))
                return;
            this.name = name;
            refresh();
        }

        public void setName(int resId) {
            setName(context.getString(resId));
        }

        public void setContent(@Nullable String content) {
            if (content == null || content.equals(this.content))
                return;
            this.content = content;
            refresh();
        }

        public void copyText() {
            if (content.isEmpty())
                return;

            ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            // 设置内容到剪切板
            cm.setPrimaryClip(ClipData.newPlainText(null, this.content));
            // Toast.makeText(itemView.getContext(), "已复制 " + name, Toast.LENGTH_SHORT).show();
            String toastText = String.format(context.getString(R.string.copied_withName), name);
            Snackbar.make(itemView, toastText, Snackbar.LENGTH_SHORT).show();
        }

        @Override
        public void onClick(View v) {
            copyText();
        }
    }
}
