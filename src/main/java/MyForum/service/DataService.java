package MyForum.service;

import java.time.LocalDate;

/**
 * Date: 2022/10/8
 * Time: 20:26
 *
 * @Author SillyBaka
 * Description：统计数据的接口
 **/
public interface DataService {
    /**
     * 统计并获取指定日期内的日活跃用户总数
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 日期区间的日活跃用户数
     */
    Long getDAU(LocalDate startDate, LocalDate endDate);

    /**
     * 统计并获取指定日期区间的独立访客数目
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 日期区间的独立访客数
     */
    Long getUV(LocalDate startDate, LocalDate endDate);
}
