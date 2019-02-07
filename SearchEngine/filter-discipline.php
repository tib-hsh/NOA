<div class="collapse" id="disciplines">
    <button class="toggle-close" type="button" data-toggle="collapse" data-target="#disciplines" aria-controls="bydiscipline">
        <span class="glyphicon glyphicon-remove"></span> Close
    </button>
    <div class="filter-cat">
        <form class="disciplines" method=get action="search.php">
            <input type="hidden" name="start" value="0">
            <input type="hidden" name="suchbegriff" value="<?php print_r(htmlspecialchars($suchTerm)); ?>">
            <input type="hidden" name="imtype" value="<?php print_r($facetImtype) ?>">

            <?php
            if ($facetDiscipline == null) {
                ?>

                <button type="submit" class="linkselected">All disciplines</button>

                <?php
            } else {
                ?>

                <button type="submit">All disciplines</button>

                <?php
            }
            ?>
        </form>

        <?php
        $facet_data = $response->facet_counts->facet_fields['discipline'];
        $max = 9;
        foreach ($facet_data as $dsc => $dsccount) {
            ?>

            <form method=get action="search.php">
                <input type="hidden" name="start" value="0">
                <input type="hidden" name="suchbegriff" value="<?php print_r(htmlspecialchars($suchTerm)); ?>">
                <input type="hidden" name="discipline" value="<?php print_r($dsc); ?>">
                <input type="hidden" name="imtype" value="<?php print_r($facetImtype) ?>">

                <?php
                if ($facetDiscipline == $dsc) {
                    ?>

                    <button type="submit" class="linkselected"><?php print_r($dsc . " (" . $dsccount . ")"); ?></button>

                    <?php
                } else {
                    ?>

                    <button type="submit"><?php print_r($dsc . " (" . $dsccount . ")"); ?></button>

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