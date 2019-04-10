package com.bright.administrator.lib_coremodel.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.bright.administrator.lib_coremodel.bean.BleData;

import java.util.List;

@Dao
public interface BleDataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(BleData ...bleData);

    @Delete
    void delete(BleData ...bleData);

    @Query("DELETE FROM BleData")
    void deleteAll();

    @Query("SELECT * FROM BleData")
    List<BleData> getAllBleData();

    @Query("SELECT * FROM BleData ORDER BY id DESC LIMIT :count")
    List<BleData> getBleData(int count);
}
