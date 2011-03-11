(function($)
{

$(document).ready(function()
{
	config.labels = new LabelsHeb();	
	$('#sendButton').html(config.labels.get('send'));
	$('#smilyButton').html(config.labels.get('smily'));
	$('#closeAdvanced').html(config.labels.get('closeAdvanced'));
	$('#private').html(config.labels.get('private'));
	//var a = $('#privateSpan');
	$('#privateSpan').attr('value', config.labels.get('privateSpan')+';');

});

function LabelsHeb()
{
	var _map = {};
	
	_map.send = 'שלח';
	_map.system = 'הודעת מערכת:';
	_map.connectionOpened = 'אתה מחובר';
	_map.connectionClose = 'אתה מנותק';
	_map.connectionBroken = 'הקשר שבור';
	_map.leftTheChannel = 'עזב את ערוץ ';
	_map.joinedTheChannel = 'הצטרף לערוץ ';
	_map.private = 'נמען:';
	_map.privateMessageFrom = 'הודעה פרטית מ-';
	_map.closeAdvanced = 'סגור';
	_map.privateSpan = 'כולם';
	_map.smily = 'סמיילי';
	_map.privateMessage = 'שלח הודעה פרטית';
	_map.offensive = 'סמן הודעה כפוגעת';
	_map.yes = 'כן';
	_map.no = 'לא';
	_map.rushur = 'האם אתה בטוח כי ההודעה פוגעת?';
	_map.chooseTo = 'בחר נמענים';
	_map.overlayMessage = "התחבר על מנת<br> להפעיל צ'ט";

	this.get = function(labelName)
	{
		return _map[labelName];
	}
}

})(jQuery);