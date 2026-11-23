package com.xtop.batch.dao.mapper.flinkJob;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.xtop.batch.model.flinkJob.entity.FlinkJobConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface FlinkJobConfigMapper extends BaseMapper<FlinkJobConfig> {

    @Select("SELECT * FROM flink_job_config WHERE status = 1")
    List<FlinkJobConfig> listRunning();

    @Select("SELECT * FROM flink_job_config WHERE job_name = #{jobName}")
    FlinkJobConfig getByName(@Param("jobName") String jobName);

    @Update("UPDATE flink_job_config SET status = #{status}, job_id = #{jobId}, " +
            "savepoint_path = #{savepointPath}, update_time = NOW() WHERE id = #{id}")
    int updateStatus(@Param("id") Long id,
                     @Param("status") Integer status,
                     @Param("jobId") String jobId,
                     @Param("savepointPath") String savepointPath);
}