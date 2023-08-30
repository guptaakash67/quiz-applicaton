package com.offline.quiz.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.offline.quiz.Constant;
import com.offline.quiz.R;
import com.offline.quiz.helper.SettingsPreferences;
import com.offline.quiz.model.Bookmark;

import java.util.ArrayList;

public class BookmarkList extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    ImageView back, setting;
    TextView title, tvNoBookmarked;
    ArrayList<Bookmark> bookmarks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bookmark_list);

        back = (ImageView) findViewById(R.id.back);
        setting = (ImageView) findViewById(R.id.setting);
        title = (TextView) findViewById(R.id.tvLevel);
        tvNoBookmarked = (TextView) findViewById(R.id.emptyMsg);
        title.setText("BookmarkList");
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        bookmarks = MainActivity.bookmarkDBHelper.getAllBookmarkedList();
      System.out.println("check solutions ----/--"+bookmarks.toString());
        if (bookmarks.size() == 0) {
            tvNoBookmarked.setVisibility(View.VISIBLE);
        }
        BookMarkAdapter adapter = new BookMarkAdapter(getApplicationContext(), bookmarks);
        recyclerView.setAdapter(adapter);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SettingsPreferences.getSoundEnableDisable(getApplicationContext())) {
                    Constant.backSoundonclick(getApplicationContext());
                }
                if (SettingsPreferences.getVibration(getApplicationContext())) {
                    Constant.vibrate(getApplicationContext(), Constant.VIBRATION_DURATION);
                }
                Intent playQuiz = new Intent(getApplicationContext(), SettingActivity.class);
                startActivity(playQuiz);
                overridePendingTransition(R.anim.open_next, R.anim.close_next);
            }
        });


    }

    public class BookMarkAdapter extends RecyclerView.Adapter<BookMarkAdapter.ItemRowHolder> {
        private ArrayList<Bookmark> bookmarks;
        private Context mContext;

        public BookMarkAdapter(Context context, ArrayList<Bookmark> bookmarks) {
            this.bookmarks = bookmarks;
            this.mContext = context;
        }

        @Override
        public ItemRowHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.bookmark_layout, parent, false);
            return new ItemRowHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ItemRowHolder holder, final int position) {

            ItemRowHolder itemRowHolder = (ItemRowHolder) holder;
            final Bookmark bookmark = bookmarks.get(position);
            itemRowHolder.tvNo.setText("" + (position + 1) + ".");
            itemRowHolder.tvQue.setText(bookmark.getQuestion());
            itemRowHolder.tvAns.setText(bookmark.getAnswer());
            itemRowHolder.remove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MainActivity.bookmarkDBHelper.delete_id(bookmark.getQue_id());
                    bookmarks.remove(position);
                    notifyDataSetChanged();
                }
            });
            itemRowHolder.cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                   SolutionDialog(bookmark.getQuestion(),bookmark.getSolution());
                }
            });
        }

        @Override
        public int getItemCount() {
            return (null != bookmarks ? bookmarks.size() : 0);
        }

        public class ItemRowHolder extends RecyclerView.ViewHolder {
            TextView tvNo, tvQue, tvAns;
            ImageView remove;
            CardView cardView;

            public ItemRowHolder(View itemView) {
                super(itemView);
                tvNo = (TextView) itemView.findViewById(R.id.tvNo);
                tvQue = (TextView) itemView.findViewById(R.id.tvQue);
                tvAns = (TextView) itemView.findViewById(R.id.tvAns);
                remove = (ImageView) itemView.findViewById(R.id.remove);
                cardView = (CardView) itemView.findViewById(R.id.cardView);


            }
        }
    }

    public void SolutionDialog(String question, String solution) {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(BookmarkList.this);

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View dialogView = inflater.inflate(R.layout.note_dialog_layout, null);
        dialog.setView(dialogView);
        TextView tvQuestion = (TextView) dialogView.findViewById(R.id.question);
        TextView tvSolution = (TextView) dialogView.findViewById(R.id.solution);
        System.out.println("check solutions ----/--" + question + "-----" + solution);
        tvQuestion.setText(question);
        tvSolution.setText(solution);
        AlertDialog alertDialog = dialog.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        alertDialog.setCancelable(true);
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
