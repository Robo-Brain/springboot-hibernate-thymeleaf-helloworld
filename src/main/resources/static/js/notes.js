function appendNotes() {

    notes = notesArr.slice(0);
    notes.sort(function(a,b) {
        return new Date(b.date) - new Date(a.date);
    });

    for (i = 0; i < notes.length; i++) {
        $("#notesTable").append(
            "<div class='divTableRow " + notes[i].id + " note'>" +
                "<input type='hidden' id='id' name='id' value='" + notes[i].id + "' />" +
                "<div class='divTableCell " + notes[i].id + " noteDate exist'>" + notes[i].date + "</div>" +
                    "<div class='divTableCell hidden " + notes[i].id + " noteDate'>" +
                    "<input type='hidden' id='noteId' name='noteId' value='" + notes[i].id + "' />" +
                    "<input type='text' id='noteDate" + notes[i].id + "' name='noteDate' class='date' value='" + notes[i].date + "' />" +
                    "</div>" +

                "<div class='divTableCell " + notes[i].id + " noteText'>" + notes[i].text + "</div>" +
                    "<div class='divTableCell hidden " + notes[i].id + " noteTextInput'>" +
                        "<textarea class='noteTextarea' id='noteText" + notes[i].id + "' name='noteText' >" + notes[i].text + "</textarea>" +
                    "</div>" +

                "<div class='divTableCell " + notes[i].id + ' ' + notes[i].remind + " noteRemind'>" +
                    "<span id='" + notes[i].id + "' class='bell " + notes[i].remind + "'>&nbsp;</span>" +
                    "<span class='remindBoolean'>" + notes[i].remind + "</span>" +
                "</div>" +

                "<div class='divTableCell salaryEditButtons " + notes[i].id + "'>" +
                    "<div class='editButtons'>" +
                        "<button class='editButton' id='" + notes[i].id + "'>EDIT</button>&nbsp;" +
                        "<button class='delButton' id='" + notes[i].id + "'>DEL</button>" +
                    "</div>" +
                "</div>" +
                    "<div class='divTableCell hidden salaryEditButtons " + notes[i].id + "'>" +
                        "<div class='salaryHiddenButtons'>" +
                            "<button class='saveButton' id='" + notes[i].id + "'>✓</button><button class='cancelButton' id='" + notes[i].id + "'>X</button>" +
                        "</div>" +
                    "</div>" +

            "</div>");

        $('.true').prop('checked', true);
        $('.false').prop('unchecked', false);
    }
}

$(".addNotePC").click(function (x) {
    x.preventDefault();
    addNote( $('#date').val(), $('#text').val(), $('#isRemind').is(':checked'));
});

$('.addNoteMobile').click(function (a) {
    a.preventDefault();
    $("#mobileAddNote").dialog({
        classes: {
            "ui-dialog": "ui-dialogMobile"
        },
        resizable: false,
        height: "auto",
        width: 400,
        modal: true,
        buttons: {
            "Add": function() {
                $( this ).dialog( "close" );
                addNote( $('#mobileDate').val(), $('#mobileText').val(), $('#mobileIsRemind').is(':checked'));
            },
            Cancel: function() {
                $(this).dialog( "close" );
                $(this).remove();
            }
        }
    });

});

function addNote(date, text, remind) {

    if (text == '') {
        text = null;
    }

    var note = {date: date, text: text, remind: remind};

    $.ajax({
        headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        },
        type: "post",
        url: "/addNote",
        data : JSON.stringify(note),
        success: $(document).ajaxStop(function(){
            window.location.reload();
        })
    });

}

$(function() {
    $(".divTableCell.hidden").hide();
});

function searchForRemind() {
    var today = new Date();
    var nowAYear = today.getFullYear();
    var nowAMonth = today.getMonth() + 1;
    var nowAday = today.getDate();

    for (i = 0; i < notes.length; i++) {
        var arrIsRemind = notes[i].remind;

        if (arrIsRemind == 1) {

            var date = notes[i].date;
            var notesYear = date.substring(0,4);
            var notesMonth = date.substring(5,7);
            var notesDay = date.substring(8,10);

            if (notesYear <= nowAYear && notesMonth <= nowAMonth && notesDay <= nowAday) {
                notice(notes[i].id);
            }
        }
    }
}

function notice(id) {
    var n = new Noty({
        type: 'error',
        theme: 'metroui',
        text: notes[i].text,
        buttons: [
            Noty.button('DONE', 'btn btn-success', function () {
                $.ajax({
                    headers: {
                        'Accept': 'application/json',
                        'Content-Type': 'application/json'
                    },
                    type: "post",
                    url: "/muteNote",
                    data : JSON.stringify(id)
                });
                n.close();
            }),
            Noty.button('NOPE', 'btn btn-error', function () {
                n.close();
            })
        ]
    }).show();
}

$(function() {
    $(".editButton").click(function(e) {
        e.preventDefault();
        var id = $(this).attr('id');

        $('.divTableCell').show();
        $('.divTableCell.hidden').hide();

        $('.divTableCell.' + id).hide();
        $('.divTableCell.hidden.' + id).show();
    });
});

$(function() {
    $(".delButton").click(function(d) {
        d.preventDefault();
        var id = $(this).attr('id');
        $.ajax({
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            },
            type: "post",
            url: "/delNote",
            data : id,
            success: $(document).ajaxStop(function(){
                window.location.reload();
            })
        });
    });
});

$(function() {
    $(".saveButton").click(function (s) {
        s.preventDefault();
        var noteId = $(this).attr('id');
        var noteDate = $('#noteDate' + noteId).val();
        var noteText = $('#noteText' + noteId).val();
        var noteIsRemind = $('#isRemind' + noteId).is(':checked');
        saveExistNote(noteId, noteDate, noteText, noteIsRemind);
    });
});

function saveExistNote(id, date, text, isRemind) {

    existNoteData = JSON.stringify({id: id, date: date, text: text, remind: isRemind});

    $.ajax({
        headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        },
        type: "post",
        url: "/saveExistNote",
        dataType : 'json',
        data : existNoteData,
        success: $(document).ajaxStop(function(){
            window.location.reload();
        })
    })

}

$(function() {
    $(".cancelButton").click(function(c) {
        c.preventDefault();
        var id = $(this).attr('id');
        $('.divTableCell.' + id).show();
        $('.divTableCell.hidden.' + id).hide();
    });
});


$(function() {
    $(".date").pickadate({
        format: 'yyyy-mm-dd',
        formatSubmit: 'yyyy-mm-dd'
    });
    $("#date").pickadate({
        format: 'yyyy-mm-dd',
        formatSubmit: 'yyyy-mm-dd'
    });
    $("#mobileDate").pickadate({
        format: 'yyyy-mm-dd',
        formatSubmit: 'yyyy-mm-dd'
    });
});