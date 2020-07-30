package com.infinite.downloaderapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.infinite.downloaderapp.chain.OkHttpClient;
import com.infinite.downloaderapp.chain.Response;
import com.infinite.downloaderapp.http.KLog;
import com.infinite.downloaderapp.json.CityEntity;
import com.infinite.downloaderapp.json.ProvinceEntity;

import java.util.ArrayList;

/**
 * Email: 690797861@qq.com
 * Author: Infinite
 * Date: 2020/4/10 - 12:19
 * Description: Class description
 */
public class HttpActivity extends AppCompatActivity {

    private Button btGet;
    private Button btPost;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_http);
        KLog.init(true, "[-HttpUtils-]");
        btGet = findViewById(R.id.btGet);
        btPost = findViewById(R.id.btPost);
        btGet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                Response response = new OkHttpClient().startRequest();
                KLog.d("request info is :\n" + response.getRequest().getName());
                KLog.d("response result is :\n" + response.getName());

//                KLog.d("futureTask start");
//                final FutureTask<String> futureTask = new FutureTask<>(new Callable<String>() {
//                    @Override
//                    public String call() throws Exception {
//                        KLog.d("get start");
//                        Thread.sleep(2000);
//                        KLog.d("get end");
//                        return "success";
//                    }
//                });
//
//
//                new Thread(futureTask).start();
//
//                btGet.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        futureTask.cancel(true);
//                        KLog.d("cancel future task");
//                    }
//                }, 1000);


            }
        });
        btPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProvinceEntity provinceEntity = new ProvinceEntity();
                final CityEntity cityEntity1 = new CityEntity();
                cityEntity1.setCityName("beijing");
                cityEntity1.setCityCode("010");
                cityEntity1.setCityId(1);

                final CityEntity cityEntity2 = new CityEntity();
                cityEntity2.setCityName("shanghai");
                cityEntity2.setCityId(2);
                provinceEntity.setCityList(new ArrayList<CityEntity>() {
                    {
                        add(cityEntity1);
                        add(new CityEntity());
                        add(new CityEntity());
                        add(cityEntity2);
                    }
                });
                String province = provinceEntity.toJson();
                KLog.d("province to json:" + province);

                ProvinceEntity provinceEntity1 = ProvinceEntity.toObject(province);

                KLog.d("json to province:" + provinceEntity1);
            }
        });
    }
}
