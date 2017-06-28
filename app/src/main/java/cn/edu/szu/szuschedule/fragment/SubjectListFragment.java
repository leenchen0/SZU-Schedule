package cn.edu.szu.szuschedule.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import cn.edu.szu.szuschedule.HomeworkListActivity;
import cn.edu.szu.szuschedule.R;
import cn.edu.szu.szuschedule.adapter.SubjectAdapter;
import cn.edu.szu.szuschedule.object.SubjectItem;
import cn.edu.szu.szuschedule.service.BBService;
import cn.edu.szu.szuschedule.util.LoadingUtil;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenlin on 07/06/2017.
 */
public class SubjectListFragment extends Fragment implements SubjectAdapter.OnClickListener {
    private final static String currentTermNum = "20162";

    View view;
    RecyclerView subjectList;
    LoadingUtil loadingUtil;
    SwipeRefreshLayout course_Refresh;
    SubjectAdapter subadpter;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_subject_list, null);
        subjectList = (RecyclerView) view.findViewById(R.id.subject_recycle);
        LinearLayoutManager sub_list_layoutManager = new LinearLayoutManager(getContext());
        subjectList.setLayoutManager(sub_list_layoutManager);

        course_Refresh = (SwipeRefreshLayout) view.findViewById(R.id.course_Refresh);

        course_Refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Toast.makeText(getActivity(),"正在刷新",Toast.LENGTH_SHORT).show();
                getCourses(1);
                subadpter.notifyDataSetChanged();
                course_Refresh.setRefreshing(false);

            }
        });
        course_Refresh.setColorSchemeColors(getResources().getColor(android.R.color.holo_blue_bright),
                getResources().getColor(android.R.color.holo_orange_light),
                getResources().getColor(android.R.color.holo_green_light));
        loadingUtil = new LoadingUtil(getActivity());
        getCourses(0);

        return view;
    }

    @Override
    public void onClick(int position, View view, SubjectItem subjectItem) {
        HomeworkListActivity.subjectItem = subjectItem;
        startActivity(new Intent(getContext(), HomeworkListActivity.class));
    }

    public  void getCourses(final int i) {
        loadingUtil.showLoading();
        Observable<ArrayList<SubjectItem>> observable;
        if (i == 0) {
            observable = BBService.getAllCourses(getActivity());
        } else {
            observable = BBService.updateAllCourses(getActivity());
        }

        observable.observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<ArrayList<SubjectItem>>() {
                    @Override
                    public void accept(@NonNull ArrayList<SubjectItem> subjectItems) throws Exception {
                        List<SubjectItem> currentTerm = BBService.getCoursesByTerm(subjectItems, currentTermNum);
                        subadpter = new SubjectAdapter(currentTerm);
                        subadpter.setOnClickListener(SubjectListFragment.this);
                        subjectList.setAdapter(subadpter);
                        loadingUtil.hideLoading();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable throwable) throws Exception {
                        loadingUtil.hideLoading();
                        Toast.makeText(getActivity(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
