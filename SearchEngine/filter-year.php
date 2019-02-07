<div class="collapse" id="year">
    <button class="toggle-close" type="button" data-toggle="collapse" data-target="#year" aria-controls="byyear">
        <span class="glyphicon glyphicon-remove"></span> Close
    </button>
    <div class="filter-cat">
        <form class="year" method=post action="search.php">
            <input type="hidden" name="start" value="0">
            <input type="hidden" name="suchbegriff" value="<?php print_r($suchTerm === '*' ? '' : $suchTerm) ?>">
            <input type="hidden" name="discipline" value="<?php print_r($facetDiscipline); ?>">

            <?php
            if ($facetYear == null) {
                ?>

                <button type="submit" class="linkselected">All years</button>

                <?php
            } else {
                ?>

                <button type="submit">All years</button>

                <?php
            }
            ?>
        </form>

        <form method=post action="search.php">
            <input type="hidden" name="start" value="0">
            <input type="hidden" name="suchbegriff" value="<?php print_r($suchTerm) ?>">
            <input type="hidden" name="discipline" value="<?php print_r($facetDiscipline); ?>">
            <input type="hidden" name="year" value="[* TO 2006]">

            <?php
            if (strstr($facetYear, '[* TO 2005]')) {
                ?>

                <button type="submit" class="linkselected">Before 2006 (<?php print_r($response->facet_counts->facet_queries['year:[* TO 2005]']); ?>)</button>

                <?php
            } else {
                ?>

                <button type="submit">Before 2006 (<?php print_r($response->facet_counts->facet_queries['year:[* TO 2005]']); ?>)</button>

                <?php
            }
            ?>
        </form>

        <form method=post action="search.php">
            <input type="hidden" name="start" value="0">
            <input type="hidden" name="suchbegriff" value="<?php print_r($suchTerm) ?>">
            <input type="hidden" name="discipline" value="<?php print_r($facetDiscipline); ?>">
            <input type="hidden" name="year" value="[2006 TO 2010]">

            <?php
            if (strstr($facetYear, '[2006 TO 2010]')) {
                ?>

                <button type="submit" class="linkselected">2006 - 2010 (<?php print_r($response->facet_counts->facet_queries['year:[2006 TO 2010]']); ?>)</button>

                <?php
            } else {
                ?>

                <button type="submit">2006 - 2010 (<?php print_r($response->facet_counts->facet_queries['year:[2006 TO 2010]']); ?>)</button>

                <?php
            }
            ?>
        </form>

        <form method=post action="search.php">
            <input type="hidden" name="start" value="0">
            <input type="hidden" name="suchbegriff" value="<?php print_r($suchTerm) ?>">
            <input type="hidden" name="discipline" value="<?php print_r($facetDiscipline); ?>">
            <input type="hidden" name="year" value="[2011 TO *]">

            <?php
            if (strstr($facetYear, '[2011 TO *]')) {
                ?>

                <button type="submit" class="linkselected">Since 2011 (<?php print_r($response->facet_counts->facet_queries['year:[2011 TO *]']); ?>)</button>

                <?php
            } else {
                ?>

                <button type="submit">Since 2011 (<?php print_r($response->facet_counts->facet_queries['year:[2011 TO *]']); ?>)</button>

                <?php
            }
            ?>
        </form>
    </div>
</div>