$(function(){
	$("#publishBtn").click(publish);
});

function publish() {
	$("#publishModal").modal("hide");
	// 获取数据
	var title = $("#recipient-name").val();
	var content = $("#message-text").val();
	// 发ajax请求给服务器
	axios({
		url: "http://localhost:8080/post",
		method: "POST",
		params: {
			title,
			content
		}
	}).then(function (res){
		var data = res.data;
		// 在提示框中显示信息
		$("#hintBody").text = data.msg;
		// 显示提示框
		$("#hintModal").modal("show");
		// 倒计时三秒后自动隐藏提示框
		setTimeout(function (){
			$("#hintModal").modal("hide");
		},3000)
	})
}