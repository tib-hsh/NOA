function settext(qresult,category,placeholder) {
	console.log(qresult)
	var pages  = qresult['query']['pages'];
	for (var pageid in pages) {
		text = pages[pageid]['extract'];
	}
	
	if(text.length > 2){
		text = '<b>'+category+'. </b>'+text+	
		'<i> Source: <a target="_blank" href="https://en.wikipedia.org/wiki/Category:'+category+'">Wikipedia.</a></i><span class="glyphicon glyphicon-new-window"/></a>';
	} else {
		text = 'Read more on <b>' + category + '</b> on <a target="_blank" href="https://en.wikipedia.org/wiki/Category:'+category+'">Wikipedia.<span class="glyphicon glyphicon-new-window"/></a>';
	}
	
	console.log(placeholder)
	placeholder.innerHTML = text;
}

function wikipedia(category,placeholder) {
	title = 'Category:'+category.replace(new RegExp(' ', 'g'), '_')
	
	$.ajax({
		global: false,
		url: "https://en.wikipedia.org/w/api.php",
		data: {
			format: "json",
			action:'query',
			titles:title,
			prop:'extracts',
			explaintext: true
		},
		dataType: 'jsonp',
		headers: {
			'Api-User-Agent': 'NOA (http://noa.wp.hs-hannover.de/)'
		},
		success: function (data) {
			console.log(data)
			settext(data,category,placeholder);
		}
	});
}