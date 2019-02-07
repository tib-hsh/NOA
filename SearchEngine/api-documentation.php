<?php include ("navbar.php"); ?>

<div class="main">
    <div class="container">

        <div class="about col-sm-10 col-sm-offset-1">
            <h1 class="h3">API</h1>
            <br/>
            <p>Base URL: <i>noa.wp.hs-hannover.de/api.php?query=</i><br/>
                Add queries to the base URL like you would in SOLR.</p>
            <p><strong>Examples:</strong><br/>
                <i>api.php?query=discipline:"Computer+Science"</i><br/>
                <i>api.php?query=doi:"10.1155/2010/419493"</i><br/>
                <i>api.php?query=discipline:"Physics"+AND+year:2009</i></p>
            <br/>
            <p>By default, there will be 10 results per page. You can change this by adding the parameter "results".</p>
            <p><strong>Examples:</strong><br/>
                <i>api.php?query=discipline:"Physics"&results=50</i><br/>
                will return the first 50 results from the physics discipline.<p>
            <p>By adding the parameter "start" you can choose which result to start from.</p>
            <p><strong>Examples:</strong><br/>
                <i>api.php?query=discipline:"Physics"&results=50&start=50</i><br/>
                will return the results from 50-99.</p>
        </div>

    </div>
</div>
</div><!-- #wrapper closes -->

<?php include ("footer.php"); ?>

