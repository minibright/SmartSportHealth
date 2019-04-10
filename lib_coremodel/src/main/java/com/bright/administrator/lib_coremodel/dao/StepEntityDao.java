package com.bright.administrator.lib_coremodel.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;
import com.bright.administrator.lib_coremodel.bean.StepEntity;

@Dao
public interface StepEntityDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(StepEntity ...stepEntity);

    @Delete
    void delete(StepEntity ...stepEntity);

    @Update
    void update(StepEntity ...stepEntity);

    @Query("DELETE FROM StepEntity")
    void deleteAll();

    @Query("SELECT * FROM StepEntity where curDate = :curDate and userID = :userID")
    StepEntity findStepEntityByDate(String curDate, String userID);

}
