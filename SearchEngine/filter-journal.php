<div class="collapse" id="journals">
    <button class="toggle-close" type="button" data-toggle="collapse" data-target="#journal" aria-controls="byjournal">
        <span class="glyphicon glyphicon-remove"></span> Close
    </button>
    <div class="filter-cat">
        <form class="journals" method=post action="search.php">
            <input type="hidden" name="start" value="0">
            <input type="hidden" name="suchbegriff" value="<?php print_r($suchTerm); ?>">
            <input type="hidden" name="year" value="<?php print_r($facetYear) ?>">

            <?php
            if ($facetJournal == null) {
                ?>

                <button type="submit" class="linkselected">All journals</button>

                <?php
            } else {
                ?>

                <button type="submit">All journals</button>

                <?php
            }
            ?>
        </form>

        <?php
        $facet_data = $response->facet_counts->facet_fields['journal'];
        $max = 9;
        foreach ($facet_data as $jrnl => $jrnlcount) {
            ?>

            <form method=post action="search.php">
                <input type="hidden" name="start" value="0">
                <input type="hidden" name="suchbegriff" value="<?php print_r($suchTerm); ?>">
                <input type="hidden" name="journal" value="<?php print_r($jrnl); ?>">
                <input type="hidden" name="year" value="<?php print_r($facetYear) ?>">

                <?php
                if ($facetJournal == $jrnl) {
                    ?>

                    <button type="submit" class="linkselected"><?php print_r($jrnl . " (" . $jrnlcount . ")"); ?></button>

                    <?php
                } else {
                    ?>

                    <button type="submit"><?php print_r($jrnl . " (" . $jrnlcount . ")"); ?></button>

                    <?php
                }
                ?>
            </form>

            <?php
            $max--;
            if ($max < 0) {
                break;
            }
        }
        ?>
    </div>
</div>