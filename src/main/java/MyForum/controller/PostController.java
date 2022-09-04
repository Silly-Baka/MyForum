package MyForum.controller;

import MyForum.DTO.Page;
import MyForum.DTO.UserDTO;
import MyForum.common.UserHolder;
import MyForum.pojo.Comment;
import MyForum.pojo.Post;
import MyForum.service.CommentService;
import MyForum.service.PostService;
import MyForum.util.CommonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Date: 2022/8/8
 * Time: 12:39
 *
 * @Author SillyBaka
 *
 * Description：关于帖子的controller
 **/
@Controller
@Slf4j
public class PostController {
    @Autowired
    private PostService postService;
    @Autowired
    private CommentService commentService;


    @GetMapping(value = {"/post/list/{currentPage}","/post/list","/index"})
    public String getPostsDefault(Model model,
                                  @PathVariable(value = "currentPage",required = false) Integer currentPage){
        // 不输入页号则默认为1
        if(currentPage == null){
            currentPage = 1;
        }
        Page<Post> page = postService.getPostList(currentPage);
        model.addAttribute("page",page);

        return "/index";
    }

    /**
     * 获取热点帖子 赞和回帖最多的帖子
     */
    @GetMapping(value = {"/post/list/hot","/post/list/hot/{currentPage}"})
    public String getHotPosts(Model model,
                              @PathVariable(value = "currentPage",required = false) Integer currentPage){
        // 不输入页号则默认为1
        if(currentPage == null){
            currentPage = 1;
        }
        Page<Post> page = postService.getHotPosts(currentPage);
        model.addAttribute("page",page);
        model.addAttribute("isHot",true);

        return "/index";
    }

    /**
     * 发布帖子
     * @param title 帖子标题
     * @param content 帖子内容
     * @return 返回统一的Json处理结果
     */
    @PostMapping("/post")
    @ResponseBody
    public String publishPost(@RequestParam("title") String title, @RequestParam("content") String content){
        UserDTO currentUser = UserHolder.getCurrentUser();
        if(currentUser == null){
            log.debug("用户尚未登录，发布帖子失败");
            return CommonUtil.getJsonString(403,"用户尚未登录，发布帖子失败");
        }
        postService.publishPost(title,content);

        System.out.println(CommonUtil.getJsonString(0, "帖子发布成功！"));
        return CommonUtil.getJsonString(0,"帖子发布成功！");
    }

    @GetMapping("/post/{postId}")
    public String getPostById(@PathVariable("postId") Long postId, Model model){
        if(postId == null){
            return "";
        }
        Post post = postService.getPostById(postId);
        model.addAttribute("post",post);

        Page<Comment> page = commentService.getCommentListByPostId(postId, 1);
        model.addAttribute("page",page);

        return "site/discuss-detail";
    }

    @GetMapping("/post/user/{userId}/{currentPage}")
    public String getPostListByUserId(@PathVariable("userId") Long userId ,
                                      @PathVariable("currentPage") Integer currentPage,
                                      Model model){
        //todo 后面再统一处理异常页面
        if(userId == null){
            return "";
        }
        if(currentPage == null || currentPage <= 0){
            currentPage = 1;
        }
        Page<Post> page = postService.getPostListByUserId(userId, currentPage);
        model.addAttribute("page",page);
        model.addAttribute("userId",userId);

        return "site/my-post";
    }
}
