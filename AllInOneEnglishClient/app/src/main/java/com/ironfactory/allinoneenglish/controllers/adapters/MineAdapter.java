package com.ironfactory.allinoneenglish.controllers.adapters;

import android.content.Context;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ironfactory.allinoneenglish.Global;
import com.ironfactory.allinoneenglish.R;
import com.ironfactory.allinoneenglish.controllers.fragments.MineFragment;
import com.ironfactory.allinoneenglish.entities.CourseEntity;
import com.ironfactory.allinoneenglish.utils.VLCUtils;
import com.ironfactory.allinoneenglish.utils.VideoUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by IronFactory on 2016. 3. 22..
 */
public class MineAdapter extends RecyclerView.Adapter<MineAdapter.StudyViewHolder> {

    private static final String TAG = "MineAdapter";

    private Context context;
    private List<CourseEntity> bookmarkList;
    private MineFragment.OnPlayVideo onPlayVideo;

    public MineAdapter(Context context, List<CourseEntity> bookmarkList, MineFragment.OnPlayVideo onPlayVideo) {
        this.context = context;
        this.bookmarkList = bookmarkList;
        this.onPlayVideo = onPlayVideo;
    }

    @Override
    public StudyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_study_class, parent, false);
        return new StudyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final StudyViewHolder holder, final int position) {
        if (position % 2 == 1) {
            holder.container.setBackgroundColor(context.getResources().getColor(R.color.background_mine_soft));
            holder.numText.setTextColor(context.getResources().getColor(R.color.background_mine_soft));
        } else {
            holder.container.setBackgroundColor(context.getResources().getColor(R.color.background_mine_hard));
            holder.numText.setTextColor(context.getResources().getColor(R.color.background_mine_hard));
        }

        StringBuilder sb = new StringBuilder();
        sb.append(position);
        if (position < 10)
            sb.insert(0, 0);
        holder.numText.setText(sb.toString());
        holder.titleText.setText(Global.files.get(bookmarkList.get(position).getIndex()).getName());
        SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd");
        holder.lastStudyDayText.setText("최종학습일 " + format.format(Global.courses.get(position).getLastStudyDate()).toString());
        holder.playText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPlayVideo.onPlay();
                final Handler handler = new Handler();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final VideoUtils videoUtils = new VideoUtils();
                        videoUtils.setPosition(position);
                        videoUtils.decryptVideo();
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                onPlayVideo.onStopPlay();
                                VLCUtils vlcUtils = new VLCUtils(context);
                                vlcUtils.playVideo(videoUtils.getVideoDecryptPath());
                                Global.courses.get(position).setLastStudyDate(new Date(System.currentTimeMillis()));
                                Global.dbManager.updateCourse(Global.courses.get(position));
                                File file = new File(videoUtils.getVideoDecryptPath());
                                if (file.delete()) {
                                    Log.d(TAG, "삭제 성공");
                                } else {
                                    Log.d(TAG, "삭제 실패");
                                }
                            }
                        });
                    }
                }).start();
            }
        });

        if (Global.courses.get(position).isBookmark()) {
            holder.bookmarkBtn.setBackgroundResource(R.drawable.ic_bookmark);
        } else {
            holder.bookmarkBtn.setBackgroundResource(R.drawable.ic_bookmark_blank);
        }

        holder.bookmarkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // DB 수정 해야함
                if (Global.courses.get(position).isBookmark()) {
                    holder.bookmarkBtn.setBackgroundResource(R.drawable.ic_bookmark_blank);
                    Global.courses.get(position).setBookmark(false);
                } else {
                    holder.bookmarkBtn.setBackgroundResource(R.drawable.ic_bookmark);
                    Global.courses.get(position).setBookmark(true);
                }
                Global.dbManager.updateCourse(Global.courses.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return bookmarkList.size();
    }

    public class StudyViewHolder extends RecyclerView.ViewHolder {

        final LinearLayout container;
        final TextView numText;
        final TextView titleText;
        final TextView lastStudyDayText;
        final TextView playText;
        final ImageButton bookmarkBtn;

        public StudyViewHolder(View itemView) {
            super(itemView);

            container = (LinearLayout) itemView.findViewById(R.id.item_study_class_container);
            numText = (TextView) itemView.findViewById(R.id.item_study_class_num);
            titleText = (TextView) itemView.findViewById(R.id.item_study_class_title);
            lastStudyDayText = (TextView) itemView.findViewById(R.id.item_study_class_last_study_day);
            playText = (TextView) itemView.findViewById(R.id.item_study_class_play);
            bookmarkBtn = (ImageButton) itemView.findViewById(R.id.item_study_class_bookmark);
        }
    }
}
