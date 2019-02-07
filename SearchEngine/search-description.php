<?php include ("navbar.php"); ?>

<div class="main">
    <div class="container">

        <div class="search-description col-sm-10 col-sm-offset-1">
            <h1 class="h2">Search description for NOA</h1>
            <p>Searching with <a href="http://noa.wp.hs-hannover.de" target="_blank">Scientific Image Search NOA</a> is quick and easy to do. You can use different search options for your queries. </p>

            <section class="table-of-contents-section">
                <!-- It's generated dynamically, please don't delete!!-->
            </section>

            <section>
                <h2 id="sec2" class="h3">1. Default search with terms</h2>	
                <p>Search terms are searched for in the fields <b>caption</b>,  <b>author</b>, <b>title</b> and <b>journal-title</b>. All terms are automatically combined with <b>OR</b>.
            </section>

            <section>
                <h2 id="sec3" class="h3">2. Boolean operators</h2>	
                <p>The default query operator is OR. The boolean operators <b>AND</b> and <b>NOT</b> can be used as an additional syntax:</p>
                <ul>
                    <li>term 1 <b>AND</b> term 2</li>
                    <li>term 1 <b>OR</b> term 2  &#8658; <i>default search</i></li>
                    <li>term 1 <b>NOT</b> term 2</li>
                    <li>term 1 <b>AND</b> term 2 <b>NOT</b> term 3</li>
                    <li>term 1 <b>AND</b> (term 2 <b>OR</b> term 3)</li>
                    <li>term 1 <b>NOT</b> (term 2 <b>OR</b> term 3) <br/>e.g.  smoke <b>NOT</b> (aspiration OR system)</li>
                </ul>
                <p><b>AND</b> is equivalent to <b>+</b></p>
                <p><b>NOT</b> is equivalent to <b>-</b></p>
                <h5>Combinations and sub-queries</h5>
                <p>You can use parenthesis to compose a query of smaller queries, referred to as sub-queries or nested queries.</p>
                <ul>
                    <li>term 1 + term 2 <b>NOT</b> (term 3 <b>OR</b> term 4)<br />
                        e.g. Stokes Efficiency Molecular <b>NOT</b> (Motor-Cargo <b>OR</b> Systems)</li>
                </ul>

                <figure>
                    <img class="img-responsive centered" src="img/stokes.png"/>
                    <figcaption> Fig 1: Stokes Efficiency Molecular NOT (Motor-Cargo OR Systems)</figcaption>
                </figure>
            </section>

            <section>
                <h2 id="sec4" class="h3">3. Wildcard queries</h2>	
                <p>Additional you can use a query for a partial match expressed using wildcards. The asterisk matches any number of characters (perhaps none). No text analysis is performed on the search word containing the wildcard, not even lowercasing. Wildcard queries are one of the slowest types you can run.</p>
                <ul>
                    <li>term*  <br/>e.g. "pathologic findings of toxic" hepatic*     &#8658; <i>right truncation only </i></li>
                    <li>term*suffix  </li>
                    <li>term?suffix    &#8658; <i>Exactly one character is replaced.</i> </li>
                </ul>

                <figure>
                    <img class="img-responsive centered" src="img/hepatitis.png"/>
                    <figcaption> Fig 2: "pathologic findings of toxic" hepatic* </figcaption>
                </figure>
            </section>

            <section>
                <h2 id="sec5" class="h3">4. Phrase queries</h2>	
                <p>You can use phrase query, a contiguous series of words to be matched in that order.</p>
                <ul>
                    <li>"term 1 term 2 term 3" <br />  e.g. "Bahloul sections in Tunisia"</li>
                </ul>
                <p>If you wanted to permit these words to be separated by no more than four words in between, then you could do this:</p>
                <ul>
                    <li>"term 1 term 2 term 3"~4  <br /> e.g. "infiltrating acanthotic"~4</li>
                </ul>
                <figure>
                    <img class="img-responsive centered" src="img/acanthotic.png"/>
                    <figcaption> Fig 3: "infiltrating acanthotic"~4 </figcaption>
                </figure>
            </section>

            <section>
                <h2 id="sec6" class="h3">5. Fuzzy queries</h2>	
                <p>Fuzzy queries are useful when your search term needn't be an exact match, but the closer the better. The fewer the number of character insertions, deletions, or exchanges relative to the search term length, the better the score.  Fuzzy queries have the same need to lowercase and to avoid stemming just as wildcard queries do.</p>
                <ul>
                    <li>term~  <br /> e.g. stream~</li>
                </ul>
                <figure>
                    <img class="img-responsive centered" src="img/stream.png"/>
                    <figcaption> Fig 4: stream~ </figcaption>
                </figure>							
            </section>

            <section>
                <h2 id="sec7" class="h3">6. Filter search results</h2>	
                <p>You can filter results by year or by discipline.</p>
                <figure>
                    <img class="img-responsive centered" src="img/filter.png">
                    <figcaption> Fig 5: Filter by discipline </figcaption>
                </figure>							
            </section>

            <section>
                <h2 id="sec8" class="h3">7. Extended search with field qualifiers</h2>	
                <p>You can search in the following fields.</p>
                <ul>
                    <li>journal <br /> e.g. journal:Ecological Processes </li>
                    <li>year <br /> e.g. year:2010</li>
                    <li>discipline <br /> e.g.  discipline: Computer Science</li>
                    <li>title <br />	e.g.  title:Access Network Selection Based on Fuzzy Logic and Genetic Algorithms</li>
                    <li>author  <br /> e.g.  author:Taekyun</li>
                    <li>doi <br />	e.g.  doi:10.1186/1741-7007-7-6   </li>
                    <li>caption   <br />   e.g. caption:Membership functions of the input variables.</li>
                    <li>category <br />   e.g.  cat:"Risk analysis" </li>
                </ul>
                <figure>
                    <img class="img-responsive centered" src="img/doi.png"/>
                    <figcaption> Fig 6: doi:10.1186/1741-7007-7-6  </figcaption>
                </figure>							

                <h5>Merging field qualifiers</h5>
                <ul>
                    <li>field1:term 1  AND field2:term 2 <br />e.g. year:2008 AND author:Hongyun </li>
                </ul>
                <figure>
                    <img class="img-responsive centered" src="img/author.png"/>
                    <figcaption> Fig 7: year:2008 AND author:Hongyun</figcaption>
                </figure>

                <h5>Merging wildcards with field qualifiers</h5>
                <ul>
                    <li>field1:term 1  field2:term 2*   <br />e.g. author:jiggins  journal-title:Ecological* </li>
                </ul>
                <figure>
                    <img class="img-responsive centered" src="img/title.png"/>
                    <figcaption> Fig 8: author:jiggins  journal-title:Ecological*</figcaption>
                </figure>





            </section>





            <section>
                <h2 id="sec9" class="h3">Reference </h2>	
                <p>Apache Solr 3 Enterprise Search Server: Smiley, David and Pugh, David Eric, 2011</p>
            </section>
        </div>

    </div><!-- .container closes -->
</div><!-- .main closes -->
</div><!-- #wrapper closes -->

<?php include ("footer.php"); ?>
