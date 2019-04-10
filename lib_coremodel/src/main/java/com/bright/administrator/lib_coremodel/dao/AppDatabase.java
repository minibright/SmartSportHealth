package com.bright.administrator.lib_coremodel.dao;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.support.annotation.NonNull;

import android.arch.persistence.db.SupportSQLiteDatabase;
import com.bright.administrator.lib_coremodel.bean.BleData;
import com.bright.administrator.lib_coremodel.bean.StepEntity;
import com.bright.administrator.lib_coremodel.bean.WeightData;
import com.bright.administrator.lib_coremodel.constant.Constant;


@Database(entities = {StepEntity.class,BleData.class, WeightData.class}, version = 1,exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static final Object mLock = new Object();

    private static AppDatabase sInstance;

    public static AppDatabase getInstance(Context context) {
        synchronized (mLock){
            if(sInstance == null){
                sInstance = Room.databaseBuilder(context.getApplicationContext(),AppDatabase.class, Constant.DATABASE_NAME)
                            .addCallback(new RoomDatabase.Callback() {
                                    //第一次创建数据库时调用，但是在创建所有表之后调用的
                                    @Override
                                    public void onCreate(@NonNull SupportSQLiteDatabase db) {
                                        super.onCreate(db);
                                    }

                                    //当数据库被打开时调用
                                    @Override
                                    public void onOpen(@NonNull SupportSQLiteDatabase db) {
                                        super.onOpen(db);
                                    }
                                })
                            .allowMainThreadQueries()//允许在主线程查询数据
                            .addMigrations()//迁移数据库使用，下面会单独拿出来讲
                            .fallbackToDestructiveMigration()//迁移数据库如果发生错误，将会重新创建数据库，而不是发生崩溃
                            .build();
            }
            return sInstance;
        }
    }

    public abstract StepEntityDao stepEntityDao();
    public abstract BleDataDao bleDataDao();
    public abstract WeightDataDao weightDataDao();

}
