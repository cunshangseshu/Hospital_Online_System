package com.hospital.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * 分页响应VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {

    private Long total; // 总记录数

    private Integer pageNum; // 当前页码

    private Integer pageSize; // 每页大小

    private List<T> list; // 数据列表
}