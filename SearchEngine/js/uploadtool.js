function uploadtool(button, doc, counter, discipline) {
    switch (button) {
        //depending on which button called the function pass doc to corresponding php file
        case 'upload':
            upload(doc, counter);
            break;
        case 'uploadConfirmation' :
            if (confirm('Are you sure you want to upload this image to Wikimedia Commons?')) {
                upload(doc, counter);
            }
            break;
        case 'markForUpload' :
            var request = $.ajax({
                url: 'markForUpload.php',
                data: {document: doc},
                type: 'get',
            });

            request.done(function (data) {
                if (data == "success") {
                    document.getElementById("uploadButton").style.visibility = "hidden";
                    document.getElementById("success").style.display = "block";

                } else {
                    alert(data);
                }
            });

            request.fail(function (jqXHR, textStatus) {
                console.log('Sorry: ' + textStatus);
            });
            break;
        case 'next':
            var request = $.ajax({
                url: 'incrementViewCount.php',
                data: {document: doc},
                type: 'get',
            });

            request.done(function (data) {
                $('#ajaxButton').html(data);
                if (discipline != "") {
                    window.location.href = "random-upload.php?discipline=" + discipline;
                } else {
                    window.location.href = "random-upload.php";
                }

            });

            request.fail(function (jqXHR, textStatus) {
                console.log('Sorry: ' + textStatus);
            });
            break;
        case 'redirect' :
            //get redirection url
            var url = "";
            var request = $.ajax({
                url: 'upload.php',
                data: {ajax: 'true'},
                type: 'get',
            });
            request.done(function (data) {
                $('#ajaxButton').html(data);
                url = data;
            });
            //Check if HTML <dialog> element is supported in browser, else use confirm()
            if (typeof redirectDialog.showModal === "function") {
                redirectDialog.showModal();
                var confirmButton = document.getElementById("confirmButton");
                confirmButton.addEventListener('click', function redirect() {
                    window.open(url, '_blank')
                    setTimeout(function () {
                        reloadDialog.showModal();
                        reloadDialog.addEventListener('close', function onClose() {
                            if (reloadDialog.returnValue == 'confirm') {
                                location.reload()
                            }
                        });
                    }, 1000);
                });
            } else {
                if (confirm('To upload images to Wikimedia Commons you need to authorize the NOA Upload tool to upload in your name.\nDo you wish to continue with the authorization?')) {
                    window.open(url, '_blank');
                    setTimeout(function () {
                        if (confirm("To continue with the upload you need to reload the page.")) {
                            location.reload()
                        }
                    }, 1000);
                }
            }
    }
}

function upload(doc, counter) {
    var categories = "";

    var json = JSON.parse(doc);
    //check which categories were selected by the user 
    if (json.hasOwnProperty('wmcat')) {
        var wmcats = json['wmcat'];
        wmcats.forEach(function (element) {
            if (document.getElementById(element + counter).checked) {
                categories += "\n[[Category:" + element + "]]";
            }
        });
    }

    var div = document.getElementById("loading" + counter);
    div.style.visibility = 'visible';

    var request = $.ajax({
        url: 'handleUploadRequest.php',
        data: {document: doc, categories: categories},
        type: 'get',
    });

    request.done(function (data) {
        var result = $.parseJSON(data);
        document.getElementById("loading" + counter).style.visibility = 'hidden';
        //if upload was successfull show link to image and hide upload button
        if (result[0] == "success") {
            document.getElementById("linkToImage" + counter).href = "https://commons.wikimedia.org/wiki/File:" + result[1];
            document.getElementById("uploadButton" + counter).style.display = "none";
            document.getElementById("success" + counter).style.display = "block";

        } else {
            alert(result[0]);
        }
    });

    request.fail(function (jqXHR, textStatus) {
        console.log('Sorry: ' + textStatus);
    });
}