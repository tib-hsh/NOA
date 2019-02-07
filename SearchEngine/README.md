# Upload Tool

Images can be uploaded to Wikimedia Commons via upload-dialog.php which is contained in view-gallery.php and view-list.php, or via random-upload.php where random images are shown to the user.  
In both cases an Wikimedia account must be authorized via OAuth. All functionality regarding OAuth can be found in OAuthFunctions.php.  
The start page for authorization redirection is upload.php and the landing page is uploadtool.php.  
uploadtool.js contains all client-side functions for the upload tool. For server-side functions AJAX calls to handleUploadRequest.php, incrementViewCount.php and markForUpload.php are made, where calls to the Wikimedia API are made and data is stored in a MySQL database.  
To use OAuth a consumer key and secret are needed, which are stored in data/config.ini.