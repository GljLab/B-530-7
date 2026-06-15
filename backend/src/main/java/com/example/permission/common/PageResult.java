package com.example.permission.common;

import lombok.Data;
import java.util.List;

/**
 * 分页结果封装
 */
@Data
public class PageResult<T> {
    
    /**
     * 总记录数
     */
    private Long total;
    
    /**
     * 数据列表
     */
    private List<T> list;
    
    /**
     * 当前页码
     */
    private Long pageNum;
    
    /**
     * 每页大小
     */
    private Long pageSize;
    
    public PageResult() {
    }
    
    public PageResult(Long total, List<T> list) {
        this.total = total;
        this.list = list;
    }
    
    public PageResult(Long total, List<T> list, Long pageNum, Long pageSize) {
        this.total = total;
        this.list = list;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
    }
}
