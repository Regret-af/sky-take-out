package com.sky.controller.admin;

import com.sky.dto.DataOverViewQueryDTO;
import com.sky.result.Result;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/report")
@Slf4j
@Api(tags = "数据统计相关接口")
public class ReportController {

    @Autowired
    private ReportService reportService;

    /**
     * 营业额统计接口
     * @param dataOverViewQueryDTO
     * @return
     */
    @GetMapping("/turnoverStatistics")
    @ApiOperation("营业额统计接口")
    public Result<TurnoverReportVO> turnoverStatistics(DataOverViewQueryDTO dataOverViewQueryDTO) {
        log.info("开始进行营业额统计:{}", dataOverViewQueryDTO);
        TurnoverReportVO turnoverReportVO = reportService.turnoverStatistics(dataOverViewQueryDTO);

        return Result.success(turnoverReportVO);
    }

    /**
     * 用户统计接口
     * @param dataOverViewQueryDTO
     * @return
     */
    @GetMapping("/userStatistics")
    @ApiOperation("用户统计接口")
    public Result<UserReportVO> userStatistics(DataOverViewQueryDTO dataOverViewQueryDTO) {
        log.info("开始进行用户数量统计:{}", dataOverViewQueryDTO);
        UserReportVO userReportVO = reportService.userStatistics(dataOverViewQueryDTO);

        return Result.success(userReportVO);
    }

    /**
     * 订单统计接口
     * @param dataOverViewQueryDTO
     * @return
     */
    @GetMapping("/ordersStatistics")
    @ApiOperation("订单统计接口")
    public Result<OrderReportVO> ordersStatistics(DataOverViewQueryDTO dataOverViewQueryDTO) {
        log.info("开始进行订单统计:{}", dataOverViewQueryDTO);
        OrderReportVO orderReportVO = reportService.orderStatistics(dataOverViewQueryDTO);

        return Result.success(orderReportVO);
    }
}
