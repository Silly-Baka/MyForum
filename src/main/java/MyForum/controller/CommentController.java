package MyForum.controller;

import MyForum.DTO.CommentDTO;
import MyForum.DTO.Page;
import MyForum.common.UserHolder;
import MyForum.pojo.Comment;
import MyForum.pojo.Post;
import MyForum.service.CommentService;
import MyForum.service.PostService;
import MyForum.util.CommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Date: 2022/8/30
 * Time: 15:16
 *
 * @Author SillyBaka
 * Description：
 **/
@Controller
@RequestMapping("/comment")
public class CommentController {

    @Autowired
    private CommentService commentService;
    @Autowired
    private PostService postService;

    @GetMapping(value = {"/{postId}/{currentPage}"})
    public String getCommentByPostId(@PathVariable("postId") Long postId ,
                                     @PathVariable("currentPage") Integer currentPage,
                                     Model model){
        if(currentPage == null || currentPage <= 0){
            currentPage = 1;
        }
        Page<CommentDTO> page = commentService.getCommentListByPostId(postId, currentPage);
        model.addAttribute("page",page);

        // 偷懒 主要不知道如何复用上一个请求获得的post 可以通过缓存减少损耗？
        Post post = postService.getPostById(postId);
        model.addAttribute("post",post);

        return "site/discuss-detail";
    }

    @PostMapping("/{postId}")
    public String addPostComment(@PathVariable("postId") Long postId, Comment comment){

        commentService.addPostComment(postId,comment);

        return "redirect:/post/"+postId;
    }

    @GetMapping("/user/{userId}/{currentPage}")
    public String getCommentByUserId(@PathVariable("userId") Long userId,
                                     @PathVariable("currentPage") Integer currentPage,
                                     Model model){
        if(currentPage == null || currentPage <= 0){
            currentPage = 1;
        }
        Page<CommentDTO> page = commentService.getCommentListByUserId(userId, currentPage);
        model.addAttribute("page",page);
        model.addAttribute("userId",userId);

        return "site/my-reply";
    }
}
