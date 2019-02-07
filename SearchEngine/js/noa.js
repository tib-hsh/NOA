// show loading screen while not all images are loaded
$(window).on('load', function() {$('.loading-screen').hide(200);checkCookie();$('.browse').show();})

// just one filter-collapse at a time
$('.filter-bar').on('show.bs.collapse', function () {
    var actives = $(this).find('.collapse.in'),
        hasData;
    
    if (actives && actives.length) {
        hasData = actives.data('collapse')
        if (hasData && hasData.transitioning) return
        actives.collapse('hide')
        hasData || actives.data('collapse', null)
    }
});


// feedback for gallery view 
$('.feedback-gallery .noa-button').click(function() {
    var id = $(this).data('id');
    if ($('textarea[data-id=' + id + ']').eq(0).val() == "") {
        var feedback = $('textarea[data-id=' + id + ']').eq(1).val();
    }
    else {
        var feedback = $('textarea[data-id=' + id + ']').eq(0).val();
    };
    var url =  $('img[data-id=' + id + ']').eq(0).attr('src');
    var searchterm = $('input[name="suchbegriff"]').val();
    var thisButton = $(this);
    
    if (!feedback) {
        var temp = thisButton.parent().find('.feedback-response');
        temp.html('Please enter your feedback');}
    else {
        $.post('send-feedback.php', {postFeedback: feedback, postUrl: url, postSearchTerm: searchterm},
            function(response) {
                thisButton.parent().find('.feedback-response').html(response);
                thisButton.prop('disabled', true);
            }
        )
    }
});


// feedback for list view 
$('.feedback-list .noa-button').click(function() {
    var id = $(this).data('id');
    if ($('textarea[data-id=' + id + ']').eq(0).val() == "") {
        var feedback = $('textarea[data-id=' + id + ']').eq(1).val();
    }
    else {
        var feedback = $('textarea[data-id=' + id + ']').eq(0).val();
    };
    var url =  $('img[data-id=' + id + ']').eq(0).attr('src');
    var searchterm = $('input[name="suchbegriff"]').val();
    var thisButton = $(this);
    
    if (!feedback) {
        var temp = thisButton.parent().find('.feedback-response');
        temp.html('Please enter your feedback');}
    else {
        $.post('send-feedback.php', {postFeedback: feedback, postUrl: url, postSearchTerm: searchterm},
            function(response) {
                thisButton.parent().find('.feedback-response').html(response);
                thisButton.prop('disabled', true);
            }
        )
    }
});


// hide metadata modal in gallery view if feedback modal is open
$(".change-modal").click(function(){
    $('.gallery-metadata-modal').modal('hide');
    $('body').removeAttr('style');
});

// clear text from input button functionality
$('.input-search input').val() ? $('#clear-input').css('color', '#5B5B5B') : $('#clear-input').css('color', 'white'); 
$('#clear-input').click(function() {$(this).css('color', 'white').prev('input').focus().val('');})
$('.input-search input').keyup(function() {
    $(this).val() ? $('#clear-input').css('color', '#5B5B5B') : $('#clear-input').css('color', 'white');
})


// build table of contents for search description
var ToC =
    "<h3>On this page:</h3>" +
    "<ul>";

var newLine, el, title, link;

$("section h2").each(function() {

  el = $(this);
  title = el.text();
  link = "#" + el.attr("id");

  newLine =
    "<li>" +
      "<a href='" + link + "'>" +
        title +
      "</a>" +
    "</li>";

  ToC += newLine;

});

ToC +=
   "</ul>";

$(".table-of-contents-section").prepend(ToC);


// Back to top Button
$(document).ready(function(){

    // Der Button wird mit JavaScript erzeugt und vor dem Ende des body eingebunden.
    var back_to_top_button = ['<a href="#top" class="back-to-top noa-button"><span class="glyphicon glyphicon-chevron-up"></span></a>'].join("");
    $("body").append(back_to_top_button)

    // Der Button wird ausgeblendet
    $(".back-to-top").hide();

    // Funktion für das Scroll-Verhalten
    $(function () {
        $(window).scroll(function () {
            if ($(this).scrollTop() > 100) { // Wenn 100 Pixel gescrolled wurde
                $('.back-to-top').fadeIn();
            } else {
                $('.back-to-top').fadeOut();
            }
        });

        $('.back-to-top').click(function () { // Klick auf den Button
            $('body,html').animate({
                scrollTop: 0
            }, 800);
            return false;
        });
    });

});