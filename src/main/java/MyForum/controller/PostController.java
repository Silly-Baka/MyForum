package MyForum.controller;

import MyForum.DTO.CommentDTO;
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

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    @Resource
    private PostService postService;
    @Resource
    private CommentService commentService;


    @GetMapping(value = {"/post/list/{currentPage}","/post/list","/index"})
    public String getPostsDefault(Model model,
                                  @PathVariable(value = "currentPage",required = false) Integer currentPage){
        // 不输入页号则默认为1
        if(currentPage == null || currentPage <= 0){
            currentPage = 1;
        }
        Page<Post> page = postService.getPostList(currentPage);
        model.addAttribute("page",page);

        model.addAttribute("isNew",true);

        return "/index";
    }

    /**
     * 获取热点帖子 赞和回帖最多的帖子
     */
    @GetMapping(value = {"/post/list/hot","/post/list/hot/{currentPage}"})
    public String getHotPosts(Model model,
                              @PathVariable(value = "currentPage",required = false) Integer currentPage){
        // 不输入页号则默认为0
        if(currentPage == null || currentPage <= 0){
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
            throw new RuntimeException("非法参数！访问失败");
        }
        Post post = postService.getPostById(postId);
        model.addAttribute("post",post);

        Page<CommentDTO> page = commentService.getCommentListByPostId(postId, 1);
        model.addAttribute("page",page);

        return "site/discuss-detail";
    }

    @GetMapping("/post/user/{userId}/{currentPage}")
    public String getPostListByUserId(@PathVariable("userId") Long userId ,
                                      @PathVariable("currentPage") Integer currentPage,
                                      Model model){
        //todo 后面再统一处理异常页面
        if(userId == null){
            throw new RuntimeException("非法参数！访问失败");
        }
        if(currentPage == null || currentPage <= 0){
            currentPage = 1;
        }
        Page<Post> page = postService.getPostListByUserId(userId, currentPage);
        model.addAttribute("page",page);
        model.addAttribute("userId",userId);

        return "site/my-post";
    }

    /**
     * 点赞帖子
     */
    @GetMapping("/post/like/{postId}")
    @ResponseBody
    public String likePost(@PathVariable("postId") Long postId){


        if(UserHolder.getCurrentUser() == null){
            return CommonUtil.getJsonString(0,"点赞失败！用户尚未登录！");
        }
        if(postId == null){
            return CommonUtil.getJsonString(0,"点赞失败！参数不能为空");
        }

        Map<String, Object> map = postService.likePost(postId);

        return CommonUtil.getJsonString(200,"点赞操作成功",map);
    }

    /**
     * 加精目标帖子
     * @param postId 目标帖子id
     * @param status 当前帖子状态
     */
    @GetMapping("/post/wonderful/{postId}/{status}")
    public String setWonderful(@PathVariable("postId") Long postId, @PathVariable("status") Integer status){
        if(postId == null || status == null){
            throw new RuntimeException("非法参数！无法访问");
        }

        postService.setPostWonderful(postId,status);

        return "redirect:/post/"+postId;
    }

    /**
     * 置顶目标帖子
     * @param postId 目标帖子id
     * @param type 目标帖子当前类型
     */
    @GetMapping("/post/top/{postId}/{type}")
    public String setTop(@PathVariable("postId") Long postId, @PathVariable("type") Integer type){
        if(postId == null || type == null){
            throw new RuntimeException("非法参数！无法访问");
        }

        postService.setPostTop(postId,type);

        return "redirect:/post/"+postId;
    }

    @GetMapping("/post/delete/{postId}")
    public String deletePost(@PathVariable("postId") Long postId){
        if(postId == null){
            throw new RuntimeException("非法参数！无法访问");
        }

        postService.deletePost(postId);

        return "redirect:/index";
    }
}
