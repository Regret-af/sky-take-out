package com.sky.service.impl;

import com.sky.constant.MessageConstant;
import com.sky.dto.DataOverViewQueryDTO;
import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.exception.DateTimeException;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WorkspaceService workspaceService;

    /**
     * 营业额统计接口
     * @param dataOverViewQueryDTO
     * @return
     */
    @Override
    public TurnoverReportVO turnoverStatistics(DataOverViewQueryDTO dataOverViewQueryDTO) {
        // 先判断传来的日期是否合理，并将日期封装为集合
        List<LocalDate> dateList = date4List(dataOverViewQueryDTO.getBegin(), dataOverViewQueryDTO.getEnd());

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
        // 先判断传来的日期是否合理，并将日期封装为集合（添加前一天，以计算第一天新增人数）
        List<LocalDate> dateList = date4List(dataOverViewQueryDTO.getBegin(), dataOverViewQueryDTO.getEnd());

        // 再查找每天用户人数
        List<Integer> userList = new ArrayList<>();
        List<Integer> newUserList = new ArrayList<>();

        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            Integer user = getUserCount(null, endTime, Orders.COMPLETED);
            Integer newUser = getUserCount(beginTime, endTime, Orders.COMPLETED);
            if (user == null) user = 0;
            if (newUser == null) newUser = 0;

            userList.add(user);
            newUserList.add(newUser);
        }

        return UserReportVO
                .builder()
                .dateList(StringUtils.join(dateList, ","))
                .totalUserList(StringUtils.join(userList, ","))
                .newUserList(StringUtils.join(newUserList, ","))
                .build();
    }

    /**
     * 订单统计接口
     * @param dataOverViewQueryDTO
     * @return
     */
    @Override
    public OrderReportVO orderStatistics(DataOverViewQueryDTO dataOverViewQueryDTO) {
        // 先判断传来的日期是否合理，并将日期封装为集合
        List<LocalDate> dateList = date4List(dataOverViewQueryDTO.getBegin(), dataOverViewQueryDTO.getEnd());

        List<Integer> orderList = new ArrayList<>();
        List<Integer> validOrderList = new ArrayList<>();

        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            // 查找每天的总订单数
            Integer orderCount = getOrderCount(beginTime, endTime, null);

            // 查找每天的有效订单数
            Integer validOrderCount = getOrderCount(beginTime, endTime, Orders.COMPLETED);

            if (orderCount == null) orderCount = 0;
            if (validOrderCount == null) validOrderCount = 0;

            validOrderList.add(validOrderCount);
            orderList.add(orderCount);
        }

        // 计算订单总数，有效订单数和完成率
        Integer totalOrderCount = orderList.stream().reduce((a, b) -> a + b).get();
        Integer validOrderCount = validOrderList.stream().reduce((a, b) -> a + b).get();
        Double orderCompletionRate = 0.0;
        if (totalOrderCount != 0) {
            orderCompletionRate = validOrderCount.doubleValue() / totalOrderCount;
        }

        return OrderReportVO
                .builder()
                .dateList(StringUtils.join(dateList, ","))
                .orderCountList(StringUtils.join(orderList, ","))
                .validOrderCountList(StringUtils.join(validOrderList, ","))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }

    /**
     * 查询销量排名top10接口
     * @param dataOverViewQueryDTO
     * @return
     */
    @Override
    public SalesTop10ReportVO getSalesTop10(DataOverViewQueryDTO dataOverViewQueryDTO) {
        LocalDateTime beginTime = LocalDateTime.of(dataOverViewQueryDTO.getBegin(), LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(dataOverViewQueryDTO.getEnd(), LocalTime.MAX);
        List<GoodsSalesDTO> goodsSalesDTOList = orderMapper.getSalesTop10(beginTime, endTime);
        
        String nameList = StringUtils.
                join(goodsSalesDTOList.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList()), ",");
        String numberList = StringUtils.
                join(goodsSalesDTOList.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList()), ",");

        return SalesTop10ReportVO
                .builder()
                .nameList(nameList)
                .numberList(numberList)
                .build();
    }

    /**
     * 导出Excel报表接口
     * @param response
     */
    @Override
    public void export(HttpServletResponse response) {
        // 首先查询总体数据
        LocalDate begin = LocalDate.now().minusDays(30);
        LocalDate end = LocalDate.now().minusDays(1);

        BusinessDataVO businessDataVO = workspaceService
                .getBusinessData(LocalDateTime.of(begin, LocalTime.MIN), LocalDateTime.of(end, LocalTime.MAX));

        // 读取 Excel表格
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("templates/运营数据报表模板.xlsx");

        try {
            XSSFWorkbook excel = new XSSFWorkbook(in);

            // 获取工作表
            XSSFSheet sheet = excel.getSheetAt(0);
            // 填入时间
            sheet.getRow(1).getCell(1).setCellValue("时间" + begin + "~" + end);

            // 写入概览数据
            sheet.getRow(3).getCell(2).setCellValue(businessDataVO.getTurnover());
            sheet.getRow(3).getCell(4).setCellValue(businessDataVO.getOrderCompletionRate());
            sheet.getRow(3).getCell(6).setCellValue(businessDataVO.getNewUsers());
            sheet.getRow(4).getCell(2).setCellValue(businessDataVO.getValidOrderCount());
            sheet.getRow(4).getCell(4).setCellValue(businessDataVO.getUnitPrice());

            // 循环写入明细数据
            for (int i = 0; i < 30; i++) {
                LocalDate date = begin.plusDays(i);
                // 查询每天的概览数据
                BusinessDataVO businessData = workspaceService.getBusinessData(LocalDateTime.of(date, LocalTime.MIN),
                        LocalDateTime.of(date, LocalTime.MAX));
                XSSFRow row = sheet.getRow(i + 7);
                row.getCell(1).setCellValue(date.toString());
                row.getCell(2).setCellValue(businessData.getTurnover());
                row.getCell(3).setCellValue(businessData.getValidOrderCount());
                row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
                row.getCell(5).setCellValue(businessData.getUnitPrice());
                row.getCell(6).setCellValue(businessData.getNewUsers());
            }

            // 输出流下载文件
            ServletOutputStream outputStream = response.getOutputStream();
            excel.write(outputStream);

            // 关闭流，节约资源
            outputStream.flush();
            outputStream.close();
            excel.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 将日期封装为集合
     * @param begin
     * @param end
     * @return
     */
    private List<LocalDate> date4List(LocalDate begin, LocalDate end) {
        // 先判断传来的日期是否合理
        if (begin.isAfter(end)) {
            throw new DateTimeException(MessageConstant.PARAMETER_PASSED_ERROR);
        }

        // 将日期封装为集合（添加前一天，以计算第一天新增人数）
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);

        while (begin.isBefore(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        return dateList;
    }

    /**
     * 根据时间区间统计用户数量
     * @param begin
     * @param end
     * @param status
     * @return
     */
    private Integer getUserCount(LocalDateTime begin, LocalDateTime end, Integer status) {
        Map map = new HashMap();
        map.put("begin", begin);
        map.put("end", end);

        return userMapper.countByMap(map);
    }

    /**
     * 根据时间区间统计订单数量
     * @param begin
     * @param end
     * @param status
     * @return
     */
    private Integer getOrderCount(LocalDateTime begin, LocalDateTime end, Integer status) {
        Map map = new HashMap();
        map.put("begin", begin);
        map.put("end", end);
        map.put("status", status);

        return orderMapper.countByMap(map);
    }
}
