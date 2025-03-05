package com.sky.service;

import com.sky.dto.DataOverViewQueryDTO;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;

public interface ReportService {

    /**
     * 营业额统计接口
     * @param dataOverViewQueryDTO
     * @return
     */
    TurnoverReportVO turnoverStatistics(DataOverViewQueryDTO dataOverViewQueryDTO);

    /**
     * 用户统计接口
     * @param dataOverViewQueryDTO
     * @return
     */
    UserReportVO userStatistics(DataOverViewQueryDTO dataOverViewQueryDTO);

    /**
     * 订单统计接口
     * @param dataOverViewQueryDTO
     * @return
     */
    OrderReportVO orderStatistics(DataOverViewQueryDTO dataOverViewQueryDTO);

    /**
     * 查询销量排名top10接口
     * @param dataOverViewQueryDTO
     * @return
     */
    SalesTop10ReportVO getSalesTop10(DataOverViewQueryDTO dataOverViewQueryDTO);
}
