
# JavaScript

`noa.js` (general JS functions)  
`settings.js` (JS functions concerning settings)  
`cite.js` (JS functions concerning cite-button)  
`uploadtool.js` (JS functions concerning image upload to Wikimedia Commons)  


# PHP Structure


`header.php` (builds the frame)  
is included by  
`navbar.php`  
is included by  
`index.php` (start page) | `search.php` (search result page) | `contact.php` (contact page) | `about.php` (about page) | `imprint.php` (imprint page) | `api-documentation.php`  
includes  
`footer.php` (footer, closes the frame)  

------


`search.php` (search result page)  
includes  
`filter.php` (filter bar frame) | `view-list.php` (list view) | `view-gallery.php` (gallery view) | `browse.php` (browsing through result pages)  

------

`filter.php` (filter bar frame)  
includes  
`filter-year.php` | `filter-journal.php` | `settings.php`  

------

`view-list.php` (list view) | `view-gallery.php` (gallery view)  
includes  
`upload-dialog.php` (dialog for choosing categories for image upload to Wikimedia)  

------

`OAuthFunctions.php` (functions for Wikimedia authorization)  
is included by  
`search.php` (search result page) | `handleUploadRequest.php` (uploads images to Wikimedia via AJAX call) | `random-upload.php` (shows random images to be uploaded) | `upload.php` (start page for OAuth) | `uploadtool.php` (landing page for OAuth)  

------

Styling is done in `main.css`  
Logos and other images can be found in img folder.


# Upload Tool

Images can be uploaded to Wikimedia Commons via `upload-dialog.php` which is contained in `view-gallery.php` and `view-list.php`, or via `random-upload.php` where random images are shown to the user.  
In both cases an Wikimedia account must be authorized via OAuth. All functionality regarding OAuth can be found in `OAuthFunctions.php`.  
The start page for authorization redirection is `upload.php` and the landing page is `uploadtool.php`.  
`uploadtool.js` contains all client-side functions for the upload tool. For server-side functions AJAX calls to `handleUploadRequest.php`, `incrementViewCount.php` and `markForUpload.php` are made, where calls to the Wikimedia API are made and data is stored in a MySQL database.  
To use OAuth a consumer key and secret are needed, which are stored in `config.ini`.
