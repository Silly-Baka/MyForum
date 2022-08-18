package MyForum.controller;

import MyForum.DTO.Page;
import MyForum.pojo.PostDetail;
import MyForum.service.PostDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Date: 2022/8/8
 * Time: 12:39
 *
 * @Author SillyBaka
 *
 * Description：关于帖子的controller
 **/
@Controller
public class PostDetailController {
    @Autowired
    private PostDetailService postDetailService;

    @GetMapping(value = {"/postDetail/{currentPage}","/postDetail","/index"})
    public String getPostsDefault(Model model,
                                  @PathVariable(value = "currentPage",required = false) Integer currentPage){
        // 不输入页号则默认为0
        if(currentPage == null){
            currentPage = 0;
        }
        Page<PostDetail> page = postDetailService.getPostList(currentPage);
        model.addAttribute("page",page);

        return "/index";
    }

    /**
     * 获取热点帖子 赞和回帖最多的帖子
     */
    @GetMapping(value = {"/postDetail/hot","/postDetail/hot/{currentPage}"})
    public String getHotPosts(Model model,
                              @PathVariable(value = "currentPage",required = false) Integer currentPage){
        // 不输入页号则默认为0
        if(currentPage == null){
            currentPage = 0;
        }
        Page<PostDetail> page = postDetailService.getHotPosts(currentPage);
        model.addAttribute("page",page);
        model.addAttribute("isHot",true);

        return "/index";
    }
}
