package MyForum.controller;

import MyForum.DTO.Page;
import MyForum.config.Config;
import MyForum.pojo.Post;
import MyForum.service.SearchService;
import cn.hutool.core.util.StrUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * Date: 2022/10/2
 * Time: 20:13
 *
 * @Author SillyBaka
 * Description：
 **/
@Controller
@RequestMapping("/search")
public class SearchController {
    @Resource
    private SearchService searchService;

    @GetMapping("/post")
    public String searchPost(String keyWord, Integer currentPage, Model model){
        // 没有输入关键字 重定向到首页
        if(StrUtil.isBlank(keyWord)){
            return "redirect:/index";
        }
        // 页号不合法 自动改为0
        if(currentPage == null || currentPage < 0){
            currentPage = 0;
        }

        Page<Post> page = searchService.searchPost(keyWord, currentPage, Config.getPageSize());

        model.addAttribute("page",page);
        model.addAttribute("keyWord",keyWord);

        return "site/search";
    }
}
