package com.offline.quiz.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.offline.quiz.activity.MainActivity;
import com.offline.quiz.R;
import com.offline.quiz.activity.SettingActivity;
import com.offline.quiz.AppController;
import com.offline.quiz.helper.SettingsPreferences;
import com.offline.quiz.Constant;
import com.offline.quiz.model.Level;

import java.util.ArrayList;
import java.util.List;

public class FragmentLock extends Fragment {
    LevelListAdapter adapter;

    public View v;
    private static int levelNo = 1;
    public static Context mcontex;
    List<Level> levelList;
    RecyclerView recyclerView;
    ImageView back, setting;
    TextView tvLevel, emtyMsg;
    RecyclerView.LayoutManager layoutManager;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.lock_fragment, container, false);
        mcontex = getActivity().getBaseContext();

        back = (ImageView) v.findViewById(R.id.back);
        setting = (ImageView) v.findViewById(R.id.setting);
        tvLevel = (TextView) v.findViewById(R.id.tvLevel);
        tvLevel.setText("Select Level");
        emtyMsg = (TextView) v.findViewById(R.id.emtyMsg);
        recyclerView = (RecyclerView) v.findViewById(R.id.recyclerView);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        levelNo = MainActivity.DBHelper.GetLevelById(Constant.categoryId,Constant.subCategoryId);
//        levelNo = MainActivity.DBHelper.GetLevelByIdUsingSingleCat(Constant.categoryId);
//        Constant.subCategoryId

        getActivity().setTitle(getString(R.string.select_level));
        Constant.totalLevel = MainActivity.DBHelper.GetMaxLevel(Constant.categoryId,Constant.subCategoryId);
//        Constant.totalLevel = MainActivity.DBHelper.GetMaxLevelSingleCat(Constant.categoryId);
//        Constant.subCategoryId
        System.out.println("max level  "+ levelNo);
        if (Constant.totalLevel == 0) {
            emtyMsg.setVisibility(View.VISIBLE);
        }
        levelList = new ArrayList<>();
        for (int i = 0; i < Constant.totalLevel; i++) {
            Level card = new Level("" + i, "Level" + " : " + (i + 1), levelNo);
            levelList.add(card);
        }

        adapter = new LevelListAdapter(getActivity(), levelList);
        recyclerView.setAdapter(adapter);


        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().getSupportFragmentManager().popBackStack();
                if (getActivity().getSupportFragmentManager().getBackStackEntryCount() == 0) {
                    try {
                        AppController.StopSound();
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    }


                }
            }
        });

        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SettingsPreferences.getSoundEnableDisable(getActivity())) {
                    Constant.backSoundonclick(getActivity());
                }
                if (SettingsPreferences.getVibration(getActivity())) {
                    Constant.vibrate(getActivity(), Constant.VIBRATION_DURATION);
                }
                Intent playQuiz = new Intent(getActivity(), SettingActivity.class);
                startActivity(playQuiz);
                getActivity().overridePendingTransition(R.anim.open_next, R.anim.close_next);
            }
        });


        return v;
    }

    public class LevelListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        public int resourceLayout;
        public Activity activity;
        private List<Level> cardList;
        public FragmentPlay fragmentPlay = new FragmentPlay();


        public LevelListAdapter(Activity activity, List<Level> cardList) {
            this.cardList = cardList;
            this.activity = activity;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.lock_item, parent, false);
            return new LevelViewHolder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            LevelViewHolder viewHolder = (LevelViewHolder) holder;
            Level card = cardList.get(position);
            viewHolder.title.setText(card.getEnglishTitle());
            viewHolder.question_no.setText("que : " + 10);
            if (card.getlock() >= position + 1) {
                viewHolder.lock.setImageResource(R.drawable.unlock);
            } else {
                viewHolder.lock.setImageResource(R.drawable.lock);
            }

            viewHolder.cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (SettingsPreferences.getSoundEnableDisable(activity)) {
                        Constant.backSoundonclick(activity);
                    }
                    if (SettingsPreferences.getVibration(activity)) {
                        Constant.vibrate(activity, Constant.VIBRATION_DURATION);
                    }
                    Constant.RequestlevelNo = position + 1;
                    if (levelNo >= position + 1) {
                        FragmentTransaction ft = ((MainActivity) activity).getSupportFragmentManager().beginTransaction();
                        ft.replace(R.id.fragment_container, fragmentPlay, "fragment");
                        ft.addToBackStack("tag");
                        ft.commit();
                    } else {
                        Toast.makeText(activity, "Level is Locked", Toast.LENGTH_SHORT).show();
                    }

                }
            });
        }

        @Override
        public int getItemCount() {
            return cardList.size();
        }

        public class LevelViewHolder extends RecyclerView.ViewHolder {
            TextView title, question_no;
            ImageView lock;
            CardView cardView;

            public LevelViewHolder(View itemView) {
                super(itemView);
                title = (TextView) itemView.findViewById(R.id.level_no);
                question_no = (TextView) itemView.findViewById(R.id.question_no);
                lock = (ImageView) itemView.findViewById(R.id.lock);
                cardView = (CardView) itemView.findViewById(R.id.cardView);
            }
        }
    }


}
