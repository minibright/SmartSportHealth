package com.bright.administrator.lib_coremodel.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.bright.administrator.lib_coremodel.bean.WeightData;

import java.util.List;

@Dao
public interface WeightDataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(WeightData... weightData);

    @Delete
    void delete(WeightData... weightData);

    @Query("DELETE FROM WeightData")
    void deleteAll();

    @Query("SELECT * FROM WeightData")
    List<WeightData> getAllWeightData();

    @Query("SELECT * FROM WeightData ORDER BY id DESC LIMIT :count")
    List<WeightData> getWeightData(int count);
}
