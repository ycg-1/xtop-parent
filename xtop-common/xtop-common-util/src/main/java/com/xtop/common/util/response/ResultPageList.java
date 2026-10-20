package com.xtop.common.util.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.util.List;

/**
 * 分页查询结果(数据集合)DTO
 *
 * @author 杨春光
 * @since 1.0.0.0
 */
@Schema(description = "分页查询结果")
public class ResultPageList<T> implements Serializable {
    private static final long serialVersionUID = -3626233218579445992L;

    /**
     * 总数
     */
    @Schema(description  = "总数")
    private long total;
    /**
     * 当前页码
     */
    @Schema(description  = "当前页码")
    private long currPage;
    /**
     * 分页大小
     */
    @Schema(description  = "分页大小")
    private long pageSize;
    /**
     * 总共页数
     */
    @Schema(description  = "总共页数")
    private long totalPage;
    /**
     * 本页数据
     */
    @Schema(description  = "本页数据")
    private List<T> records;

    public ResultPageList() {

    }

    public ResultPageList(List<T> records) {
        this.records = records;
        if (records != null) {
            this.total = records.size();
            this.currPage = 1L;
            this.totalPage = 1L;
        }
    }

    public ResultPageList(List<T> records, long count) {
        this.records = records;
        this.total = count;
        this.currPage = 1L;
        this.totalPage = 1L;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getCurrPage() {
        return currPage;
    }

    public void setCurrPage(long currPage) {
        this.currPage = currPage;
    }

    public long getPageSize() {
        return pageSize;
    }

    public void setPageSize(long pageSize) {
        this.pageSize = pageSize;
    }

    public long getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(long totalPage) {
        this.totalPage = totalPage;
    }

    public List<T> getRecords() {
        return records;
    }

    public void setRecords(List<T> records) {
        this.records = records;
    }
}
