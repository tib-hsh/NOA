<!DOCTYPE html>
<html>
<head>
<meta name="viewport" content="width=device-width, initial-scale=1">
	<head>
	    <meta charset="utf-8">
	    <meta http-equiv="X-UA-Compatible" content="IE=edge">
	    <meta name="robots" content="noimageindex, noimageclick" />
		<meta name="viewport" content="width=device-width, initial-scale=1.0">
	    <title>NOA - Scientific Image Search</title>
<style>
* {box-sizing: border-box}
body {font-family: Verdana, sans-serif; margin:0}

.mySlides {
	display: none;
	background-color: #555;
}

.slideimg {
	height: 500px;
	display:block;
	margin-left:auto;
	margin-right:auto;
}
	
.navbar {
	text-align: center;
	margin-bottom: unset;
}

.index-logo {
	margin: 20px 0 0; 
	width: 10%;
}

.noa {
    color: #333;
	background-color: #333;
	text-align: center;
    padding: 15px;
    border-radius: 3px;
}


.small-h1 {
	font-size: 24px;
	color: #fff;
	margin-bottom: 0;
}
	
	
	

/* Slideshow container */
.slideshow-container {
  width: 100%;
  position: relative;
  margin: auto;
}


/* Position the "next button" to the right */
.next {
  right: 0;
  border-radius: 3px 0 0 3px;
}

/* On hover, add a black background color with a little bit see-through */
.prev:hover, .next:hover {
  background-color: rgba(0,0,0,0.8);
}


/* Fading animation */
.fade {
  -webkit-animation-name: fade;
  -webkit-animation-duration: 1.5s;
  animation-name: fade;
  animation-duration: 1.5s;
}

@-webkit-keyframes fade {
  from {opacity: .4} 
  to {opacity: 1}
}

@keyframes fade {
  from {opacity: .4} 
  to {opacity: 1}
}


</style>
</head>
<body>

<header>
	<nav class="navbar">
		<div class="noa container">
			<a href="http://noa.wp.hs-hannover.de/"><img src="img/noa-weiss.png" class="index-logo"/></a>
			<h1 class="small-h1">Scientific Image Search</h1>
		</div>
	</nav>
</header>	

<div class="slideshow-container">

<!--
<div class="mySlides fade">
  <img src="./gallery/12898_2016_90_Fig2_HTML.jpg" class="slideimg">
</div>

<div class="mySlides fade">
  <img src="./gallery/537986.fig.0027b.jpg" class="slideimg">
</div>

<div class="mySlides fade">
  <img src="./gallery/2617597.fig.004b.jpg" class="slideimg">
</div>
-->
<?php
    $path = "./gallery";
    $all_files = scandir($path);
    $numimg = count($all_files);
    for ($i=2; $i<$numimg;$i++) {
	   echo "<div class=\"mySlides fade\">";
       echo "<img src=\"./gallery/$all_files[$i]\" class=\"slideimg\"/>";
	   echo "</div>";
    }
   ?>


</div>
<br>


<script>
var slideIndex = 1;
showSlides(slideIndex);
setInterval(next, 3600);

function plusSlides(n) {
  showSlides(slideIndex += n);
}

function currentSlide(n) {
  showSlides(slideIndex = n);
}

function showSlides(n) {
  var i;
  var slides = document.getElementsByClassName("mySlides");
  var dots = document.getElementsByClassName("dot");
  if (n > slides.length) {slideIndex = 1}    
  if (n < 1) {slideIndex = slides.length}
  for (i = 0; i < slides.length; i++) {
      slides[i].style.display = "none";  
  }
  for (i = 0; i < dots.length; i++) {
      dots[i].className = dots[i].className.replace(" active", "");
  }
  slides[slideIndex-1].style.display = "block";  
}

function next() {
	slideIndex += 1;
	showSlides(slideIndex);
}

</script>

</body>
</html> 
