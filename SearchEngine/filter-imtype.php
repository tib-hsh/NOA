<div class="collapse" id="imgtype">
    <button class="toggle-close" type="button" data-toggle="collapse" data-target="#imgtype" aria-controls="byimgtype">
        <span class="glyphicon glyphicon-remove"></span> Close
    </button>
    <div class="filter-cat">
        <form class="imgtype" method=get action="search.php">
            <input type="hidden" name="start" value="0">
            <input type="hidden" name="suchbegriff" value="<?php print_r(htmlspecialchars($suchTerm)) ?>">
            <input type="hidden" name="discipline" value="<?php print_r($facetDiscipline); ?>">

            <?php
            if ($facetImtype == null) {
                ?>

                <button type="submit" class="linkselected">All image types</button>

                <?php
            } else {
                ?>

                <button type="submit">All image types</button>

                <?php
            }
            ?>
        </form>

        <?php
        $facet_data = $response->facet_counts->facet_fields['imtype'];
        foreach ($facet_data as $imt => $imtcount) {
            ?>

            <form method=get action="search.php">
                <input type="hidden" name="start" value="0">
                <input type="hidden" name="suchbegriff" value="<?php print_r(htmlspecialchars($suchTerm)); ?>">
                <input type="hidden" name="discipline" value="<?php print_r($facetDiscipline); ?>">
                <input type="hidden" name="imtype" value="<?php print_r($imt); ?>">

                <?php
                if ($facetImtype == $imt) {
                    ?>

                    <button type="submit" class="linkselected"><?php print_r($imt . " (" . $imtcount . ")"); ?></button>

                    <?php
                } else {
                    ?>

                    <button type="submit"><?php print_r($imt . " (" . $imtcount . ")"); ?></button>

                    <?php
                }
                ?>
            </form>

            <?php
        }
        ?>
    </div>
</div>