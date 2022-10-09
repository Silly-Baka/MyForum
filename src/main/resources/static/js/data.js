
// 统计日活跃用户
function getDAU(){
    var form = document.getElementById("dauForm");
    // 获取表单数据
    var formData = new FormData(form);
    var startDateStr = formData.get("startDate").toString();
    var endDateStr = formData.get("endDate").toString();
    console.log(startDateStr)
    console.log(endDateStr)
    axios({
        url: "http://localhost:8080/data/dau",
        method: "POST",
        params:{
            startDateStr,
            endDateStr
        }
    }).then(function (res){
        var data = res.data;
        if(data.code == 200){
            $("#dauResult").text(data.result);
        }
    })
}

// 统计日期区间的独立访客数
function getUV(){
    var form = document.getElementById("uvForm");
    var formData = new FormData(form);

    var startDateStr = formData.get("startDate").toString();
    var endDateStr = formData.get("endDate").toString();

    axios({
        url: "http://localhost:8080/data/uv",
        method: "POST",
        params: {
            startDateStr,
            endDateStr
        }
    }).then(function (res){
        var data = res.data;
        // 如果获取成功
        if(data.code == 200){
            $("#uvResult").text(data.result)
        }
    })
}