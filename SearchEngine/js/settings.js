$(document).ready(function() {

    // change view 
	$('.change-view-list').click(function() {
		$('.view-gallery').hide();
		$('.view-list').show();
        $(this).addClass('setting_active');
        $('span.change-view-gallery').removeClass('setting_active');
		setCookie('view', 'list', 100);
        $('.list-view-hidden-image').hide();
	});
	$('.change-view-gallery').click(function() {
		$('.view-list').hide();
		$('.view-gallery').show();
        $(this).addClass('setting_active');
        $('span.change-view-list').removeClass('setting_active');
		setCookie('view', 'gallery', 100);
        $('.list-view-hidden-image').show();
	});

    // change results per page
    $('.results-per-page').click(function() {
        setCookie('results_per_page', $(this).text(), 100);
        $('.results-per-page').removeClass('setting_active');
        $(this).addClass('setting_active');
        location.reload();
    });

    // change text size
    $('#small').click(function() {
        setCookie('text_size', 'small', 100);
        changeTextSize('small');
    });
    $('#normal').click(function() {
        setCookie('text_size', 'normal', 100);
        changeTextSize('normal');
    });
    $('#huge').click(function() {
        setCookie('text_size', 'huge', 100);
        changeTextSize('huge');
    });

    // change if you want to see broken images
    $('.broken-yes').click(function() {
        setCookie('show_broken_images', '1', 100);
        $('.broken-no').removeClass('setting_active');
        $(this).addClass('setting_active');
        location.reload();
    });
    $('.broken-no').click(function() {
        setCookie('show_broken_images', '0', 100);
        $('.broken-yes').removeClass('setting_active');
        $(this).addClass('setting_active');
        location.reload();
    })

    // save settings
    $('#save_settings').click(function() {
        checkCookie();
    });
});


// set cookie
function setCookie(cname, cvalue, exdays) {
    var d = new Date();
    d.setTime(d.getTime() + (exdays * 24 * 60 * 60 * 1000));
    var expires = "expires="+d.toUTCString();
    document.cookie = cname + "=" + cvalue + ";" + expires + ";path=/";
}


// get cookie
function getCookie(cname) {
    var name = cname + "=";
    var ca = document.cookie.split(';');
    for(var i = 0; i < ca.length; i++) {
        var c = ca[i];
        while (c.charAt(0) == ' ') {
            c = c.substring(1);
        }
        if (c.indexOf(name) == 0) {
            return c.substring(name.length, c.length);
        }
    }
    return "";
}


// check cookie 
function checkCookie() {
    // view
    var view = getCookie("view");
    if (view != "") {
        if (view == 'list') {
        	$('.view-gallery').hide();
			$('.view-list').show();
            $('span.change-view-list').addClass('setting_active');
            $('span.change-view-gallery').removeClass('setting_active');
        } 
        else if (view == 'gallery') {
        	$('.view-list').hide();
			$('.view-gallery').show();
            $('span.change-view-gallery').addClass('setting_active');
            $('span.change-view-list').removeClass('setting_active');
        }
    }
    else {
    	$('.view-gallery').hide();
		$('.view-list').show();
    }

    // select correkt results per page
    $('.results-per-page').val(getCookie('results_per_page'));

    // highlight correct results per page
    var rpp = getCookie('results_per_page');
    if (rpp != '') {var rpp_button = '.results-per-page[value=' + getCookie('results_per_page') + ']';}
    else {var rpp_button = '.results-per-page[value=12]';};
    $(rpp_button).addClass('setting_active');

    // text size
    var text_size = getCookie("text_size");
    if (text_size != "") {
        changeTextSize(text_size);
        var ts_span = '#' + text_size;
    }
    else {var ts_span = '#normal';};
    $(ts_span).addClass('setting_active');

    // broken images
    var show_broken_images = getCookie('show_broken_images');
    if (show_broken_images == '0') {
        $('.broken-no').addClass('setting_active');
        var countBroken = 0;
        $('img').on('error', function() {

            $(this).parent().parent().hide();
            $(this).parent().parent().addClass('hidden-image');
            $(this).parent().parent().parent().addClass('list-view-hidden-image');
            if (view == 'list') {$(this).parent().parent().parent().hide();}

            countBroken = $('.gallery-image.hidden-image').length;
            if (countBroken != 0) {
                $('#removed-images').html( ' ('+ countBroken + ' hidden)');
            }
        })
    }
    else {$('.broken-yes').addClass('setting_active');}
}


// add size class to .changeable to be able to style text size responsive
function changeTextSize(size) {
    var sizes = ['small', 'normal', 'huge'];
    for (var i = 0; i < sizes.length; i++) {
       $('#' + sizes[i]).removeClass('setting_active');
       $('.changeable').removeClass(sizes[i]);
    };
    $('#' + size).addClass('setting_active');
    $('.changeable').addClass(size);
}
