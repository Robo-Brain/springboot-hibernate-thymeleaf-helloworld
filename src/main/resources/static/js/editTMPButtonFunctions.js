$('#actionForm').submit(function(e) {
    e.preventDefault();
    var formArr = [];
    var formData = {};
    var formId;
    var formSpendId;
    var formAmount;
    var formName;
    var formSalaryPrepaid;
    var formWithdraw;

    $.each(this, function(i, v){
        var input = $(v);

        if (input.attr("name") == 'undefined' || input.attr("name") == undefined){
            console.log('fkin bttn again tryin to get into JSON!');
        } else if(input.attr("name") == 'id') {
            formId = input.val();
        } else if(input.attr("name") == 'spendId') {
            formSpendId = input.val();
        } else if(input.attr("name") == 'amount') {
            formAmount = input.val();
        } else if(input.attr("name") == 'name') {
            formName = input.val();
        }else if(input.attr("id") == 'salaryPrepaid') {
            formSalaryPrepaid = input.val();
        }else if(input.attr("id") == 'withdraw') {
            formWithdraw = input.val();
            formData = {id : formId, spendId : formSpendId, name: formName, amount : formAmount, salaryPrepaid: formSalaryPrepaid, withdraw: formWithdraw};
            formArr.push(formData);
        }
    });

    $.ajax({
        headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        },
        type: "post",
        url: "/saveTMP",
        dataType : 'json',
        data : JSON.stringify(formArr),
        success: $(document).ajaxStop(function(){
            window.location.reload();
        })
    })
});

$('#addForm').submit(function(z) {
    z.preventDefault();

    var formName = $("#newName").val();
    var formAmount = $("#amount").val();
    var formSalaryPrepaid = $(".salaryPrepaid[type='radio']:checked").val();
    var formWithdraw = $(".withdraw[type='radio']:checked").val();;

    formData = {name : formName, amount : formAmount, salaryPrepaid : formSalaryPrepaid, withdraw: formWithdraw};

    console.log(formData);

    $.ajax({
        headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        },
        type: "post",
        url: "/addNewSpend",
        dataType : 'json',
        data : JSON.stringify(formData),
        success: $(document).ajaxStop(function(){
            window.location.reload();
        })
    });
});

$("#hideAddForm").click(function(f){
    f.preventDefault();
    $("#addForm").hide();
    $("#showAddForm").show();
    $("#hideAddForm").hide();
});
$("#showAddForm").click(function(q){
    q.preventDefault();
    $("#addForm").show();
    $("#showAddForm").hide();
    $("#hideAddForm").show();
});

