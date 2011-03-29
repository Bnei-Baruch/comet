(function($)
{
	$.Chat = function(app)
	{
		// Initialization
		var _self = this;
		var _app = app;
		var _channels = new Array();
		var _privateMessageHandler = null;
		var _privateServiceChannel = "privatechat";
		
		_app.subscribeToServiceChannel(_privateServiceChannel, 
		function(message){
		  if (_privateMessageHandler != null)
		  	_privateMessageHandler(message.data);
		});
				
		this.addChannel = function(channel, receiveMessageHandler)
		{
			_app.subscribe(channel, receiveMessageHandler);
			_channels.push(channel);
		}
		
		this.sendMessageToChannel = function(channel, message)
		{
			_app.send(channel, message);
		}
		
		this.leaveChannel = function(channel)
		{
			_channels.remove(channel);
			_app.unsubscribe(channel);
		}
		
		this.setPrivateMessageReceivedHandler = function(privateMessageReceived)
		{
			_privateMessageHandler = privateMessageReceived;
		}
		
		this.sendPrivateChat = function(to, message)
		{
			_app.publishToService(_privateServiceChannel, {"to":to, "message":message})
		}
		
		// Admin action, specially authenticated
		this.blockUser(username)
		{
		}
		
		this.unblockUser(username)
		{
		}
		
		this.getBlockedUsers()
		{
		}
	}

})(jQuery);