var withdrawSalary = 0;
var withdrawNotSalary = 0;

var withdrawPrepaid = 0;
var withdrawNotPrepaid = 0;

function appendCurrentDateTable() {

    currentMonth = curMonth.slice(0);
    currentMonth.sort(function(a,b) {
        return a.index - b.index;
    });
    console.log(currentMonth);

    for (i = 0; i < currentMonth.length; i++) {
        $("#currentDate").html(currentMonth[i].date);
        $(".divTable").append(
            "<div  class='divTableRow " + i + "'>" +
            "<div class='divTableCell left'>" + currentMonth[i].name +
                "<input type='hidden' id='id' name='id' value='" + currentMonth[i].id + "' />" +
                "<input type='hidden' id='date' name='date' value='" + currentMonth[i].date + "' />" +
                "<input type='hidden' id='spendName' name='spendName' value='" + currentMonth[i].spendName + "' />" +
            "</div>" +
            "<div class='divTableCell left'>" +
                "<input type='text' class='amount" + i + "' id='amount" + i + "' name='amount' value='" + currentMonth[i].monthAmount + "' autocomplete='off' />&nbsp;" +
                "<button value='" + currentMonth[i].amount + "' class='" + i + "'>&nbsp;<<<&nbsp;</button>&nbsp;" +
                "<span class='" + i + "TMP'>" + parseInt(currentMonth[i].amount).toLocaleString('ru-RU',{style:'currency', currency:'RUB'}) + "</span>"+
            "</div>" +
            "<div class='divTableCell " + i + " " + currentMonth[i].withdraw + "'>" +
            "</div>" +
            "</div>");

        if (currentMonth[i].salaryPrepaid == 'true' && currentMonth[i].withdraw == 'true'){
            $('.divTableRow.' + i).addClass('salary');
            withdrawSalary += +currentMonth[i].amount;
        } else if (currentMonth[i].salaryPrepaid == 'true' && currentMonth[i].withdraw == 'false'){
            $('.divTableRow.' + i).addClass('salary');
            withdrawNotSalary += +currentMonth[i].amount;
        } else if (currentMonth[i].salaryPrepaid == 'false' && currentMonth[i].withdraw == 'true'){
            $('.divTableRow.' + i).addClass('prepaid');
            withdrawPrepaid += +currentMonth[i].amount;
        } else if (currentMonth[i].salaryPrepaid == 'false' && currentMonth[i].withdraw == 'false'){
            $('.divTableRow.' + i).addClass('prepaid');
            withdrawNotPrepaid += +currentMonth[i].amount;
        }

        if (parseInt(currentMonth[i].amount) > parseInt(currentMonth[i].monthAmount)) {
            $(".amount" + i).toggleClass('notEnough');
        }

    }
}

function appendResultTable() {
    $(".resultTable").append( // amount of expenses from prepaid and salary
        "<div class='divTableRow salary'>" +
        "<div class='divTableCell true'></div>" +
        "<div class='divTableCell'>" + withdrawSalary.toLocaleString('ru-RU',{style:'currency', currency:'RUB'}) + "</div>" +
        "<div class='divTableCell false'></div>" +
        "<div class='divTableCell'>" + withdrawNotSalary.toLocaleString('ru-RU',{style:'currency', currency:'RUB'}) + "</div></div>" +
        "<div class='divTableRow'>" +
        "<div class='divTableCell nope'></div><div class='divTableCell nope'></div><div class='divTableCell nope'></div>" +
        "<div class='divTableCell salary'>" + sum(withdrawSalary, withdrawNotSalary).toLocaleString('ru-RU',{style:'currency', currency:'RUB'}) + "</div></div>"
        +
        "<div class='divTableRow prepaid'>" +
        "<div class='divTableCell true'></div>" +
        "<div class='divTableCell'>" + withdrawPrepaid.toLocaleString('ru-RU',{style:'currency', currency:'RUB'}) + "</div>" +
        "<div class='divTableCell false'></div>" +
        "<div class='divTableCell'>" + withdrawNotPrepaid.toLocaleString('ru-RU',{style:'currency', currency:'RUB'}) + "</div></div>" +
        "<div class='divTableRow'>" +
        "<div class='divTableCell nope'></div><div class='divTableCell nope'></div><div class='divTableCell nope'></div>" +
        "<div class='divTableCell prepaid'>" + sum(withdrawPrepaid, withdrawNotPrepaid).toLocaleString('ru-RU',{style:'currency', currency:'RUB'}) + "</div></div>"
    );
}

function sum(x, y) {
    return parseInt(x) + parseInt(y);
}
