package MyForum.DTO;

import MyForum.config.Config;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Date: 2022/8/8
 * Time: 20:12
 *
 * @Author SillyBaka
 *
 * Description：用于封装分页信息 （采用逻辑分页 避免物理分页的大流量）
 *       MyBatis的PageHelper不好用 自己封装一个
 **/
@Data
public class Page<T> implements Serializable {
    /**
     * 现页号
     */
    private Integer currentPage;
    /**
     * 每页大小
     */
    private Integer pageSize;
    /**
     * 总数据条数
     */
    private Integer totalCount;
    /**
     * 总页号
     */
    private Integer total;
    /**
     * 本页记录
     */
    private List<T> records;
    /**
     * 用于前端分页
     */
    private Integer from;

    /**
     * 用于前端分页
     */
    private Integer to;

    public Page(){
        pageSize = Config.getPageSize();
    }
    public Page(Integer totalRows){
        this();
        this.totalCount = totalRows;
        total = getTotalPage();
    }
    public Page(Integer currentPage, Integer totalRows) {
        this();
        this.currentPage = currentPage;
        this.totalCount = totalRows;
        total = getTotalPage();
        from = currentPage;
        // 固定只能同时加载五页
        int maxPerPage = Config.getMaxPerPage();
        if(currentPage + maxPerPage > total){
            to = total;
        }else {
            to = currentPage+maxPerPage;
        }
    }

    /**
     * 获取总页号  0-n
     */
    private Integer getTotalPage(){
        total = totalCount/pageSize;
        if(totalCount % pageSize != 0){
            total+=1;
        }
        return total;
    }

}
