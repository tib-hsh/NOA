<div class="filter-bar">

    <div class="search-and-results col-md-6 col-xs-12">
        <!-- search form -->						
        <form class="search-form" action="search.php" method="get">
            <div class="input-field input-search shadows">
                <span class="glyphicon glyphicon-search"></span>
                <input class="input-width" type="text" value="<?= htmlspecialchars($suchTerm) ?>" name="suchbegriff">
                <span id='clear-input' class="glyphicon glyphicon-remove"></span>
            </div>
            <input type ="hidden" name="start" value="0">
            <div class="search-button">
                <button type="submit" class="noa-button search-submit-button">Search</button>
                <a href="search-description.php" class="noa-button help-button">
                    <span class="glyphicon glyphicon-question-sign">
                        <span class="tooltiptext3">Search Description</span>
                    </span>
                </a>
            </div>
        </form>

        <!-- number of currently displayed results-->
        <div class="display-number">
            <?php
            if ($nrOfResults != 0) {
                ?>
                Currently displaying results <?php print_r($start + 1); ?> to 

                <?php
                $end = $start + $results_per_page;
                if ($end > $nrOfResults) {
                    $end = $nrOfResults;
                }
                print_r($end);
                echo '<span id="removed-images"></span>';
                if ($nrOfResults > $results_per_page) {
                    print_r(" of ");
                    print_r($nrOfResults);
                }
                ?>
                <?php
            }
            ?>	
        </div>

    </div>	


    <div class="filter-and-views col-md-6 col-xs-12">
        <!-- buttons for filter, view, settings -->
        <div class="choose">
            <button class="noa-button" type="button" data-toggle="collapse" data-target="#imgtype" aria-expanded="false" aria-controls="byimgtype">
                <span class="glyphicon glyphicon-picture"></span> Filter by image type
            </button>
            <button class="noa-button" type="button" data-toggle="collapse" data-target="#disciplines" aria-expanded="false" aria-controls="bydiscipline">
                <span class="glyphicon glyphicon-filter"></span> Filter by discipline
            </button>
            <div class="view">
                <button class="noa-button change-view-gallery" type="button">
                    <span class="glyphicon glyphicon-th">
                        <span class="tooltiptext4">Change to grid-view</span>
                    </span>
                </button>
                <button class="noa-button change-view-list" type="button">
                    <span class="glyphicon glyphicon-th-list">
                        <span class="tooltiptext5">Change to list-view</span>
                    </span>
                </button>
                <button class="noa-button settings-btn" type="button" data-toggle="collapse" data-target="#configuration" aria-expanded="false" aria-controls="configuration">
                    <span class="glyphicon glyphicon-cog">
                        <span class="tooltiptext6">Personal settings</span>
                    </span>
                </button>
            </div>
            <!-- misspelled start -->			<?php if ($wikiUsername != NULL) { ?>
                <div class="display-number">Connected Wikimedia account: <b><?php print_r($wikiUsername); ?></b></div>
            <?php }
            ?>
        </div>

        <!-- including collapse filter by year -->
        <?php include ("filter-imtype.php"); ?>

        <!-- including collapse filter by journals -->
        <?php include ("filter-discipline.php"); ?>

        <!-- including collapse settings -->
        <?php include ("settings.php"); ?>
    </div>
</div>


