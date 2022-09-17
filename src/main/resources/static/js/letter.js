$(function(){
	$("#sendBtn").click(send_letter);
	$(".close").click(delete_msg);
});

function send_letter() {
	$("#sendModal").modal("hide");
	var toUserName = $("#recipient-name").val();
	var content = $("#message-text").val();
	axios({
		url: "http://localhost:8080/message/letter",
		method: "POST",
		params: {
			toUserName,
			content
		}
	}).then(function (res){
		var data = res.data;
		$("#hintBody").text = data.msg;

		$("#hintModal").modal("show");
		setTimeout(function(){
			$("#hintModal").modal("hide");
		}, 3000);
	})

}

function delete_msg() {
	// TODO 删除数据
	$(this).parents(".media").remove();
}