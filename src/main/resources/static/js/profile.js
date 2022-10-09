$(function(){
	$(".follow-btn").click(follow);
});

function follow() {
	var btn = this;

	var targetUserId = $(btn).prev().val();

	console.log(targetUserId);

	axios({
		url: "http://localhost:8080/common/follow/" + targetUserId,
		method: "GET"
	}).then(function (res){
		var data = res.data;
		console.log(data);
		if(data.code == 0){
			alert(data.msg);
		}else {
			if(data.followStatus == true){
				$(btn).text("已关注").removeClass("btn-info").addClass("btn-secondary");
			}else{
				$(btn).text("关注TA").removeClass("btn-secondary").addClass("btn-info");

			}
			$("#followedCount").text(data.followedCount);
		}
	})
}