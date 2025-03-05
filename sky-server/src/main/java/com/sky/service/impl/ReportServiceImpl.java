package com.sky.service.impl;

import com.sky.constant.MessageConstant;
import com.sky.dto.DataOverViewQueryDTO;
import com.sky.entity.Orders;
import com.sky.exception.DateTimeException;
import com.sky.mapper.OrderMapper;
import com.sky.service.ReportService;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;

    /**
     * 营业额统计接口
     * @param dataOverViewQueryDTO
     * @return
     */
    @Override
    public TurnoverReportVO turnoverStatistics(DataOverViewQueryDTO dataOverViewQueryDTO) {
        // 先判断传来的日期是否合理
        if (dataOverViewQueryDTO.getBegin().isAfter(dataOverViewQueryDTO.getEnd())) {
            throw new DateTimeException(MessageConstant.PARAMETER_PASSED_ERROR);
        }

        // 将日期封装为集合
        List<LocalDate> dateList = new ArrayList<>();
        LocalDate begin = dataOverViewQueryDTO.getBegin();
        LocalDate end = dataOverViewQueryDTO.getEnd();
        dateList.add(begin);

        while (begin.isBefore(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        // 再查找营业额
        List<Double> turnoverList = new ArrayList<>();
        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            Map map = new HashMap();
            map.put("begin", beginTime);
            map.put("end", endTime);
            map.put("status", Orders.COMPLETED);
            Double turnover = orderMapper.sumMoneyByMap(map);
            if (turnover == null) turnover = 0.0;

            turnoverList.add(turnover);
        }

        return TurnoverReportVO
                .builder()
                .dateList(StringUtils.join(dateList, ","))
                .turnoverList(StringUtils.join(turnoverList, ","))
                .build();
    }

    /**
     * 用户统计接口
     * @param dataOverViewQueryDTO
     * @return
     */
    @Override
    public UserReportVO userStatistics(DataOverViewQueryDTO dataOverViewQueryDTO) {
        // 先判断传来的日期是否合理
        if (dataOverViewQueryDTO.getBegin().isAfter(dataOverViewQueryDTO.getEnd())) {
            throw new DateTimeException(MessageConstant.PARAMETER_PASSED_ERROR);
        }

        // 将日期封装为集合（添加前一天，以计算第一天新增人数）
        List<LocalDate> dateList = new ArrayList<>();
        LocalDate begin = dataOverViewQueryDTO.getBegin();
        LocalDate end = dataOverViewQueryDTO.getEnd();
        dateList.add(begin.minusDays(1));
        dateList.add(begin);

        while (begin.isBefore(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        // 再查找每天用户人数
        List<Integer> userList = new ArrayList<>();
        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            Map map = new HashMap();
            map.put("begin", beginTime);
            map.put("end", endTime);
            map.put("status", Orders.COMPLETED);
            Integer total = orderMapper.sumUserByMap(map);
            if (total == null) total = 0;

            userList.add(total);
        }

        // 计算新增用户人数（需要查询前一天的用户人数以填充第一天新增人数）
        List<Integer> newUserList = new ArrayList<>();
        for (int i = 0; i < userList.size() - 1; i++) {
            newUserList.add(userList.get(i + 1) - userList.get(i));
        }

        // 删除多余的日期和当天用户人数
        dateList.remove(0);
        userList.remove(0);

        return UserReportVO
                .builder()
                .dateList(StringUtils.join(dateList, ","))
                .totalUserList(StringUtils.join(userList, ","))
                .newUserList(StringUtils.join(newUserList, ","))
                .build();
    }
}
