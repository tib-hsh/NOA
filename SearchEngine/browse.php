<div class="browse col-md-10 col-md-offset-2 col-sm-9 col-sm-offset-3 col-xs-12">
    <?php
    if ($start > 0) {
        ?>

        <form method=get action="search.php" class="browse-button">
            <input type="hidden" name="start" value="<?php print_r($start - $results_per_page) ?>"/>
            <input type="hidden" name="suchbegriff" value="<?php print_r(htmlspecialchars($suchTerm)) ?>"/>
            <input type="hidden" name="imtype" value="<?php print_r($facetImtype) ?>"/>
            <input type="hidden" name="discipline" value="<?php print_r($facetDiscipline); ?>"/>
            <button type="submit" class="noa-button"/>Previous</button>
        </form>

        <?php
    }

    $pagenr_start = $start / $results_per_page - 5;
    if ($pagenr_start < 0) {
        $pagenr_start = 0;
    }
    $pagenr_max = floor(($nrOfResults - 1) / $results_per_page);
    $pagenr_end = $start / $results_per_page + 5;
    if ($pagenr_end > $pagenr_max) {
        $pagenr_end = $pagenr_max;
    }

    for ($i = $pagenr_start; $i <= $pagenr_end; $i++) {
        ?>

        <form method=get action="search.php" class="browse-button">
            <input type="hidden" name="start" value="<?php print_r($i * $results_per_page) ?>"/>
            <input type="hidden" name="suchbegriff" value="<?php print_r(htmlspecialchars($suchTerm)) ?>"/>
            <input type="hidden" name="imtype" value="<?php print_r($facetImtype) ?>"/>
            <input type="hidden" name="discipline" value="<?php print_r($facetDiscipline); ?>"/>

            <?php
            if ($i == $start / $results_per_page) {
                ?>

                <button type="submit" class="noa-button linkselected"/><?php print_r($i + 1); ?></button>

                <?php
            } else {
                ?>

                <button type="submit" class="noa-button"/><?php print_r($i + 1); ?></button>

                <?php
            }
            ?>
        </form>

        <?php
    }
    if ($nrOfResults > $start + $results_per_page) {
        ?>

        <form method=get action="search.php" class="browse-button">
            <input type="hidden" name="start" value="<?php print_r($start + $results_per_page) ?>"/>
            <input type="hidden" name="suchbegriff" value="<?php print_r(htmlspecialchars($suchTerm)) ?>"/>
            <input type="hidden" name="imtype" value="<?php print_r($facetImtype) ?>"/>
            <input type="hidden" name="discipline" value="<?php print_r($facetDiscipline); ?>"/>
            <button type="submit" class="noa-button"/>Next</button>
        </form>

        <?php
    }
    ?>
</div>
