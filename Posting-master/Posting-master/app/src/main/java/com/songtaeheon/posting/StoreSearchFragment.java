package com.songtaeheon.posting;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.songtaeheon.posting.DataModel.NaverStoreInfo;
import com.songtaeheon.posting.DataModel.StoreInfo;
import com.songtaeheon.posting.Utils.RecyclerItemClickListener;
import com.songtaeheon.posting.Utils.RecyclerviewAdapterForShare;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.songtaeheon.posting.SearchService.API_URL;


public class StoreSearchFragment extends Fragment {
    private final String TAG = "TAGStoreResearchFrag";
    private final String naverApiId = "B6m_wV3mDimRZaxT8fZe";
    private final String naverApiSecret = "rb2Or79uka";

    Retrofit retrofit;
    SearchService service;

    EditText searchWordText;
    String searchWord;

    RecyclerView mRecyclerView;
    ArrayList<NaverStoreInfo> storeInfoArrayList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.d(TAG, "OnCreateView : started");
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_store_search, container, false);
        mRecyclerView = view.findViewById(R.id.recyclerview);




        //back 버튼
        ImageView backButton = view.findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onclick : closing the restaurantn research fragment, back to gallery fragment");
                //back button 기능
                FragmentManager fm = getActivity().getSupportFragmentManager();
                fm.popBackStackImmediate();

            }
        });

        //next 버튼
        TextView nextScreen = view.findViewById(R.id.tvNext);
        nextScreen.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onclick : navigating to the final share screen : 필요없어질 수도!");
                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.relLayout1, new LastShareFragment());
                ft.addToBackStack(null);
                ft.commit();
            }
        });

        //search 버튼(EditText에 있는 단어를 받아서 검색)
        searchWordText = view.findViewById(R.id.searchWord);
        Button searchButton = view.findViewById(R.id.searchButton);
        searchButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                searchWord = searchWordText.getText().toString();
                Log.d(TAG, "search button clicked. searchWord : "+ searchWord);
                requestSearchApi(searchWord);


            }
        });

        //recyclerview setup
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);


        //recyclerView 아이템 터치 리스터. recycler view 중 가게를 하나 선택하면 다음 프래그먼트로 이동
        mRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getActivity(), mRecyclerView ,new RecyclerItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {

                        //선택한 아이템뷰 확인 및 데이터 전달
                        int itemPosition = mRecyclerView.getChildLayoutPosition(view);
                        Log.d(TAG, "item clicked : " + storeInfoArrayList.get(itemPosition).title);
                        Log.d(TAG, "move to Last Share Fragment");

                        //선택시 색깔 변하도록
                        view.setBackgroundColor(0x000000);

                        //선택한 가게 정보 데이터를 bundle에 넣고 다음 프래그먼트로 이동
                        setFragmentAndMove(itemPosition)              ;

                    }

                    @Override public void onLongItemClick(View view, int position) {
                        // do whatever
                    }
                })
        );


        return view;
    }

    //선택한 가게 정보 데이터를 bundle에 넣고 다음 프래그먼트로 이동
    private void setFragmentAndMove(int itemPosition) {
        Log.d(TAG, "put data in Fragment");
        LastShareFragment fragment = new LastShareFragment();
        Bundle args = new Bundle();
        args.putParcelable("StoreData", storeInfoArrayList.get(itemPosition));
        fragment.setArguments(args);

        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.relLayout1, fragment);
        ft.addToBackStack(null);
        ft.commit();
    }


    //네이버 api 검색 실행. 성공하면 정보 받아서 리사이클러뷰 어댑터로 넘긴다.
    private void requestSearchApi(String searchWord){

        retrofit = new Retrofit.Builder()
                .baseUrl(API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        service = retrofit.create(SearchService.class);
        Call<JsonObject> request = service.getUserRepositories(naverApiId, naverApiSecret, searchWord);
        request.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                Log.d(TAG, "request enqueue is Successed  ");
                storeInfoArrayList  = parseJsonToStoreInfo(response.body());
                setRecyclerviewAdapter(storeInfoArrayList);
            }
            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.d(TAG, "request enqueue is failed : w : " + t.toString());
                t.printStackTrace();
            }
        });


    }

    //naver api에서 받아온 jsonObject를 파싱해서 ArrayList<>로 변경
    private ArrayList<NaverStoreInfo> parseJsonToStoreInfo(JsonObject jsonObject) {
        ArrayList<NaverStoreInfo> dataList = new ArrayList<>();
        Gson gson = new Gson();

        JsonArray jsonArray = (JsonArray) jsonObject.get("items");
        for(int i=0 ; i<jsonArray.size(); i++){
            NaverStoreInfo object = gson.fromJson(jsonArray.get(i), NaverStoreInfo.class);
            dataList.add(object);
        }
        return dataList;

    }

    //recycler view를 네이버 api에서 가져온 리스트와 함께 어댑터 세팅
    private void setRecyclerviewAdapter(ArrayList<NaverStoreInfo> storeInfoArrayList) {
        RecyclerviewAdapterForShare myAdapter = new RecyclerviewAdapterForShare(getActivity(), storeInfoArrayList);
        mRecyclerView.setAdapter(myAdapter);
    }
}