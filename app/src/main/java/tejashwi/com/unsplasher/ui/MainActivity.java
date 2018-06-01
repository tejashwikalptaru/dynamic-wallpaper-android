/*
 * Copyright (c) 2018, Tejashwi Kalp Taru
 */

package tejashwi.com.unsplasher.ui;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import tejashwi.com.unsplasher.R;
import tejashwi.com.unsplasher.adapter.CustomListAdapter;
import tejashwi.com.unsplasher.rest.APIUtils;
import tejashwi.com.unsplasher.rest.Services;
import tejashwi.com.unsplasher.rest.model.RandomImagesObject;
import tejashwi.com.unsplasher.rest.model.SearchResult;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MAIN-ERROR";
    private Services mServiceAPI = null;
    private int mPageCounter = 0;
    private int mSearchCounter = 0;
    private ListView mListView;
    private CustomListAdapter mAdapter;
    private boolean mSearchOpen = false;
    private boolean mBackedUp = false;
    private String mQuery = "";
    private List<RandomImagesObject> mBackup = null;
    private Button mLoadMore = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mAdapter = new CustomListAdapter(this);
        mListView = findViewById(R.id.imageList);
        mLoadMore = new Button(this);
        mLoadMore.setText("Load More");
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                RandomImagesObject item = (RandomImagesObject) mListView.getItemAtPosition(position);
                Intent preview = new Intent(MainActivity.this, DetailsScreen.class);
                preview.putExtra("url", item.getUrls().getRegular());
                preview.putExtra("download", item.getUrls().getFull());
                preview.putExtra("author", item.getUser().getName());
                preview.putExtra("id", item.getId());
                startActivity(preview);

            }
        });

        mLoadMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if(mSearchOpen){
                    getSearchedImages(mQuery);
                }else{
                    getRandomImages();
                }
            }
        });

        getRandomImages();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                if(mBackup != null){
                    mAdapter.restorePrevious(mBackup);
                    mBackup = null;
                    mSearchCounter = 0;
                    mBackedUp = false;
                }
                mSearchOpen = false;
                return false;
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mQuery = query;
                getSearchedImages(query);
                return true;
            }
            @Override
            public boolean onQueryTextChange(String query) {
                mSearchOpen = true;
                return true;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_about) {
            final Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.about_dialog);
            TextView tv = dialog.findViewById(R.id.aboutMessage);
            tv.setText("Unsplasher v0.1\n\nTejashwi Kalp Taru\nhttps://github.com/tejashwikalptaru\n\n");
            Button dismiss = dialog.findViewById(R.id.dismissAbout);
            dismiss.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            dialog.show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void getRandomImages() {
        if(mServiceAPI == null){
            mServiceAPI = APIUtils.getClient();
        }

        final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("Loading images\nPlease wait...");
        pDialog.setCancelable(false);
        pDialog.show();

        mPageCounter++;
        mServiceAPI.getRandom(mPageCounter).enqueue(new Callback<List<RandomImagesObject>>() {
            @Override
            public void onResponse(Call<List<RandomImagesObject>> call, Response<List<RandomImagesObject>> response) {
                if(response.isSuccessful()){
                    List<RandomImagesObject> list = response.body();
                    mAdapter.updateDataSet(list);
                    pDialog.cancel();
                    mListView.addFooterView(mLoadMore);
                } else {
                    Log.e(TAG, "failed");
                    pDialog.cancel();
                }
            }

            @Override
            public void onFailure(Call<List<RandomImagesObject>> call, Throwable t) {
                Log.e(TAG, "onFailure: " + t.getMessage() + "\nCause: " + t.getCause());
                pDialog.cancel();
            }
        });
    }

    private void getSearchedImages(String query) {
        if(mServiceAPI == null){
            mServiceAPI = APIUtils.getClient();
        }
        mSearchCounter++;
        mServiceAPI.searchImage(query, mSearchCounter).enqueue(new Callback<SearchResult>() {
            @Override
            public void onResponse(Call<SearchResult> call, Response<SearchResult> response) {
                if(response.isSuccessful()){
                    List<RandomImagesObject> list =  response.body().getResults();
                    if(mBackedUp){
                        mAdapter.updateDataSet(list);
                    } else {
                        mBackup = mAdapter.backUpAndShowThis(list);
                        mBackedUp = true;
                    }
                } else {
                    Log.e(TAG, "failed");
                }
            }

            @Override
            public void onFailure(Call<SearchResult> call, Throwable t) {
                Log.e(TAG, "onFailure: " + t.getMessage() + "\nCause: " + t.getCause());
            }
        });
    }
}
