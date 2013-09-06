if (typeof jQuery !== 'undefined') {
	(function($) { 
		$('#spinner').ajaxStart(function() {
			$(this).fadeIn();
		}).ajaxStop(function() {
			$(this).fadeOut();
		});
	})(jQuery);
}

function findIndexByKeyValue(obj, key, value) {
    for (var i = 0; i < obj.length; i++) {
        if (obj[i][key] == value) { 
            return i;
        }
    }
    return null;
}

function escapeHtml(str) {
    var div = document.createElement('div');
    div.appendChild(document.createTextNode(str));
    return div.innerHTML;
}