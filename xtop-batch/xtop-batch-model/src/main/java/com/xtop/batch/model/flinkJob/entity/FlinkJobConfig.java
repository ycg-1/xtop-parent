package com.xtop.batch.model.flinkJob.entity;



import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("flink_job_config")
public class FlinkJobConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 作业唯一标识 */
    private String jobName;

    /** 作业类型 */
    private String jobType;

    /** Kafka topic */
    private String topic;

    /** 消费组 */
    private String groupId;

    /** 为空则用全局默认 */
    private String bootstrapServers;

    /** 并行度 */
    private Integer parallelism;

    /** earliest / latest */
    private String autoOffsetReset;

    /** 消息体类型：STRING / JSON */
    private String valueType;

    /** JSON 时的目标类全限定名 */
    private String valueClass;

    /** 策略 Bean 名称 */
    private String handlerBean;

    /** 0=停止 1=运行中 2=异常 */
    private Integer status;

    /** Flink JobID */
    private String jobId;

    /** 最近一次 savepoint 路径 */
    private String savepointPath;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}