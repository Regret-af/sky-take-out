package com.sky.service;

import com.sky.dto.DataOverViewQueryDTO;
import com.sky.vo.TurnoverReportVO;

public interface ReportService {

    /**
     * 营业额统计接口
     * @param dataOverViewQueryDTO
     * @return
     */
    TurnoverReportVO turnoverStatistics(DataOverViewQueryDTO dataOverViewQueryDTO);
}
