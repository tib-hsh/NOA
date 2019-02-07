<?php include ("header.php"); ?>

<div class="gradient">

    <header>
        <nav class="navbar">
            <div class="noa container">
                <span class="last-update">Last Update: June 8th, 2018</span>
                <a href="http://noa.wp.hs-hannover.de/"><img src="img/noa-weiss.png" class="index-logo"/></a>
                <h1 class="small-h1">Scientific Image Search</h1>
                <span class="tooltiptext">Reuse of open access media / Nachnutzung von Open-Access-Abbildungen</span>
            </div>
        </nav>
    </header>	

    <div class="container search">
        <h1 class="large-h1">Scientific Image Search</h1>
        <form class="col-md-8 col-md-offset-2 col-sm-10 col-sm-offset-1 col-xs-12" action="search.php" method="get">
            <div class="input-field input-search col-sm-9 col-xs-9">
                <span class="glyphicon glyphicon-search"></span>
                <input class="input-width" type="text" placeholder="Search term" name="suchbegriff">
                <span id='clear-input' class="glyphicon glyphicon-remove"></span>
            </div>
            <input type ="hidden" name="start" value="0">
            <div class="search-button col-sm-3 col-xs-3">
                <button type="submit" class="noa-button noa-button-no-shadows search-submit-button">Search</button>
                <a href="search-description.php" class="noa-button noa-button-no-shadows help-button">
                    <span class="glyphicon glyphicon-question-sign">
                        <span class="tooltiptext3">Search Description</span>
                    </span>
                </a>
            </div>
        </form>
        <!-- <p class="col-xs-12 notice-english">Please notice: Currently NOA only works with english search terms.</p> -->
    </div>
</div><!-- .gradient closes -->

<div class="container main-content">
    <div class="col-md-6">
        <!-- start feedwind code --> 
        <script type="text/javascript" src="https://feed.mikle.com/js/fw-loader.js" data-fw-param="64045/"></script> 
        <!-- end feedwind code -->
    </div>
    <div class="col-md-6">
        <div class="publications panel">
            <div class="panel-heading">Publications in Project</div>
            <div class="panel-body">
                <ul>
                    <li>
                        <ul>
                            <li>Lucia Sohmen, Jean Charbonnier, Ina Blümel, Christian Wartena, Lambert Heller</li>
                            <li class="pub-title">Figures in Open Access Scientific Publications</li>
                            <li>Paper accepted for TPDL 2018</li>
                        </ul>
                    </li>
                    <li>
                        <ul>
                            <li>Frieda Josi, Christian Wartena, Jean Charbonnier</li>
                            <li class="pub-title">Text-based annotation of scientific images using Wikimedia categories</li>
                            <li>TIR 2018 - <a href="https://serwiss.bib.hs-hannover.de/frontdoor/index/index/searchtype/latest/docId/1248/start/0/rows/10" rel="noopener" target="_blank">Link zu Serwiss <span class="glyphicon glyphicon-new-window"/></a></li>
                        </ul>
                    </li>
                    <li>
                        <ul>
                            <li>Jean Charbonnier, Christian Wartena</li>
                            <li class="pub-title">Using Word Embeddings for Unsupervised Acronym Disambiguation</li>
                            <li>Coling 2018 - <a href="http://aclweb.org/anthology/C18-1221" rel="noopener" target="_blank">Link zu Serwiss <span class="glyphicon glyphicon-new-window"/></a></li>
                        </ul>
                    </li>
                    <li>
                        <ul>
                            <li>Jean Charbonnier, Lucia Sohmen, John Rothman, Birte Rohden, Christian Wartena</li>
                            <li class="pub-title">NOA: A Search Engine for Reusable Scientific Images Beyond the Life Sciences </li>
                            <li>27.03.2018 - <a href="https://serwiss.bib.hs-hannover.de/frontdoor/index/index/docId/1208" rel="noopener" target="_blank">Link zu Serwiss <span class="glyphicon glyphicon-new-window"/></a></li>
                        </ul>
                    </li>
                    <li>
                        <ul>
                            <li>Frieda Josi</li>
                            <li class="pub-title">Textbasierte Annotation von Abbildungen mit Kategorien von Wikimedia</li>
                            <li>12.02.2018 - <a href="https://serwiss.bib.hs-hannover.de/frontdoor/index/index/docId/1194" rel="noopener" target="_blank">Master Thesis <span class="glyphicon glyphicon-new-window"/></a></li>
                        </ul>
                    </li>
                    <li>
                        <ul>
                            <li>Birte Rohden</li>
                            <li class="pub-title">Konzeption, Entwicklung und Evaluation einer Web-Oberfläche für die wissenschaftliche Bildersuchmaschine NOA</li>
                            <li>16.01.2018 - <a href="https://serwiss.bib.hs-hannover.de/frontdoor/index/index/docId/1188" rel="noopener" target="_blank">Bachelor Thesis <span class="glyphicon glyphicon-new-window"/></a></li>
                        </ul>
                    </li>
                    <li>
                        <ul>
                            <li>Lucia Sohmen</li>
                            <li class="pub-title">Erste Ergebnisse des DFG geförderten Projekts NOA – Nachnutzung von Open-Access-Abbildungen.</li>
                            <li>12.09.2017 - <a href="http://blogs.tib.eu/wp/noa/wp-content/uploads/sites/19/2017/09/Erste-Ergebnisse-des-DFG-gefo%CC%88rderten-Projekts-NOA-%E2%80%93-Nachnutzung.pdf">Link zum Vortrag <span class="glyphicon glyphicon-new-window"/></a></li>
                        </ul>
                    </li>
                    <li>
                        <ul>
                            <li>Lambert Heller</li>
                            <li class="pub-title">Das DFG-Projekt NOA. Wissenschaftskommunikation im Zeitalter von Open Access und Open Science.</li>
                            <li>13.12.2016 - <a href="http://blogs.tib.eu/wp/noa/wp-content/uploads/sites/19/2017/04/L3S-Poster-Parla-Abend-Open-Science.pdf">L3S Open Science <span class="glyphicon glyphicon-new-window"/></a></li>
                        </ul>
                    </li>
                    <li>
                        <ul>
                            <li>Ina Blümel, Simone Cartellieri, Lambert Heller, Christian Wartena</li>
                            <li class="pub-title">Discovery and efficient reuse of technology pictures using Wikimedia infra­structures.</li>
                            <li>08.07.2016 - <a href="https://zenodo.org/record/51562" rel="noopener" target="_blank">https://zenodo.org/record/51562 <span class="glyphicon glyphicon-new-window"/></a></li>
                        </ul>
                    </li>
                    <li>
                        <ul>
                            <li>Ina Blümel, Simone Cartellieri, Lambert Heller, Christian Wartena</li>
                            <li class="pub-title">Entwicklung eines Verfahrens zur automatischen Sammlung, Erschließ­ung und Bereitstellung multimedialer Open-Access-Objekte mittels der Infrastruktur von Wikimedia Commons und Wikidata.</li>
                            <li>30.10.2014 - <a href="https://serwiss.bib.hs-hannover.de/frontdoor/index/index/docId/675" rel="noopener" target="_blank">urn:nbn:de:bsz:960-opus4-6755 <span class="glyphicon glyphicon-new-window"/></a></li>
                        </ul>
                    </li>
                </ul>
            </div>
        </div>
    </div>
</div>
</div><!-- #wrapper closes -->

<?php include ("footer.php"); ?>