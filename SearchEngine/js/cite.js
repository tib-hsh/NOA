// cite function - gets data from html tree
function cite(img, view, cite_type) {
	switch (view) {
		case 'list':
			var parentDiv = img.closest('.description-column');		
		break;
		case 'gallery':
		default:
			var parentDiv = img.closest('.metadata');
	}
	var metadata = $(parentDiv).find('.hiddenmetadata');
	var classNames = ['cite-paper', 'cite-journal', 'cite-year', 'cite-author','cite-doi','cite-publisher'];
	var informationArr = new Object;
	for (var i = 0; i < classNames.length; i++) {
		informationArr[classNames[i]] = $(metadata).find('.'+classNames[i]).text();
	}
	

	switch (cite_type) {
		case 'ris':
			var risString = new String;
			risString = risString.concat('TY%20%20-%20JOUR');
			risString = risString.concat('%0A');
			risString = risString.concat('TI%20%20-%20' + encodeURI(informationArr['cite-paper']));
			risString = risString.concat('%0A');
			risString = risString.concat('AU%20%20-%20' + encodeURI(informationArr['cite-author']));
			risString = risString.concat('%0A');
			risString = risString.concat('T2%20%20-%20' + encodeURI(informationArr['cite-journal']));
			risString = risString.concat('%0A');
			risString = risString.concat('DO%20%20-%20' + encodeURI(informationArr['cite-doi']));
			risString = risString.concat('%0A');
			risString = risString.concat('PB%20%20-%20' + encodeURI(informationArr['cite-publisher']));
			risString = risString.concat('%0A');
			risString = risString.concat('PY%20%20-%20' + encodeURI(informationArr['cite-year']));
			risString = risString.concat('%0A');
			risString = risString.concat('ER%20%20-%20');
			var a         = document.createElement('a');
			a.href        = 'data:application/x-research-info-systems,' + risString;
			a.target      = '_blank';
			a.download    = 'cite.ris';
			document.body.appendChild(a);
			a.click();
			return true;
			break;

		case 'bibtex':
		default:
			var bibtexString = new String;
			bibtexString = bibtexString.concat('@article{LABEL,');
			bibtexString = bibtexString.concat('<br/>');
			bibtexString = bibtexString.concat('&nbsp;&nbsp;&nbsp;&nbsp;title={'+informationArr['cite-paper']+'},');
			bibtexString = bibtexString.concat('<br/>');			
			bibtexString = bibtexString.concat('&nbsp;&nbsp;&nbsp;&nbsp;author={'+informationArr['cite-author']+'},');
			bibtexString = bibtexString.concat('<br/>');
			bibtexString = bibtexString.concat('&nbsp;&nbsp;&nbsp;&nbsp;journal={'+informationArr['cite-journal']+'},');
			bibtexString = bibtexString.concat('<br/>');
			bibtexString = bibtexString.concat('&nbsp;&nbsp;&nbsp;&nbsp;year={'+informationArr['cite-year']+'},');
			bibtexString = bibtexString.concat('<br/>');
			bibtexString = bibtexString.concat('&nbsp;&nbsp;&nbsp;&nbsp;publisher={'+informationArr['cite-publisher']+'},');
			bibtexString = bibtexString.concat('<br/>');
			bibtexString = bibtexString.concat('&nbsp;&nbsp;&nbsp;&nbsp;doi={'+informationArr['cite-doi']+'},');
			bibtexString = bibtexString.concat('<br/>');
			bibtexString = bibtexString.concat('}');

			window.open('about:blank','BibTeX - '+informationArr['cite-paper']).document.write(bibtexString);
	}
}