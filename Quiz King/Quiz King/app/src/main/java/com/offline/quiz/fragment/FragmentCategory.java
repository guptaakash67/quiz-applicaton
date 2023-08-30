package com.offline.quiz.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.offline.quiz.AppController;
import com.offline.quiz.Constant;
import com.offline.quiz.R;
import com.offline.quiz.activity.MainActivity;
import com.offline.quiz.activity.SettingActivity;
import com.offline.quiz.helper.CircleImageView;
import com.offline.quiz.helper.SettingsPreferences;
import com.offline.quiz.model.Category;
import com.offline.quiz.model.SubCategory;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;

public class FragmentCategory extends Fragment {
    private RecyclerView recyclerView;
    private View view;

    ArrayList<Category> mListItem;
    AdView mAdView;
    TextView empty_msg, tvTitle;
    CoordinatorLayout layout;
    ImageView back, setting;
    public static ArrayList<Category> categoryList;
    public static ArrayList<SubCategory> subCatList;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.category, container, false);


        mAdView = (AdView) view.findViewById(R.id.banner_AdView);
        mAdView.loadAd(new AdRequest.Builder().build());
        layout = (CoordinatorLayout) view.findViewById(R.id.layout);
        empty_msg = (TextView) view.findViewById(R.id.txtblanklist);
        recyclerView = (RecyclerView) view.findViewById(R.id.category_recycleview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        back = (ImageView) view.findViewById(R.id.back);
        setting = (ImageView) view.findViewById(R.id.setting);
        tvTitle = (TextView) view.findViewById(R.id.tvLevel);
        tvTitle.setText(getString(R.string.select_category));
        mListItem = new ArrayList<>();
        mListItem = MainActivity.DBHelper.getAllCategories();
        CategoryAdapter categoryAdapter = new CategoryAdapter(getActivity(), mListItem);
        recyclerView.setAdapter(categoryAdapter);
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

        return view;
    }


    @Override
    public void onResume() {
        super.onResume();

    }

    public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ItemRowHolder> {
        private ArrayList<Category> dataList;
        private Context mContext;
        public FragmentLock fragmentlock = new FragmentLock();
        public CategoryAdapter(Context context, ArrayList<Category> dataList) {
            this.dataList = dataList;
            this.mContext = context;
        }

        @Override
        public CategoryAdapter.ItemRowHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_category, parent, false);
            return new ItemRowHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull CategoryAdapter.ItemRowHolder holder, final int position) {
            empty_msg.setVisibility(View.GONE);
            final Category singleItem = dataList.get(position);
            holder.text.setText(singleItem.getName());


            holder.relativeLayout
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Constant.categoryId = singleItem.getId();
                            FragmentSubcategory fragmentsubcategory = new FragmentSubcategory();
                            FragmentTransaction ft = ((MainActivity) mContext).getSupportFragmentManager().beginTransaction();
                            Constant.FragmentTitle = getString(R.string.select_subcategory);

//                            ft.hide(getFragmentManager().findFragmentByTag("categoryfragment"));
                            ft.add(R.id.fragment_container, fragmentsubcategory, "subcategoryfragment");
//                            ft.add(R.id.fragment_container, fragmentlock, "fragmentlock");
                            ft.addToBackStack(null);
                            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                            ft.commit();

                        }
                    });
        }

        @Override
        public int getItemCount() {
            return (null != dataList ? dataList.size() : 0);
        }

        public class ItemRowHolder extends RecyclerView.ViewHolder {
            public CircleImageView image;
            public TextView text;
            RelativeLayout relativeLayout;

            public ItemRowHolder(View itemView) {
                super(itemView);
                image = (CircleImageView) itemView.findViewById(R.id.imgcategory);
                text = (TextView) itemView.findViewById(R.id.item_title);
                relativeLayout = (RelativeLayout) itemView.findViewById(R.id.cat_layout);
            }
        }
    }
}
